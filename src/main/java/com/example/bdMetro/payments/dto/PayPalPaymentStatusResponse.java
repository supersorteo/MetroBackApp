package com.example.bdMetro.payments.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PayPalPaymentStatusResponse(
        String externalId,
        String paypalOrderId,
        String status,
        String statusDetail,
        String accessCode,
        String countryCode,
        String currencyCode,
        Integer planMonths,
        BigDecimal amount,
        BigDecimal baseUsdAmount,
        BigDecimal exchangeRateApplied,
        LocalDateTime paidAt
) {
}
