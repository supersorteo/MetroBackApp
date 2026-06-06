package com.example.bdMetro.controller;

import com.example.bdMetro.entity.AjustePrecioPersonalizadaHistorial;
import com.example.bdMetro.services.AjustePrecioPersonalizadaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ajuste-precio-personalizada")
public class AjustePrecioPersonalizadaController {

    @Autowired
    private AjustePrecioPersonalizadaService service;

    @GetMapping("/{userCode}")
    public ResponseEntity<Map<String, Object>> getFactor(@PathVariable String userCode) {
        return ResponseEntity.ok(service.getFactor(userCode));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> aplicarAjuste(@RequestBody Map<String, Object> body) {
        String userCode = (String) body.get("userCode");
        String tipo     = (String) body.get("tipo");
        BigDecimal porcentaje = body.get("porcentaje") != null
                ? new BigDecimal(body.get("porcentaje").toString())
                : null;
        return ResponseEntity.ok(service.aplicarAjuste(userCode, tipo, porcentaje));
    }

    @GetMapping("/{userCode}/historial")
    public ResponseEntity<List<AjustePrecioPersonalizadaHistorial>> getHistorial(@PathVariable String userCode) {
        return ResponseEntity.ok(service.getHistorial(userCode));
    }
}
