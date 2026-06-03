package com.example.bdMetro.services;

import com.example.bdMetro.entity.AjustePrecioHistorial;
import com.example.bdMetro.entity.AjustePrecioLista;
import com.example.bdMetro.repository.AjustePrecioHistorialRepository;
import com.example.bdMetro.repository.AjustePrecioListaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AjustePrecioService {

    @Autowired
    private AjustePrecioListaRepository listaRepo;

    @Autowired
    private AjustePrecioHistorialRepository historialRepo;

    public Map<String, Object> getFactor(String userCode, String pais) {
        AjustePrecioLista ajuste = listaRepo
                .findByUserCodeAndPaisIgnoreCase(userCode, pais)
                .orElse(null);

        BigDecimal factor = ajuste != null ? ajuste.getFactor() : BigDecimal.ONE;
        LocalDateTime updatedAt = ajuste != null ? ajuste.getUpdatedAt() : null;

        return Map.of("factor", factor, "updatedAt", updatedAt != null ? updatedAt.toString() : "");
    }

    @Transactional
    public Map<String, Object> aplicarAjuste(String userCode, String pais, String tipo, BigDecimal porcentaje) {
        AjustePrecioLista ajuste = listaRepo
                .findByUserCodeAndPaisIgnoreCase(userCode, pais)
                .orElseGet(() -> {
                    AjustePrecioLista nuevo = new AjustePrecioLista();
                    nuevo.setUserCode(userCode);
                    nuevo.setPais(pais);
                    nuevo.setFactor(BigDecimal.ONE);
                    return nuevo;
                });

        BigDecimal nuevoFactor;

        if ("reestablecer".equals(tipo)) {
            nuevoFactor = BigDecimal.ONE;
        } else if ("subir".equals(tipo)) {
            BigDecimal multiplicador = BigDecimal.ONE.add(porcentaje.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP));
            nuevoFactor = ajuste.getFactor().multiply(multiplicador).setScale(6, RoundingMode.HALF_UP);
        } else { // bajar
            BigDecimal multiplicador = BigDecimal.ONE.subtract(porcentaje.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP));
            nuevoFactor = ajuste.getFactor().multiply(multiplicador).setScale(6, RoundingMode.HALF_UP);
        }

        ajuste.setFactor(nuevoFactor);
        ajuste.setUpdatedAt(LocalDateTime.now());
        listaRepo.save(ajuste);

        AjustePrecioHistorial log = new AjustePrecioHistorial();
        log.setUserCode(userCode);
        log.setPais(pais);
        log.setTipo(tipo);
        log.setPorcentaje(porcentaje);
        log.setFactorResultado(nuevoFactor);
        log.setCreatedAt(LocalDateTime.now());
        historialRepo.save(log);

        return Map.of("factor", nuevoFactor, "updatedAt", ajuste.getUpdatedAt().toString());
    }

    public List<AjustePrecioHistorial> getHistorial(String userCode, String pais) {
        return historialRepo.findByUserCodeAndPaisIgnoreCaseOrderByCreatedAtDesc(userCode, pais);
    }
}
