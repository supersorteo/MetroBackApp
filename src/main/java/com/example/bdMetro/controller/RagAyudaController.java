package com.example.bdMetro.controller;

import com.example.bdMetro.dto.RagAyudaRequest;
import com.example.bdMetro.dto.RagConsultaResponse;
import com.example.bdMetro.services.RagAyudaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rag")
public class RagAyudaController {

    private static final Logger log = LoggerFactory.getLogger(RagAyudaController.class);

    @Autowired
    private RagAyudaService ragAyudaService;

    @PostMapping("/ayuda/consulta")
    public ResponseEntity<RagConsultaResponse> consultar(@RequestBody RagAyudaRequest request) {
        if (request.getPregunta() == null || request.getPregunta().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new RagConsultaResponse("La pregunta no puede estar vacía.", true));
        }
        try {
            String respuesta = ragAyudaService.consultar(request.getPregunta());
            return ResponseEntity.ok(new RagConsultaResponse(respuesta, false));
        } catch (Exception e) {
            log.error("Error en RAG ayuda: {}", e.getMessage(), e);
            return ResponseEntity.ok(
                    new RagConsultaResponse("Error al procesar tu consulta. Intenta nuevamente.", true));
        }
    }
}
