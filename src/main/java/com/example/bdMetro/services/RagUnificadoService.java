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
public class RagUnificadoService {

    private final String geminiApiKey;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String corpusUnificado;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=";

    private static final String INSTRUCCIONES = """

            INSTRUCCIONES:
            - Sos el asistente inteligente de MetroApp.
            - Respondé en español argentino, de forma amigable y concisa.
            - Tu conocimiento cubre DOS áreas:
              1. CÁLCULO DE MATERIALES: Si el usuario pregunta sobre materiales de construcción y da una medida (m², m³, ml), \
            calculá las cantidades usando el catálogo de tareas. Mostrá los resultados en lista clara. \
            Si el cemento aparece en KG, también indicá cuántas bolsas de 50 KG son necesarias (redondeando hacia arriba). \
            No inventés materiales ni cantidades que no estén en el catálogo.
              2. AYUDA DE LA APP: Si el usuario pregunta sobre cómo usar MetroApp, planes VIP, límites, funciones o \
            cualquier duda sobre la aplicación, respondé usando la documentación de ayuda.
            - Si la pregunta mezcla ambos temas, respondé todo en una sola respuesta.
            - Si falta la medida para calcular materiales, pedila.
            - Si la pregunta no corresponde a ninguno de los dos temas, decilo amablemente.
            - Si pregunta cómo activar VIP o conseguir un código, indicale que debe contactar al administrador de MetroApp.
            - Respondé directamente sin introducción larga.
            """;

    public RagUnificadoService(@Value("${gemini.api-key}") String geminiApiKey) {
        this.geminiApiKey = geminiApiKey;
        this.corpusUnificado = cargarCorpus();
    }

    public String consultar(String pregunta) {
        String systemPrompt = corpusUnificado + INSTRUCCIONES;

        Map<String, Object> body = Map.of(
                "system_instruction", Map.of(
                        "parts", List.of(Map.of("text", systemPrompt))
                ),
                "contents", List.of(
                        Map.of("role", "user",
                                "parts", List.of(Map.of("text", pregunta)))
                ),
                "generationConfig", Map.of("maxOutputTokens", 2048)
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

    private String cargarCorpus() {
        try {
            String tareas = new String(
                    new ClassPathResource("catalogo-tareas.txt").getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8);
            String ayuda = new String(
                    new ClassPathResource("catalogo-ayuda.txt").getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8);
            return tareas + "\n\n" + ayuda;
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar el corpus del asistente.", e);
        }
    }
}
