package com.example.bdMetro.services;

import com.example.bdMetro.entity.AjustePrecioPersonalizada;
import com.example.bdMetro.entity.AjustePrecioPersonalizadaHistorial;
import com.example.bdMetro.repository.AjustePrecioPersonalizadaHistorialRepository;
import com.example.bdMetro.repository.AjustePrecioPersonalizadaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AjustePrecioPersonalizadaService {

    @Autowired
    private AjustePrecioPersonalizadaRepository repo;

    @Autowired
    private AjustePrecioPersonalizadaHistorialRepository historialRepo;

    public Map<String, Object> getFactor(String userCode) {
        AjustePrecioPersonalizada ajuste = repo.findByUserCode(userCode).orElse(null);
        BigDecimal factor = ajuste != null ? ajuste.getFactor() : BigDecimal.ONE;
        LocalDateTime updatedAt = ajuste != null ? ajuste.getUpdatedAt() : null;
        return Map.of("factor", factor, "updatedAt", updatedAt != null ? updatedAt.toString() : "");
    }

    @Transactional
    public Map<String, Object> aplicarAjuste(String userCode, String tipo, BigDecimal porcentaje) {
        AjustePrecioPersonalizada ajuste = repo.findByUserCode(userCode)
                .orElseGet(() -> {
                    AjustePrecioPersonalizada nuevo = new AjustePrecioPersonalizada();
                    nuevo.setUserCode(userCode);
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
        repo.save(ajuste);

        AjustePrecioPersonalizadaHistorial log = new AjustePrecioPersonalizadaHistorial();
        log.setUserCode(userCode);
        log.setTipo(tipo);
        log.setPorcentaje(porcentaje);
        log.setFactorResultado(nuevoFactor);
        log.setCreatedAt(LocalDateTime.now());
        historialRepo.save(log);

        return Map.of("factor", nuevoFactor, "updatedAt", ajuste.getUpdatedAt().toString());
    }

    public List<AjustePrecioPersonalizadaHistorial> getHistorial(String userCode) {
        return historialRepo.findByUserCodeOrderByCreatedAtDesc(userCode);
    }
}
