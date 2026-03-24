package com.example.bdMetro.payments.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DlocalCreatePaymentRequest(
        BigDecimal amount,
        String currency,
        String country,
        @JsonProperty("payment_method_flow") String paymentMethodFlow,
        @JsonProperty("payment_method_id") String paymentMethodId,
        Payer payer,
        @JsonProperty("order_id") String orderId,
        String description,
        @JsonProperty("notification_url") String notificationUrl,
        @JsonProperty("callback_url") String callbackUrl
) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Payer(
            String name,
            String email,
            String document
    ) {
    }
}
