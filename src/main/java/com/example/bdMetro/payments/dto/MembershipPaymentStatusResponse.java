package com.example.bdMetro.payments.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MembershipPaymentStatusResponse(
        String externalId,
        String providerPaymentId,
        String status,
        String statusDetail,
        String accessCode,
        String redirectUrl,
        String countryCode,
        String currencyCode,
        Integer planMonths,
        BigDecimal amount,
        BigDecimal baseUsdAmount,
        BigDecimal exchangeRateApplied,
        LocalDateTime paidAt
) {
}
