package com.example.bdMetro.payments.dto;

import java.math.BigDecimal;

public record CreateMembershipPaymentResponse(
        String externalId,
        String providerPaymentId,
        String status,
        String statusDetail,
        String redirectUrl,
        String countryCode,
        String currencyCode,
        Integer planMonths,
        BigDecimal amount,
        BigDecimal baseUsdAmount,
        BigDecimal exchangeRateApplied
) {
}
