package com.example.bdMetro.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class RagAyudaService {

    private final String geminiApiKey;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String documentacion;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=";

    private static final String INSTRUCCIONES = """

            INSTRUCCIONES:
            - Sos el asistente de ayuda de MetroApp.
            - Respondé en español, de forma amigable y concisa.
            - Usá solo la información de la documentación provista. No inventes funciones que no existan.
            - Si la pregunta no está relacionada con MetroApp, decí amablemente que solo podés ayudar con dudas sobre la app.
            - Si el usuario pregunta cómo activar VIP o conseguir un código, indicale que debe contactar al administrador de MetroApp.
            - Respondé directamente sin introducción larga.
            """;

    public RagAyudaService(@Value("${gemini.api-key}") String geminiApiKey) {
        this.geminiApiKey = geminiApiKey;
        this.documentacion = cargarDocumentacion();
    }

    public String consultar(String pregunta) {
        String systemPrompt = documentacion + INSTRUCCIONES;

        Map<String, Object> body = Map.of(
                "system_instruction", Map.of(
                        "parts", List.of(Map.of("text", systemPrompt))
                ),
                "contents", List.of(
                        Map.of("role", "user",
                                "parts", List.of(Map.of("text", pregunta)))
                ),
                "generationConfig", Map.of("maxOutputTokens", 1024)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(
                GEMINI_URL + geminiApiKey, request, Map.class);

        if (response == null) return "No se pudo obtener una respuesta.";

        try {
            @SuppressWarnings("unchecked")
            var candidates = (List<Map<String, Object>>) response.get("candidates");
            @SuppressWarnings("unchecked")
            var content = (Map<String, Object>) candidates.get(0).get("content");
            @SuppressWarnings("unchecked")
            var parts = (List<Map<String, Object>>) content.get("parts");
            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            return "No se pudo obtener una respuesta.";
        }
    }

    private String cargarDocumentacion() {
        try {
            var resource = new ClassPathResource("catalogo-ayuda.txt");
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar catalogo-ayuda.txt", e);
        }
    }
}
