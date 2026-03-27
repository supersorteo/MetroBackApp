package com.example.bdMetro.payments.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.example.bdMetro.payments.config.PayPalProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PayPalClient {

    private static final Logger log = LoggerFactory.getLogger(PayPalClient.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final RestClient restClient;
    private final PayPalProperties properties;

    public PayPalClient(
            @Qualifier("payPalRestClient") RestClient restClient,
            PayPalProperties properties
    ) {
        this.restClient = restClient;
        this.properties = properties;
    }

    public String getAccessToken() {
        try {
            log.info("[PP] >>> POST /v1/oauth2/token");
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("grant_type", "client_credentials");

            JsonNode response = restClient.post()
                    .uri("/v1/oauth2/token")
                    .headers(h -> h.setBasicAuth(properties.getClientId(), properties.getClientSecret()))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(JsonNode.class);

            String token = text(response, "access_token");
            log.info("[PP] <<< Token obtenido OK");
            return token;
        } catch (RestClientResponseException e) {
            log.error("[PP] <<< ERROR obteniendo token | status={} | body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new IllegalStateException("No se pudo autenticar con PayPal: " + e.getResponseBodyAsString(), e);
        }
    }

    public JsonNode createOrder(Map<String, Object> payload, String accessToken) {
        try {
            log.info("[PP] >>> POST /v2/checkout/orders | payload: {}", toJson(payload));
            JsonNode response = restClient.post()
                    .uri("/v2/checkout/orders")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .header("PayPal-Request-Id", java.util.UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(JsonNode.class);
            log.info("[PP] <<< Orden creada OK | id={} | status={}", text(response, "id"), text(response, "status"));
            return response;
        } catch (RestClientResponseException e) {
            log.error("[PP] <<< ERROR creando orden | status={} | body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new IllegalStateException("PayPal rechazo la creacion de la orden: " + e.getResponseBodyAsString(), e);
        }
    }

    public JsonNode captureOrder(String orderId, String accessToken) {
        try {
            log.info("[PP] >>> POST /v2/checkout/orders/{}/capture", orderId);
            JsonNode response = restClient.post()
                    .uri("/v2/checkout/orders/{orderId}/capture", orderId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .header("PayPal-Request-Id", java.util.UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of())
                    .retrieve()
                    .body(JsonNode.class);
            log.info("[PP] <<< Captura OK | id={} | status={}", text(response, "id"), text(response, "status"));
            return response;
        } catch (RestClientResponseException e) {
            log.error("[PP] <<< ERROR capturando orden {} | status={} | body={}", orderId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new IllegalStateException("PayPal rechazo la captura del pago: " + e.getResponseBodyAsString(), e);
        }
    }

    public JsonNode getOrder(String orderId, String accessToken) {
        try {
            log.info("[PP] >>> GET /v2/checkout/orders/{}", orderId);
            JsonNode response = restClient.get()
                    .uri("/v2/checkout/orders/{orderId}", orderId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(JsonNode.class);
            log.info("[PP] <<< Orden consultada OK | id={} | status={}", text(response, "id"), text(response, "status"));
            return response;
        } catch (RestClientResponseException e) {
            log.error("[PP] <<< ERROR consultando orden {} | status={} | body={}", orderId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new IllegalStateException("No se pudo consultar la orden de PayPal: " + e.getResponseBodyAsString(), e);
        }
    }

    private String text(JsonNode node, String field) {
        if (node == null) return null;
        JsonNode child = node.get(field);
        return child == null || child.isNull() ? null : child.asText();
    }

    private String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }
}
