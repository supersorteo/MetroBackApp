package com.example.bdMetro.payments.config;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payments.membership")
public class MembershipCatalogProperties {

    private String notificationPath = "/api/payments/memberships/dlocal/webhook";
    private String defaultCallbackUrl;
    private final Map<String, BigDecimal> basePlansUsd = new LinkedHashMap<>();
    private final Map<String, CountryCatalog> catalog = new LinkedHashMap<>();

    public String getNotificationPath() {
        return notificationPath;
    }

    public void setNotificationPath(String notificationPath) {
        this.notificationPath = notificationPath;
    }

    public String getDefaultCallbackUrl() {
        return defaultCallbackUrl;
    }

    public void setDefaultCallbackUrl(String defaultCallbackUrl) {
        this.defaultCallbackUrl = defaultCallbackUrl;
    }

    public Map<String, BigDecimal> getBasePlansUsd() {
        return basePlansUsd;
    }

    public Map<String, CountryCatalog> getCatalog() {
        return catalog;
    }

    public static class CountryCatalog {
        private boolean enabled;
        private String currency;
        private String displayName;
        private String documentLabel;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getDocumentLabel() {
            return documentLabel;
        }

        public void setDocumentLabel(String documentLabel) {
            this.documentLabel = documentLabel;
        }
    }
}
