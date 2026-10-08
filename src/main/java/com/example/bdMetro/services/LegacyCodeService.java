package com.example.bdMetro.services;

import com.example.bdMetro.dto.ImportResultDTO;
import com.example.bdMetro.entity.AccessCode;
import com.example.bdMetro.entity.LegacyCode;
import com.example.bdMetro.repository.AccessCodeRepository;
import com.example.bdMetro.repository.LegacyCodeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LegacyCodeService {

    @Autowired
    private LegacyCodeRepository legacyCodeRepository;

    @Autowired
    private AccessCodeRepository accessCodeRepository;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private ObjectMapper objectMapper;

    public ImportResultDTO importFromJson() throws IOException {
        ClassPathResource resource = new ClassPathResource("codigosmetro.json");
        JsonNode root = objectMapper.readTree(resource.getInputStream());
        JsonNode registros = root.get("registros");
        int imported = 0;
        int skipped = 0;
        int total = registros.size();

        for (JsonNode node : registros) {
            String code = node.get("code").asText().trim().toUpperCase();
            if (code.length() > 20 || legacyCodeRepository.existsByCode(code)) {
                skipped++;
                continue;
            }
            LegacyCode lc = new LegacyCode();
            lc.setCode(code);
            lc.setMeses(node.get("meses").asInt());
            lc.setFechaAdquisicion(parseLocalDate(node.get("fechaAdquisicion")));
            lc.setFechaVencimiento(parseLocalDate(node.get("fechaVencimiento")));
            lc.setFechaCreacion(parseLocalDateTime(node.get("fechaCreacion")));
            legacyCodeRepository.save(lc);
            imported++;
        }
        return new ImportResultDTO(imported, skipped, total);
    }

    public ImportResultDTO importFromStream(InputStream inputStream) throws IOException {
        JsonNode root = objectMapper.readTree(inputStream);
        JsonNode registros = root.get("registros");
        if (registros == null || !registros.isArray())
            throw new IllegalArgumentException("El JSON no contiene un array 'registros'.");
        int imported = 0;
        int skipped = 0;
        int total = registros.size();
        for (JsonNode node : registros) {
            String code = node.get("code").asText().trim().toUpperCase();
            if (code.length() > 20 || legacyCodeRepository.existsByCode(code)) {
                skipped++;
                continue;
            }
            LegacyCode lc = new LegacyCode();
            lc.setCode(code);
            lc.setMeses(node.get("meses").asInt());
            lc.setFechaAdquisicion(parseLocalDate(node.get("fechaAdquisicion")));
            lc.setFechaVencimiento(parseLocalDate(node.get("fechaVencimiento")));
            lc.setFechaCreacion(parseLocalDateTime(node.get("fechaCreacion")));
            legacyCodeRepository.save(lc);
            imported++;
        }
        return new ImportResultDTO(imported, skipped, total);
    }

    public Optional<LegacyCode> checkCode(String code) {
        return legacyCodeRepository.findByCode(code.trim().toUpperCase()).filter(lc ->
            !lc.isClaimed() &&
            lc.getFechaVencimiento() != null &&
            !lc.getFechaVencimiento().isBefore(LocalDate.now())
        );
    }

    @Transactional
    public Optional<AccessCode> claimAndActivate(String code, String email, String telefono, String provincia) {
        String normalizedCode = code.trim().toUpperCase();
        return legacyCodeRepository.findByCode(normalizedCode).map(lc -> {
            if (lc.isClaimed())
                throw new IllegalStateException("Este código ya fue reclamado.");
            if (lc.getFechaVencimiento() == null || lc.getFechaVencimiento().isBefore(LocalDate.now()))
                throw new IllegalStateException("Este código está vencido.");

            String normalizedEmail = email.trim().toLowerCase();
            if (accessCodeRepository.findByEmail(normalizedEmail) != null)
                throw new IllegalArgumentException("Este email ya está registrado en el sistema.");
            if (accessCodeRepository.findByCodeIgnoreCase(normalizedCode) != null)
                throw new IllegalStateException("Este código ya tiene una cuenta activa.");

            AccessCode ac = new AccessCode();
            ac.setCode(normalizedCode);
            ac.setEmail(normalizedEmail);
            ac.setTelefono(telefono);
            ac.setProvincia(provincia);
            ac.setPais("Argentina");
            ac.setFechaRegistro(LocalDate.now());
            ac.setFechaVencimiento(lc.getFechaVencimiento());
            ac.setLegacy(true);
            accessCodeRepository.save(ac);

            lc.setClaimed(true);
            lc.setClaimedByEmail(normalizedEmail);
            lc.setClaimedAt(LocalDateTime.now());
            legacyCodeRepository.save(lc);

            return ac;
        });
    }

    @Transactional
    public void deleteLegacyCode(String code) {
        String normalizedCode = code.trim().toUpperCase();
        LegacyCode lc = legacyCodeRepository.findByCode(normalizedCode)
            .orElseThrow(() -> new IllegalArgumentException("Código legacy no encontrado."));

        if (lc.isClaimed()) {
            AccessCode ac = accessCodeRepository.findByCodeIgnoreCase(normalizedCode);
            if (ac != null) {
                authenticationService.deleteUserData(normalizedCode);
                authenticationService.deleteCode(normalizedCode);
            }
        }
        legacyCodeRepository.delete(lc);
    }

    @Transactional
    public int deleteExpiredUnclaimed() {
        List<LegacyCode> expired = legacyCodeRepository.findAll().stream()
            .filter(lc -> !lc.isClaimed() &&
                         lc.getFechaVencimiento() != null &&
                         lc.getFechaVencimiento().isBefore(LocalDate.now()))
            .toList();
        legacyCodeRepository.deleteAll(expired);
        return expired.size();
    }

    @Transactional
    public int deleteAll() {
        List<LegacyCode> all = legacyCodeRepository.findAll();
        for (LegacyCode lc : all) {
            if (lc.isClaimed()) {
                AccessCode ac = accessCodeRepository.findByCodeIgnoreCase(lc.getCode());
                if (ac != null) {
                    authenticationService.deleteUserData(lc.getCode());
                    authenticationService.deleteCode(lc.getCode());
                }
            }
        }
        legacyCodeRepository.deleteAll(all);
        return all.size();
    }

    @Transactional
    public void resetClaim(String code) {
        String normalizedCode = code.trim().toUpperCase();
        LegacyCode lc = legacyCodeRepository.findByCode(normalizedCode)
            .orElseThrow(() -> new IllegalArgumentException("Código legacy no encontrado."));
        if (!lc.isClaimed()) return;
        authenticationService.deleteUserData(normalizedCode);
        authenticationService.deleteCode(normalizedCode);
        lc.setClaimed(false);
        lc.setClaimedByEmail(null);
        lc.setClaimedAt(null);
        legacyCodeRepository.save(lc);
    }

    public List<LegacyCode> getAll() {
        return legacyCodeRepository.findAll();
    }

    private LocalDate parseLocalDate(JsonNode node) {
        if (node == null || node.isNull()) return null;
        return LocalDate.parse(node.asText());
    }

    private LocalDateTime parseLocalDateTime(JsonNode node) {
        if (node == null || node.isNull()) return null;
        String text = node.asText();
        // fechaCreacion comes as ISO-8601 with Z offset ("2026-09-29T14:41:14.833Z")
        return OffsetDateTime.parse(text).toLocalDateTime();
    }
}
