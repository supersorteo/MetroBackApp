package com.example.bdMetro.payments.dto;

import java.math.BigDecimal;

public record CreatePayPalPaymentResponse(
        String externalId,
        String paypalOrderId,
        String status,
        String approvalUrl,
        String countryCode,
        String currencyCode,
        Integer planMonths,
        BigDecimal amount,
        BigDecimal baseUsdAmount,
        BigDecimal exchangeRateApplied
) {
}
