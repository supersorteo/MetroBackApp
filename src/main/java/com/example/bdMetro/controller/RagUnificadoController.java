package com.example.bdMetro.controller;

import com.example.bdMetro.dto.RagUnificadoRequest;
import com.example.bdMetro.services.RagAyudaService;
import com.example.bdMetro.services.RagUnificadoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/rag")
public class RagUnificadoController {

    private static final Logger log = LoggerFactory.getLogger(RagUnificadoController.class);

    private final RagUnificadoService ragUnificadoService;

    public RagUnificadoController(RagUnificadoService ragUnificadoService) {
        this.ragUnificadoService = ragUnificadoService;
    }

    @PostMapping("/consulta")
    public ResponseEntity<Map<String, Object>> consultar(@RequestBody RagUnificadoRequest request) {
        try {
            String respuesta = ragUnificadoService.consultar(request.getPregunta(), request.getUserCode());
            return ResponseEntity.ok(Map.of("respuesta", respuesta, "error", false));
        } catch (Exception e) {
            log.error("Error en RAG unificado: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of(
                "respuesta", "Error al procesar tu consulta. Intenta nuevamente.",
                "error", true
            ));
        }
    }
}
