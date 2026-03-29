package com.example.bdMetro.services;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.bdMetro.entity.AccessCode;
import com.example.bdMetro.repository.AccessCodeRepository;

@Service
public class AuthenticationService {
    @Autowired
    private AccessCodeRepository accessCodeRepository;

    private static final List<String> PROMOCIONALES = Arrays.asList(
            "gPgkyN1", "96XizD2", "pu39k73", "P2QZ954", "VpMwGb5", "2eoHmo6", "oay1o67", "zKHlVm8", "Klsyw09", "VKm5Mc10", "MwUQNu11", "LBaAwg12", "1zEFRj13", "EC42hW14", "S1IATP15", "mdACsM16", "quO4mi17", "YgUCVN18", "qypqH119", "s0xEZX20", "lSlov921", "PzPfbU22", "HWDG3o23", "MmJCGo24", "vB49t925", "jGMSNZ26", "cOCOBO27", "IP8AbG28", "RcPUtD29", "e0a5uT30");

    private static final List<String> CONTRASENAS = Arrays.asList(
            "Andr0meda656", "Yo9eiP", "NsUUbN", "ONkJCl", "03qEbm", "9ErI7P", "W3nnik", "rE5LIs", "U2uNlC", "AsIjg1", "ZEOqc7", "HjUjzp", "SIpeud", "TUpc5S", "CeeM6E", "RpfCY1", "EVCaxp", "T4OXZ2", "8BAg3W", "GcNEET", "lctnJq", "YqJS15", "0Uw9PB", "XURkW0", "5xb3PL", "ORfpbg", "MQsghI", "opUXHQ", "tZTZmn", "QQxfi3", "o3dqkU");

    public String login(String code) {
        AccessCode accessCode = accessCodeRepository.findByCode(code);
        if (accessCode == null) {
            return "Código no encontrado";
        } else if (accessCode.getEmail() == null) {
            return "Código existe pero no asignado a un usuario";
        } else {
            return accessCode.getEmail();
        }
    }

    public List<AccessCode> getAllCodes() {
        return accessCodeRepository.findAll();
    }

    public List<AccessCode> getCodesByPais(String pais) {
        return accessCodeRepository.findByPaisIgnoreCase(normalizeCountry(pais));
    }

    public AccessCode getCode(String code) {
        return accessCodeRepository.findByCode(code);
    }

    public AccessCode updateCode(String code, String email, String username, String telefono, String provincia, String pais) {
        AccessCode accessCode = accessCodeRepository.findByCode(code);
        if (accessCode == null) {
            throw new IllegalArgumentException("Este código no existe en la base de datos");
        }

        AccessCode emailAssignedCode = accessCodeRepository.findByEmail(email);
        if (emailAssignedCode != null && !emailAssignedCode.getCode().equals(code)) {
            throw new IllegalArgumentException("Este email está en la base de datos");
        }

        accessCode.setEmail(email);
        accessCode.setTelefono(telefono);
        accessCode.setProvincia(provincia);
        accessCode.setPais(resolveCountryForRegistration(accessCode.getPais(), pais));
        return accessCodeRepository.save(accessCode);
    }

    public AccessCode agregarCode(AccessCode accessCode) {
        AccessCode existingCode = accessCodeRepository.findByCode(accessCode.getCode());
        AccessCode existingEmail = accessCodeRepository.findByEmail(accessCode.getEmail());
        if (existingCode != null) {
            throw new IllegalArgumentException("Este código ya está creado");
        }
        if (existingEmail != null) {
            throw new IllegalArgumentException("Este email ya está creado");
        }
        accessCode.setPais(normalizeCountry(accessCode.getPais()));
        accessCode.setFechaRegistro(LocalDate.now());
        accessCode.setFechaVencimiento(calcularFechaVencimiento(accessCode.getCode()));
        return accessCodeRepository.save(accessCode);
    }

    public List<AccessCode> agregarCodes(List<AccessCode> accessCodes) {
        for (AccessCode accessCode : accessCodes) {
            AccessCode existingCode = accessCodeRepository.findByCode(accessCode.getCode());
            if (existingCode != null) {
                throw new IllegalArgumentException("El código " + accessCode.getCode() + " ya está creado");
            }
            accessCode.setPais(normalizeCountry(accessCode.getPais()));
            accessCode.setFechaRegistro(LocalDate.now());
            accessCode.setFechaVencimiento(calcularFechaVencimiento(accessCode.getCode()));
        }
        return accessCodeRepository.saveAll(accessCodes);
    }

    public AccessCode addCode(AccessCode accessCode) {
        AccessCode existingCode = accessCodeRepository.findByCode(accessCode.getCode());
        if (existingCode == null) {
            throw new IllegalArgumentException("Código no encontrado");
        } else if (existingCode.getEmail() != null) {
            throw new IllegalArgumentException("Código ya tiene un email asignado");
        }

        AccessCode emailAssignedCode = accessCodeRepository.findByEmail(accessCode.getEmail());
        if (emailAssignedCode != null) {
            throw new IllegalArgumentException("El email ya está asignado a otro código");
        }

        existingCode.setEmail(accessCode.getEmail());
        existingCode.setTelefono(accessCode.getTelefono());
        existingCode.setProvincia(accessCode.getProvincia());
        existingCode.setPais(resolveCountryForRegistration(existingCode.getPais(), accessCode.getPais()));
        existingCode.setFechaRegistro(LocalDate.now());
        existingCode.setFechaVencimiento(calcularFechaVencimiento(existingCode.getCode()));
        return accessCodeRepository.save(existingCode);
    }

    private LocalDate calcularFechaVencimiento(String code) {
        LocalDate fechaRegistro = LocalDate.now();
        return code.length() == 5 ? fechaRegistro.plusMonths(3) : fechaRegistro.plusMonths(6);
    }

    public void deleteCode(String code) {
        accessCodeRepository.deleteById(code);
    }

    private String resolveCountryForRegistration(String existingCountry, String requestedCountry) {
        String normalizedExistingCountry = normalizeCountry(existingCountry);
        String normalizedRequestedCountry = normalizeCountry(requestedCountry);

        if (normalizedExistingCountry != null && !normalizedExistingCountry.isBlank()) {
            if (normalizedRequestedCountry != null
                    && !normalizedRequestedCountry.isBlank()
                    && !normalizedExistingCountry.equals(normalizedRequestedCountry)) {
                throw new IllegalArgumentException(
                        "El pais seleccionado no coincide con el pais del codigo. Este codigo pertenece a "
                                + normalizedExistingCountry + ".");
            }
            return normalizedExistingCountry;
        }

        return normalizedRequestedCountry;
    }

    private String normalizeCountry(String country) {
        if (country == null) {
            return null;
        }

        String trimmed = country.trim();
        if (trimmed.isBlank()) {
            return trimmed;
        }

        return switch (trimmed.toLowerCase()) {
            case "ar", "argentina" -> "Argentina";
            case "uy", "uruguay" -> "Uruguay";
            case "co", "colombia" -> "Colombia";
            default -> trimmed;
        };
    }
}
