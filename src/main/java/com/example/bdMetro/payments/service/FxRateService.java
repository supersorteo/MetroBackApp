package com.example.bdMetro.payments.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.example.bdMetro.payments.config.FxRateProperties;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class FxRateService {

    private final RestClient fxRestClient;
    private final FxRateProperties properties;

    private final Map<String, CachedRate> cache = new ConcurrentHashMap<>();

    public FxRateService(@Qualifier("fxRestClient") RestClient fxRestClient, FxRateProperties properties) {
        this.fxRestClient = fxRestClient;
        this.properties = properties;
    }

    public BigDecimal getRate(String currencyCode) {
        String normalizedCurrency = normalizeCurrency(currencyCode);
        if (normalizedCurrency.equals(properties.getBaseCurrency())) {
            return BigDecimal.ONE;
        }

        CachedRate cachedRate = cache.get(normalizedCurrency);
        if (cachedRate != null && !cachedRate.isExpired(properties.getCacheMinutes())) {
            return cachedRate.rate();
        }

        JsonNode response = fxRestClient.get()
                .uri("/{baseCurrency}", properties.getBaseCurrency())
                .retrieve()
                .body(JsonNode.class);

        if (response == null || response.path("result").isMissingNode()) {
            throw new IllegalStateException("No se pudo obtener respuesta del proveedor FX");
        }

        String result = response.path("result").asText("");
        if (!"success".equalsIgnoreCase(result)) {
            throw new IllegalStateException("El proveedor FX devolvio un estado invalido: " + result);
        }

        JsonNode rateNode = response.path("rates").path(normalizedCurrency);
        if (!rateNode.isNumber()) {
            throw new IllegalArgumentException("No existe tasa de cambio para la moneda " + normalizedCurrency);
        }

        BigDecimal rate = rateNode.decimalValue().setScale(6, RoundingMode.HALF_UP);
        cache.put(normalizedCurrency, new CachedRate(rate, Instant.now()));
        return rate;
    }

    public BigDecimal convertFromUsd(BigDecimal usdAmount, String targetCurrency) {
        if (usdAmount == null) {
            throw new IllegalArgumentException("El monto base en USD es obligatorio");
        }

        BigDecimal rate = getRate(targetCurrency);
        return usdAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeCurrency(String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) {
            throw new IllegalArgumentException("La moneda destino es obligatoria");
        }
        return currencyCode.trim().toUpperCase();
    }

    private record CachedRate(BigDecimal rate, Instant fetchedAt) {
        private boolean isExpired(long cacheMinutes) {
            return fetchedAt.plus(Duration.ofMinutes(cacheMinutes)).isBefore(Instant.now());
        }
    }
}
