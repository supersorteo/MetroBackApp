package com.example.bdMetro.controller;

import com.example.bdMetro.dto.CalculoMaterialRequest;
import com.example.bdMetro.dto.CalculoMaterialResponse;
import com.example.bdMetro.dto.TareaCalculadaResumenResponse;
import com.example.bdMetro.services.CalculoMaterialService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/calculadora-materiales")
public class CalculoMaterialController {

    private final CalculoMaterialService calculoMaterialService;

    public CalculoMaterialController(CalculoMaterialService calculoMaterialService) {
        this.calculoMaterialService = calculoMaterialService;
    }

    @PostMapping
    public ResponseEntity<CalculoMaterialResponse> guardar(@RequestBody CalculoMaterialRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(calculoMaterialService.guardar(request));
    }

    @GetMapping("/history/{userCode}")
    public List<CalculoMaterialResponse> obtenerHistorial(
            @PathVariable String userCode,
            @RequestParam(defaultValue = "10") int limit) {
        return calculoMaterialService.obtenerHistorial(userCode, limit);
    }

    @GetMapping("/latest-tasks/{userCode}")
    public List<TareaCalculadaResumenResponse> obtenerUltimasTareas(
            @PathVariable String userCode,
            @RequestParam(defaultValue = "5") int limit) {
        return calculoMaterialService.obtenerUltimasTareas(userCode, limit);
    }

    @DeleteMapping("/{calculoId}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long calculoId,
            @RequestParam String userCode) {
        calculoMaterialService.eliminar(calculoId, userCode);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }
}
