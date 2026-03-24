package com.example.bdMetro.payments.service;

import java.time.OffsetDateTime;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.example.bdMetro.payments.config.DlocalProperties;
import com.example.bdMetro.payments.dto.DlocalCreatePaymentRequest;
import com.example.bdMetro.payments.dto.DlocalPaymentResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DlocalClient {

    private final RestClient restClient;
    private final DlocalProperties properties;
    private final DlocalSignatureService signatureService;
    private final ObjectMapper objectMapper;

    public DlocalClient(
            @Qualifier("dlocalRestClient") RestClient dlocalRestClient,
            DlocalProperties properties,
            DlocalSignatureService signatureService,
            ObjectMapper objectMapper
    ) {
        this.restClient = dlocalRestClient;
        this.properties = properties;
        this.signatureService = signatureService;
        this.objectMapper = objectMapper;
    }

    public DlocalPaymentResponse createPayment(DlocalCreatePaymentRequest request) {
        String body = toJson(request);
        String xDate = OffsetDateTime.now().toString();

        try {
            return restClient.post()
                    .uri("/payments")
                    .headers(headers -> applyHeaders(headers, xDate, body))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(DlocalPaymentResponse.class);
        } catch (RestClientResponseException exception) {
            throw new IllegalStateException("dLocal rechazó la creación del pago: " + exception.getResponseBodyAsString(), exception);
        }
    }

    public DlocalPaymentResponse getPayment(String paymentId) {
        String xDate = OffsetDateTime.now().toString();

        try {
            return restClient.get()
                    .uri("/payments/{paymentId}", paymentId)
                    .headers(headers -> applyHeaders(headers, xDate, ""))
                    .retrieve()
                    .body(DlocalPaymentResponse.class);
        } catch (RestClientResponseException exception) {
            throw new IllegalStateException("No se pudo consultar el pago en dLocal: " + exception.getResponseBodyAsString(), exception);
        }
    }

    private void applyHeaders(HttpHeaders headers, String xDate, String body) {
        headers.set("X-Date", xDate);
        headers.set("X-Login", properties.getXLogin());
        headers.set("X-Trans-Key", properties.getXTransKey());
        headers.set("X-Version", properties.getApiVersion());
        headers.set("User-Agent", properties.getUserAgent());
        headers.set(HttpHeaders.AUTHORIZATION, signatureService.buildAuthorizationHeader(xDate, body));
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("No se pudo serializar la petición hacia dLocal", exception);
        }
    }
}
