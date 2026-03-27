package com.example.bdMetro.payments.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.example.bdMetro.payments.config.MercadoPagoProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MercadoPagoClient {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoClient.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final RestClient restClient;
    private final MercadoPagoProperties properties;

    public MercadoPagoClient(
            @Qualifier("mercadoPagoRestClient") RestClient restClient,
            MercadoPagoProperties properties
    ) {
        this.restClient = restClient;
        this.properties = properties;
    }

    public JsonNode createPreference(Map<String, Object> payload) {
        try {
            log.info("[MP] >>> POST /checkout/preferences | payload: {}", toJson(payload));
            JsonNode response = restClient.post()
                    .uri("/checkout/preferences")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getAccessToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(JsonNode.class);
            log.info("[MP] <<< Preferencia OK | id={} | init_point={} | sandbox_init_point={}",
                    text(response, "id"), text(response, "init_point"), text(response, "sandbox_init_point"));
            return response;
        } catch (RestClientResponseException exception) {
            log.error("[MP] <<< ERROR creando preferencia | status={} | body={}",
                    exception.getStatusCode(), exception.getResponseBodyAsString());
            throw new IllegalStateException("Mercado Pago rechazo la creacion de la preferencia: " + exception.getResponseBodyAsString(), exception);
        }
    }

    public JsonNode getPayment(String paymentId) {
        try {
            log.info("[MP] >>> GET /v1/payments/{}", paymentId);
            JsonNode response = restClient.get()
                    .uri("/v1/payments/{paymentId}", paymentId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getAccessToken())
                    .retrieve()
                    .body(JsonNode.class);
            log.info("[MP] <<< Pago consultado OK | id={} | status={} | status_detail={}",
                    text(response, "id"), text(response, "status"), text(response, "status_detail"));
            return response;
        } catch (RestClientResponseException exception) {
            log.error("[MP] <<< ERROR consultando pago {} | status={} | body={}",
                    paymentId, exception.getStatusCode(), exception.getResponseBodyAsString());
            throw new IllegalStateException("No se pudo consultar el pago en Mercado Pago: " + exception.getResponseBodyAsString(), exception);
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
