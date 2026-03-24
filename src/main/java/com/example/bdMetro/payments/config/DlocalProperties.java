package com.example.bdMetro.payments.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dlocal")
public class DlocalProperties {

    private boolean enabled;
    private String xLogin;
    private String xTransKey;
    private String secretKey;
    private String baseUrl = "https://sandbox.dlocal.com";
    private String apiVersion = "2.1";
    private String userAgent = "bdMetro / 1.0";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getXLogin() {
        return xLogin;
    }

    public void setXLogin(String xLogin) {
        this.xLogin = xLogin;
    }

    public String getXTransKey() {
        return xTransKey;
    }

    public void setXTransKey(String xTransKey) {
        this.xTransKey = xTransKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
