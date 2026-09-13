package com.example.bdMetro.services;

// ── OPCIÓN GEMINI (activa) ─────────────────────────────────────────────────
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

// ── OPCIÓN CLAUDE (comentada — reactivar si se agrega saldo en console.anthropic.com) ──
// import com.anthropic.client.AnthropicClient;
// import com.anthropic.client.okhttp.AnthropicOkHttpClient;
// import com.anthropic.models.messages.CacheControlEphemeral;
// import com.anthropic.models.messages.Message;
// import com.anthropic.models.messages.MessageCreateParams;
// import com.anthropic.models.messages.TextBlockParam;

@Service
public class RagCalculadoraService {

    // ── GEMINI ──
    private final String geminiApiKey;
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=";

    // ── CLAUDE (comentado) ──
    // private final AnthropicClient client;

    private final String catalogoTexto;

    private static final String INSTRUCCIONES = """

            INSTRUCCIONES:
            - Respondé en español argentino.
            - Si el usuario da una medida (m², m³, ml), calculá la cantidad de cada material \
            multiplicando los valores por unidad del catálogo.
            - Mostrá los resultados en una lista clara: nombre del material y cantidad calculada.
            - Si el cemento aparece en KG, también indicá cuántas bolsas de 50 KG son necesarias \
            (redondeando hacia arriba).
            - Si la pregunta no corresponde a ninguna tarea del catálogo, decilo claramente.
            - Si falta información (por ejemplo el usuario no dio la medida), pedila.
            - No inventés materiales ni cantidades que no estén en el catálogo.
            - Sé conciso: respondé directamente sin introducción larga.
            """;

    public RagCalculadoraService(
            @Value("${gemini.api-key}") String geminiApiKey
            // Para reactivar Claude: agregar @Value("${anthropic.api-key}") String anthropicApiKey
    ) {
        this.geminiApiKey = geminiApiKey;
        // ── CLAUDE (comentado) ──
        // this.client = AnthropicOkHttpClient.builder()
        //         .apiKey(anthropicApiKey)
        //         .putHeader("anthropic-beta", "prompt-caching-2024-07-31")
        //         .build();
        this.catalogoTexto = cargarCatalogo();
    }

    public String consultar(String pregunta) {
        String systemPrompt = catalogoTexto + INSTRUCCIONES;

        // ── GEMINI ──
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

        // ── CLAUDE (comentado) ──
        // String systemPromptClaude = catalogoTexto + INSTRUCCIONES;
        // MessageCreateParams params = MessageCreateParams.builder()
        //         .model("claude-haiku-4-5-20251001")
        //         .maxTokens(1024L)
        //         .systemOfTextBlockParams(List.of(
        //                 TextBlockParam.builder()
        //                         .text(systemPromptClaude)
        //                         .cacheControl(CacheControlEphemeral.builder().build())
        //                         .build()))
        //         .addUserMessage(pregunta)
        //         .build();
        // Message responseClaude = client.messages().create(params);
        // return responseClaude.content().stream()
        //         .flatMap(block -> block.text().stream())
        //         .map(com.anthropic.models.messages.TextBlock::text)
        //         .findFirst()
        //         .orElse("No se pudo obtener una respuesta.");
    }

    private String cargarCatalogo() {
        try {
            var resource = new ClassPathResource("catalogo-tareas.txt");
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar catalogo-tareas.txt", e);
        }
    }
}
