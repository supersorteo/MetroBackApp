package com.example.bdMetro.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RagUnificadoService {

    private static final Logger log = LoggerFactory.getLogger(RagUnificadoService.class);

    private final String geminiApiKey;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String corpusUnificado;
    private final ArchiToolsService archiToolsService;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=";

    private static final String INSTRUCCIONES = """
 
            IDENTIDAD:
            - Tu nombre es Archi. Sos el asistente inteligente de MetroApp.
            - Tenés personalidad de profesional de la construcción: directo, confiable y con buen humor ocasional.
            - Respondé en español rioplatense (Argentina/Uruguay), de forma amigable y concisa.
            - Cuando te pregunten cómo te llamás, respondé que sos Archi, el asistente de MetroApp.

            INSTRUCCIONES:
            - Tu conocimiento cubre TRES áreas:
              1. CÁLCULO DE MATERIALES: Si el usuario pregunta sobre materiales de construcción y da una medida (m², m³, ml), \
            calculá las cantidades usando el catálogo de tareas. Mostrá los resultados en lista clara. \
            Si el cemento aparece en KG, indicá también cuántas bolsas de 50 KG son necesarias (redondeando hacia arriba).
              2. AYUDA DE LA APP: Si el usuario pregunta sobre cómo usar MetroApp, planes VIP, límites o funciones, \
            respondé usando la documentación de ayuda.
              3. DATOS EN TIEMPO REAL: Si el usuario pregunta por sus clientes, presupuestos o historial de cálculos, \
            usá las herramientas disponibles para consultar sus datos.
            - Ejemplos de cuándo usar herramientas:
              • "¿cuáles son mis clientes?" o "mostrá mis clientes" → consultarClientes
              • "¿qué presupuestos tengo?" o "mostrame mis presupuestos" → consultarPresupuestos
              • "¿cuál fue mi último cálculo?" o "mi historial" → consultarHistorialCalculos
              • "agregá un cliente", "creá el cliente Juan con email juan@mail.com" → crearCliente
            - Para crearCliente: necesitás nombre y email obligatoriamente. Si el usuario pide crear un cliente pero no dio alguno de esos datos, preguntale antes de llamar a la herramienta.
            - Si falta la medida para calcular materiales, pedila de forma breve.
            - Si la pregunta no corresponde a ningún tema, decilo con humor y redirigí al usuario.
            - Respondé directamente. Nunca empieces con "¡Claro!" o frases genéricas.
            """;

    private static final List<Map<String, Object>> TOOLS = List.of(
            Map.of("functionDeclarations", List.of(
                    Map.of(
                            "name", "consultarClientes",
                            "description", "Consulta y devuelve la lista de clientes registrados del usuario en MetroApp.",
                            "parameters", Map.of("type", "OBJECT", "properties", Map.of())
                    ),
                    Map.of(
                            "name", "consultarPresupuestos",
                            "description", "Consulta y devuelve los presupuestos creados por el usuario en MetroApp.",
                            "parameters", Map.of("type", "OBJECT", "properties", Map.of())
                    ),
                    Map.of(
                            "name", "consultarHistorialCalculos",
                            "description", "Consulta y devuelve el historial reciente de cálculos de materiales del usuario.",
                            "parameters", Map.of("type", "OBJECT", "properties", Map.of())
                    ),
                    Map.of(
                            "name", "crearCliente",
                            "description", "Crea un nuevo cliente en MetroApp para el usuario. Requiere nombre y email.",
                            "parameters", Map.of(
                                    "type", "OBJECT",
                                    "properties", Map.of(
                                            "nombre",   Map.of("type", "STRING", "description", "Nombre completo del cliente"),
                                            "email",    Map.of("type", "STRING", "description", "Email del cliente"),
                                            "telefono", Map.of("type", "STRING", "description", "Teléfono de contacto (opcional)")
                                    ),
                                    "required", List.of("nombre", "email")
                            )
                    )
            ))
    );

    public RagUnificadoService(@Value("${gemini.api-key:}") String geminiApiKey,
                                ArchiToolsService archiToolsService) {
        this.geminiApiKey = geminiApiKey;
        this.archiToolsService = archiToolsService;
        this.corpusUnificado = cargarCorpus();
    }

    public String consultar(String pregunta, String userCode) {
        String systemPrompt = corpusUnificado + INSTRUCCIONES;

        List<Map<String, Object>> contents = List.of(
                Map.of("role", "user", "parts", List.of(Map.of("text", pregunta)))
        );

        Map<String, Object> response = callGemini(buildBody(systemPrompt, contents));
        if (response == null) return "No se pudo obtener una respuesta.";

        try {
            var result = extractResult(response);
            if (result.functionCall != null) {
                log.info("[Archi] Tool call detectado: {}", result.functionCall.name());
                String toolResult = archiToolsService.execute(result.functionCall.name(), result.functionCall.args(), userCode);
                log.info("[Archi] Tool result: {}", toolResult);

                // Usamos el content COMPLETO del modelo para preservar thought_signature
                // que gemini-thinking requiere en el segundo turno
                List<Map<String, Object>> contents2 = List.of(
                        Map.of("role", "user", "parts", List.of(Map.of("text", pregunta))),
                        result.modelContent(),
                        Map.of("role", "user", "parts", List.of(Map.of(
                                "functionResponse", Map.of(
                                        "name", result.functionCall.name(),
                                        "response", Map.of("output", toolResult)
                                )
                        )))
                );

                Map<String, Object> response2 = callGemini(buildBody(systemPrompt, contents2));
                if (response2 == null) return toolResult;

                var result2 = extractResult(response2);
                return result2.text != null ? result2.text : toolResult;
            }
            return result.text != null ? result.text : "No se pudo obtener una respuesta.";
        } catch (Exception e) {
            log.error("[Archi] Error procesando respuesta de Gemini: {}", e.getMessage(), e);
            return "No se pudo obtener una respuesta.";
        }
    }

    private Map<String, Object> buildBody(String systemPrompt, List<Map<String, Object>> contents) {
        return Map.of(
                "system_instruction", Map.of("parts", List.of(Map.of("text", systemPrompt))),
                "contents", contents,
                "tools", TOOLS,
                "generationConfig", Map.of("maxOutputTokens", 2048)
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> callGemini(Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        int delayMs = 2000;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                return restTemplate.postForObject(GEMINI_URL + geminiApiKey, entity, Map.class);
            } catch (HttpServerErrorException.ServiceUnavailable e) {
                if (attempt < 3) {
                    log.warn("[Archi] Gemini 503, reintento {}/3 en {}ms...", attempt, delayMs);
                    try { Thread.sleep(delayMs); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    delayMs *= 2;
                } else {
                    log.error("[Archi] Gemini HTTP {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
                    throw e;
                }
            } catch (HttpStatusCodeException e) {
                log.error("[Archi] Gemini HTTP {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
                throw e;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private GeminiResult extractResult(Map<String, Object> response) {
        var candidates = (List<Map<String, Object>>) response.get("candidates");
        var content = (Map<String, Object>) candidates.get(0).get("content");
        var parts = (List<Map<String, Object>>) content.get("parts");

        Optional<Map<String, Object>> fcPart = parts.stream()
                .filter(p -> p.containsKey("functionCall"))
                .findFirst();

        if (fcPart.isPresent()) {
            var fc = (Map<String, Object>) fcPart.get().get("functionCall");
            String name = (String) fc.get("name");
            Map<String, Object> args = fc.containsKey("args")
                    ? (Map<String, Object>) fc.get("args")
                    : Map.of();
            // content completo (con thought_signature intacto) para el segundo turno
            return new GeminiResult(null, new FunctionCallInfo(name, args), content);
        }

        String text = parts.stream()
                .filter(p -> p.containsKey("text"))
                .map(p -> (String) p.get("text"))
                .findFirst()
                .orElse(null);
        return new GeminiResult(text, null, content);
    }

    private record FunctionCallInfo(String name, Map<String, Object> args) {}
    private record GeminiResult(String text, FunctionCallInfo functionCall, Map<String, Object> modelContent) {}

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
