package com.example.bdMetro.controller;

import com.example.bdMetro.entity.AjustePrecioAdminHistorial;
import com.example.bdMetro.services.AjustePrecioAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ajuste-precio-admin")
public class AjustePrecioAdminController {

    @Autowired
    private AjustePrecioAdminService service;

    @GetMapping("/{pais}")
    public ResponseEntity<Map<String, Object>> getFactor(@PathVariable String pais) {
        return ResponseEntity.ok(service.getFactor(pais));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> aplicarAjuste(@RequestBody Map<String, Object> body) {
        String pais = (String) body.get("pais");
        String tipo = (String) body.get("tipo");
        BigDecimal porcentaje = body.get("porcentaje") != null
                ? new BigDecimal(body.get("porcentaje").toString())
                : null;
        return ResponseEntity.ok(service.aplicarAjuste(pais, tipo, porcentaje));
    }

    @GetMapping("/{pais}/historial")
    public ResponseEntity<List<AjustePrecioAdminHistorial>> getHistorial(@PathVariable String pais) {
        return ResponseEntity.ok(service.getHistorial(pais));
    }
}
