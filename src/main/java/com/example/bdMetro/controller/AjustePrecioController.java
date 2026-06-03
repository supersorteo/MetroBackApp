package com.example.bdMetro.controller;

import com.example.bdMetro.entity.AjustePrecioHistorial;
import com.example.bdMetro.services.AjustePrecioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ajuste-precio")
public class AjustePrecioController {

    @Autowired
    private AjustePrecioService service;

    @GetMapping("/{userCode}/{pais}")
    public ResponseEntity<Map<String, Object>> getFactor(
            @PathVariable String userCode,
            @PathVariable String pais) {
        return ResponseEntity.ok(service.getFactor(userCode, pais));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> aplicarAjuste(@RequestBody Map<String, Object> body) {
        String userCode  = (String) body.get("userCode");
        String pais      = (String) body.get("pais");
        String tipo      = (String) body.get("tipo");
        BigDecimal porcentaje = body.get("porcentaje") != null
                ? new BigDecimal(body.get("porcentaje").toString())
                : null;

        return ResponseEntity.ok(service.aplicarAjuste(userCode, pais, tipo, porcentaje));
    }

    @GetMapping("/{userCode}/{pais}/historial")
    public ResponseEntity<List<AjustePrecioHistorial>> getHistorial(
            @PathVariable String userCode,
            @PathVariable String pais) {
        return ResponseEntity.ok(service.getHistorial(userCode, pais));
    }
}
