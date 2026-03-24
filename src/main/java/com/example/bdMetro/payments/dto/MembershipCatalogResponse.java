package com.example.bdMetro.payments.dto;

import java.math.BigDecimal;
import java.util.Map;

public record MembershipCatalogResponse(
        Map<String, CountryCatalogItem> countries
) {
    public record CountryCatalogItem(
            String displayName,
            String currency,
            String documentLabel,
            Map<String, BigDecimal> plans,
            Map<String, BigDecimal> basePlansUsd
    ) {
    }
}
