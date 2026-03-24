package com.example.bdMetro.payments.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fx")
public class FxRateProperties {

    private String baseUrl = "https://open.er-api.com/v6/latest";
    private String baseCurrency = "USD";
    private long cacheMinutes = 15;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public long getCacheMinutes() {
        return cacheMinutes;
    }

    public void setCacheMinutes(long cacheMinutes) {
        this.cacheMinutes = cacheMinutes;
    }
}
