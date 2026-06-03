package com.example.bdMetro.services;

import com.example.bdMetro.entity.AjustePrecioAdmin;
import com.example.bdMetro.entity.AjustePrecioAdminHistorial;
import com.example.bdMetro.repository.AjustePrecioAdminHistorialRepository;
import com.example.bdMetro.repository.AjustePrecioAdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AjustePrecioAdminService {

    @Autowired
    private AjustePrecioAdminRepository adminRepo;

    @Autowired
    private AjustePrecioAdminHistorialRepository historialRepo;

    public Map<String, Object> getFactor(String pais) {
        AjustePrecioAdmin ajuste = adminRepo.findByPaisIgnoreCase(pais).orElse(null);
        BigDecimal factor = ajuste != null ? ajuste.getFactor() : BigDecimal.ONE;
        LocalDateTime updatedAt = ajuste != null ? ajuste.getUpdatedAt() : null;
        return Map.of("factor", factor, "updatedAt", updatedAt != null ? updatedAt.toString() : "");
    }

    @Transactional
    public Map<String, Object> aplicarAjuste(String pais, String tipo, BigDecimal porcentaje) {
        AjustePrecioAdmin ajuste = adminRepo.findByPaisIgnoreCase(pais)
                .orElseGet(() -> {
                    AjustePrecioAdmin nuevo = new AjustePrecioAdmin();
                    nuevo.setPais(pais);
                    nuevo.setFactor(BigDecimal.ONE);
                    return nuevo;
                });

        BigDecimal nuevoFactor;

        if ("reestablecer".equals(tipo)) {
            nuevoFactor = BigDecimal.ONE;
        } else if ("subir".equals(tipo)) {
            BigDecimal mult = BigDecimal.ONE.add(porcentaje.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP));
            nuevoFactor = ajuste.getFactor().multiply(mult).setScale(6, RoundingMode.HALF_UP);
        } else {
            BigDecimal mult = BigDecimal.ONE.subtract(porcentaje.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP));
            nuevoFactor = ajuste.getFactor().multiply(mult).setScale(6, RoundingMode.HALF_UP);
        }

        ajuste.setFactor(nuevoFactor);
        ajuste.setUpdatedAt(LocalDateTime.now());
        adminRepo.save(ajuste);

        AjustePrecioAdminHistorial log = new AjustePrecioAdminHistorial();
        log.setPais(pais);
        log.setTipo(tipo);
        log.setPorcentaje(porcentaje);
        log.setFactorResultado(nuevoFactor);
        log.setCreatedAt(LocalDateTime.now());
        historialRepo.save(log);

        return Map.of("factor", nuevoFactor, "updatedAt", ajuste.getUpdatedAt().toString());
    }

    public List<AjustePrecioAdminHistorial> getHistorial(String pais) {
        return historialRepo.findByPaisIgnoreCaseOrderByCreatedAtDesc(pais);
    }
}
