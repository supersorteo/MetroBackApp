package com.example.bdMetro.payments.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.example.bdMetro.payments.config.MercadoPagoProperties;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class MercadoPagoClient {

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
            return restClient.post()
                    .uri("/checkout/preferences")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getAccessToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientResponseException exception) {
            throw new IllegalStateException("Mercado Pago rechazo la creacion de la preferencia: " + exception.getResponseBodyAsString(), exception);
        }
    }

    public JsonNode getPayment(String paymentId) {
        try {
            return restClient.get()
                    .uri("/v1/payments/{paymentId}", paymentId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getAccessToken())
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientResponseException exception) {
            throw new IllegalStateException("No se pudo consultar el pago en Mercado Pago: " + exception.getResponseBodyAsString(), exception);
        }
    }
}
