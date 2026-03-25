package com.example.bdMetro.payments.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "membership_payment_order")
public class MembershipPaymentOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String externalId;

    @Column(unique = true, length = 80)
    private String providerPaymentId;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(length = 255)
    private String statusDetail;

    @Column(nullable = false, length = 2)
    private String countryCode;

    @Column(nullable = false, length = 3)
    private String currencyCode;

    @Column(nullable = false)
    private Integer planMonths;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal baseUsdAmount;

    @Column(nullable = false, precision = 15, scale = 6)
    private BigDecimal exchangeRateApplied;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 120)
    private String payerName;

    @Column(nullable = false, length = 120)
    private String payerEmail;

    @Column(length = 40)
    private String payerPhone;

    @Column(nullable = false, length = 40)
    private String payerDocument;

    @Column(length = 120)
    private String province;

    @Column(length = 500)
    private String callbackUrl;

    @Column(length = 500)
    private String notificationUrl;

    @Column(length = 500)
    private String redirectUrl;

    @Column(length = 40)
    private String accessCode;

    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Lob
    private String rawProviderPayload;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getProviderPaymentId() {
        return providerPaymentId;
    }

    public void setProviderPaymentId(String providerPaymentId) {
        this.providerPaymentId = providerPaymentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusDetail() {
        return statusDetail;
    }

    public void setStatusDetail(String statusDetail) {
        this.statusDetail = statusDetail;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public Integer getPlanMonths() {
        return planMonths;
    }

    public void setPlanMonths(Integer planMonths) {
        this.planMonths = planMonths;
    }

    public BigDecimal getBaseUsdAmount() {
        return baseUsdAmount;
    }

    public void setBaseUsdAmount(BigDecimal baseUsdAmount) {
        this.baseUsdAmount = baseUsdAmount;
    }

    public BigDecimal getExchangeRateApplied() {
        return exchangeRateApplied;
    }

    public void setExchangeRateApplied(BigDecimal exchangeRateApplied) {
        this.exchangeRateApplied = exchangeRateApplied;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPayerName() {
        return payerName;
    }

    public void setPayerName(String payerName) {
        this.payerName = payerName;
    }

    public String getPayerEmail() {
        return payerEmail;
    }

    public void setPayerEmail(String payerEmail) {
        this.payerEmail = payerEmail;
    }

    public String getPayerPhone() {
        return payerPhone;
    }

    public void setPayerPhone(String payerPhone) {
        this.payerPhone = payerPhone;
    }

    public String getPayerDocument() {
        return payerDocument;
    }

    public void setPayerDocument(String payerDocument) {
        this.payerDocument = payerDocument;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getNotificationUrl() {
        return notificationUrl;
    }

    public void setNotificationUrl(String notificationUrl) {
        this.notificationUrl = notificationUrl;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getAccessCode() {
        return accessCode;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getRawProviderPayload() {
        return rawProviderPayload;
    }

    public void setRawProviderPayload(String rawProviderPayload) {
        this.rawProviderPayload = rawProviderPayload;
    }
}
