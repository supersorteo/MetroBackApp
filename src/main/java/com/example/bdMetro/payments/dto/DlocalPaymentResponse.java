package com.example.bdMetro.payments.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DlocalPaymentResponse(
        String id,
        BigDecimal amount,
        String currency,
        String country,
        String status,
        @JsonProperty("status_detail") String statusDetail,
        @JsonProperty("status_code") String statusCode,
        @JsonProperty("redirect_url") String redirectUrl,
        @JsonProperty("order_id") String orderId
) {
}
