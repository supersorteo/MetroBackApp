package com.example.bdMetro.controller;

import com.example.bdMetro.dto.ClaimCodeRequest;
import com.example.bdMetro.dto.ImportResultDTO;
import com.example.bdMetro.dto.LegacyCodeDTO;
import com.example.bdMetro.entity.AccessCode;
import com.example.bdMetro.entity.LegacyCode;
import com.example.bdMetro.services.LegacyCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class LegacyCodeController {

    @Autowired
    private LegacyCodeService legacyCodeService;

    @PostMapping("/api/admin/legacy-codes/import")
    public ResponseEntity<?> importCodes() {
        try {
            ImportResultDTO result = legacyCodeService.importFromJson();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getClass().getSimpleName() + ": " + e.getMessage()));
        }
    }

    @PostMapping("/api/admin/legacy-codes/import-file")
    public ResponseEntity<?> importCodesFromFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "El archivo está vacío."));
        try {
            ImportResultDTO result = legacyCodeService.importFromStream(file.getInputStream());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getClass().getSimpleName() + ": " + e.getMessage()));
        }
    }

    @GetMapping("/api/admin/legacy-codes")
    public ResponseEntity<Page<LegacyCodeDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        List<LegacyCode> all = legacyCodeService.getAll();
        int start = page * size;
        int end = Math.min(start + size, all.size());
        if (start > all.size()) {
            return ResponseEntity.ok(new PageImpl<>(List.of(), PageRequest.of(page, size), all.size()));
        }
        List<LegacyCodeDTO> dtos = all.subList(start, end).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new PageImpl<>(dtos, PageRequest.of(page, size), all.size()));
    }

    @DeleteMapping("/api/admin/legacy-codes/expired")
    public ResponseEntity<Map<String, Object>> deleteExpired() {
        int deleted = legacyCodeService.deleteExpiredUnclaimed();
        return ResponseEntity.ok(Map.of("deleted", deleted));
    }

    @DeleteMapping("/api/admin/legacy-codes")
    public ResponseEntity<Map<String, Object>> deleteAll() {
        int deleted = legacyCodeService.deleteAll();
        return ResponseEntity.ok(Map.of("deleted", deleted));
    }

    @DeleteMapping("/api/admin/legacy-codes/{code}/claim")
    public ResponseEntity<?> resetClaim(@PathVariable String code) {
        try {
            legacyCodeService.resetClaim(code);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/api/admin/legacy-codes/{code}")
    public ResponseEntity<?> deleteLegacyCode(@PathVariable String code) {
        try {
            legacyCodeService.deleteLegacyCode(code);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /** Verifica si un código es legacy válido y vigente (no reclamado). */
    @GetMapping("/api/legacy-codes/check/{code}")
    public ResponseEntity<LegacyCodeDTO> checkCode(@PathVariable String code) {
        return legacyCodeService.checkCode(code)
                .map(lc -> ResponseEntity.ok(toDTO(lc)))
                .orElse(ResponseEntity.notFound().build());
    }

    /** Activa un código legacy: crea AccessCode y marca el legacy como reclamado. */
    @PostMapping("/api/legacy-codes/claim/{code}")
    public ResponseEntity<Map<String, String>> claimCode(
            @PathVariable String code,
            @RequestBody ClaimCodeRequest request) {
        try {
            return legacyCodeService.claimAndActivate(
                            code,
                            request.getEmail(),
                            request.getTelefono(),
                            request.getProvincia())
                    .map(ac -> {
                        Map<String, String> resp = new HashMap<>();
                        resp.put("message", "Código activado con éxito");
                        resp.put("code", ac.getCode());
                        resp.put("email", ac.getEmail());
                        resp.put("telefono", ac.getTelefono());
                        resp.put("pais", ac.getPais());
                        resp.put("provincia", ac.getProvincia());
                        resp.put("fechaRegistro", ac.getFechaRegistro() != null ? ac.getFechaRegistro().toString() : "");
                        resp.put("fechaVencimiento", ac.getFechaVencimiento() != null ? ac.getFechaVencimiento().toString() : "");
                        return ResponseEntity.ok(resp);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalStateException e) {
            return ResponseEntity.unprocessableEntity().body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    private LegacyCodeDTO toDTO(LegacyCode lc) {
        LegacyCodeDTO dto = new LegacyCodeDTO();
        dto.setCode(lc.getCode());
        dto.setMeses(lc.getMeses());
        dto.setFechaAdquisicion(lc.getFechaAdquisicion());
        dto.setFechaVencimiento(lc.getFechaVencimiento());
        dto.setFechaCreacion(lc.getFechaCreacion());
        dto.setClaimed(lc.isClaimed());
        dto.setClaimedByEmail(lc.getClaimedByEmail());
        dto.setClaimedAt(lc.getClaimedAt());
        return dto;
    }
}
