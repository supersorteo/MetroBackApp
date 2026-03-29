package com.example.bdMetro.payments.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bdMetro.entity.AccessCode;
import com.example.bdMetro.payments.config.MembershipCatalogProperties;
import com.example.bdMetro.payments.config.PayPalProperties;
import com.example.bdMetro.payments.dto.CreatePayPalPaymentRequest;
import com.example.bdMetro.payments.dto.CreatePayPalPaymentResponse;
import com.example.bdMetro.payments.dto.PayPalPaymentStatusResponse;
import com.example.bdMetro.payments.entity.PayPalPaymentOrder;
import com.example.bdMetro.payments.repository.PayPalPaymentOrderRepository;
import com.example.bdMetro.repository.AccessCodeRepository;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class PayPalPaymentService {

    private static final String STATUS_PENDING  = "PENDING";
    private static final String STATUS_PAID     = "PAID";
    private static final String STATUS_REJECTED = "REJECTED";

    private final PayPalPaymentOrderRepository orderRepository;
    private final AccessCodeRepository accessCodeRepository;
    private final MembershipCatalogProperties catalogProperties;
    private final PayPalProperties payPalProperties;
    private final PayPalClient payPalClient;
    private final FxRateService fxRateService;

    public PayPalPaymentService(
            PayPalPaymentOrderRepository orderRepository,
            AccessCodeRepository accessCodeRepository,
            MembershipCatalogProperties catalogProperties,
            PayPalProperties payPalProperties,
            PayPalClient payPalClient,
            FxRateService fxRateService
    ) {
        this.orderRepository    = orderRepository;
        this.accessCodeRepository = accessCodeRepository;
        this.catalogProperties  = catalogProperties;
        this.payPalProperties   = payPalProperties;
        this.payPalClient       = payPalClient;
        this.fxRateService      = fxRateService;
    }

    @Transactional
    public CreatePayPalPaymentResponse createCheckout(CreatePayPalPaymentRequest request) {
        requirePayPalEnabled();

        String countryCode = request.countryCode().trim().toUpperCase(Locale.ROOT);
        validateExistingEmailCountry(request.payerEmail(), countryCode);
        MembershipCatalogProperties.CountryCatalog countryCatalog = getCountryCatalog(countryCode);

        BigDecimal baseUsdAmount = getBaseUsdAmount(request.planMonths());
        BigDecimal exchangeRate  = fxRateService.getRate(countryCatalog.getCurrency());
        BigDecimal localAmount   = baseUsdAmount.multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);

        String externalId   = "PP-" + UUID.randomUUID();
        String callbackUrl  = resolveCallbackUrl(request.callbackUrl());

        PayPalPaymentOrder order = new PayPalPaymentOrder();
        order.setExternalId(externalId);
        order.setStatus(STATUS_PENDING);
        order.setStatusDetail("Orden creada localmente");
        order.setCountryCode(countryCode);
        order.setCurrencyCode(countryCatalog.getCurrency());
        order.setPlanMonths(request.planMonths());
        order.setBaseUsdAmount(baseUsdAmount);
        order.setExchangeRateApplied(exchangeRate);
        order.setAmount(localAmount);
        order.setPayerName(request.payerName().trim());
        order.setPayerEmail(request.payerEmail().trim().toLowerCase(Locale.ROOT));
        order.setPayerPhone(request.payerPhone() != null ? request.payerPhone().trim() : "");
        order.setPayerDocument(request.payerDocument() != null ? request.payerDocument().trim() : "");
        order.setProvince(request.province().trim());
        orderRepository.save(order);

        String accessToken = payPalClient.getAccessToken();
        JsonNode ppOrder   = payPalClient.createOrder(buildOrderPayload(order, callbackUrl), accessToken);

        order.setPaypalOrderId(text(ppOrder, "id"));
        order.setApprovalUrl(extractApprovalUrl(ppOrder));
        order.setStatusDetail("Orden PayPal creada");
        orderRepository.save(order);

        return toCreateResponse(order);
    }

    @Transactional
    public PayPalPaymentStatusResponse capturePayment(String externalId, String paypalOrderId) {
        requirePayPalEnabled();

        PayPalPaymentOrder order = findOrder(externalId, paypalOrderId);

        if (isFinalStatus(order.getStatus())) {
            return toStatusResponse(order);
        }

        String accessToken = payPalClient.getAccessToken();
        String orderIdToCapture = order.getPaypalOrderId() != null ? order.getPaypalOrderId() : paypalOrderId;

        JsonNode capture = payPalClient.captureOrder(orderIdToCapture, accessToken);
        applyPayPalState(order, capture);
        orderRepository.save(order);

        return toStatusResponse(order);
    }

    @Transactional
    public PayPalPaymentStatusResponse getOrderStatus(String externalId) {
        PayPalPaymentOrder order = orderRepository.findByExternalId(externalId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la orden " + externalId));

        if (!isFinalStatus(order.getStatus()) && order.getPaypalOrderId() != null) {
            String accessToken = payPalClient.getAccessToken();
            JsonNode ppOrder   = payPalClient.getOrder(order.getPaypalOrderId(), accessToken);
            String ppStatus    = text(ppOrder, "status");
            if ("COMPLETED".equalsIgnoreCase(ppStatus)) {
                applyPayPalState(order, ppOrder);
                orderRepository.save(order);
            }
        }

        return toStatusResponse(order);
    }

    private void applyPayPalState(PayPalPaymentOrder order, JsonNode payload) {
        String ppStatus = text(payload, "status");
        order.setStatus(mapProviderStatus(ppStatus));
        order.setStatusDetail(firstNonBlank(ppStatus, "Estado actualizado por PayPal"));
        order.setRawProviderPayload(payload.toString());

        if (STATUS_PAID.equals(order.getStatus())) {
            issueOrRenewAccess(order);
            if (order.getPaidAt() == null) {
                order.setPaidAt(LocalDateTime.now());
            }
        }
    }

    private void issueOrRenewAccess(PayPalPaymentOrder order) {
        AccessCode existing = accessCodeRepository.findByEmail(order.getPayerEmail());
        if (existing != null) {
            validateExistingAccessCountry(existing, order.getCountryCode());
            LocalDate base = existing.getFechaVencimiento() != null && existing.getFechaVencimiento().isAfter(LocalDate.now())
                    ? existing.getFechaVencimiento() : LocalDate.now();
            existing.setTelefono(order.getPayerPhone());
            existing.setPais(expandCountry(order.getCountryCode()));
            existing.setProvincia(order.getProvince());
            existing.setFechaRegistro(LocalDate.now());
            existing.setFechaVencimiento(base.plusMonths(order.getPlanMonths()));
            accessCodeRepository.save(existing);
            order.setAccessCode(existing.getCode());
            return;
        }
        AccessCode code = new AccessCode();
        code.setCode(generateUniqueCode(order.getPlanMonths()));
        code.setEmail(order.getPayerEmail());
        code.setTelefono(order.getPayerPhone());
        code.setPais(expandCountry(order.getCountryCode()));
        code.setProvincia(order.getProvince());
        code.setFechaRegistro(LocalDate.now());
        code.setFechaVencimiento(LocalDate.now().plusMonths(order.getPlanMonths()));
        accessCodeRepository.save(code);
        order.setAccessCode(code.getCode());
    }

    private Map<String, Object> buildOrderPayload(PayPalPaymentOrder order, String callbackUrl) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("intent", "CAPTURE");

        Map<String, Object> amount = new LinkedHashMap<>();
        amount.put("currency_code", order.getCurrencyCode());
        amount.put("value", order.getAmount().toPlainString());

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", "Membresia Metro " + order.getPlanMonths() + " meses");
        item.put("quantity", "1");
        item.put("unit_amount", Map.of("currency_code", order.getCurrencyCode(), "value", order.getAmount().toPlainString()));

        Map<String, Object> purchaseUnit = new LinkedHashMap<>();
        purchaseUnit.put("reference_id", order.getExternalId());
        purchaseUnit.put("description", "Acceso Metro - " + order.getPlanMonths() + " meses");
        purchaseUnit.put("amount", amount);
        purchaseUnit.put("items", List.of(item));

        payload.put("purchase_units", List.of(purchaseUnit));
        payload.put("application_context", Map.of(
                "return_url", resolveReturnUrl(callbackUrl, order.getExternalId()),
                "cancel_url", firstNonBlank(payPalProperties.getCancelUrl(), callbackUrl),
                "brand_name", "Metro App",
                "landing_page", "LOGIN",
                "user_action", "PAY_NOW"
        ));
        return payload;
    }

    private String resolveReturnUrl(String callbackUrl, String externalId) {
        String base = firstNonBlank(payPalProperties.getSuccessUrl(), callbackUrl);
        return base + (base.contains("?") ? "&" : "?") + "externalId=" + externalId + "&provider=paypal";
    }

    private String resolveCallbackUrl(String callbackUrl) {
        if (callbackUrl != null && !callbackUrl.isBlank()) return callbackUrl.trim();
        if (catalogProperties.getDefaultCallbackUrl() != null) return catalogProperties.getDefaultCallbackUrl().trim();
        return "http://localhost:4200/payment-result";
    }

    private String extractApprovalUrl(JsonNode order) {
        JsonNode links = order.get("links");
        if (links != null && links.isArray()) {
            for (JsonNode link : links) {
                if ("approve".equals(text(link, "rel"))) {
                    return text(link, "href");
                }
            }
        }
        throw new IllegalStateException("PayPal no devolvio URL de aprobacion");
    }

    private PayPalPaymentOrder findOrder(String externalId, String paypalOrderId) {
        if (externalId != null && !externalId.isBlank()) {
            return orderRepository.findByExternalId(externalId)
                    .orElseThrow(() -> new IllegalArgumentException("No existe la orden " + externalId));
        }
        return orderRepository.findByPaypalOrderId(paypalOrderId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la orden PayPal " + paypalOrderId));
    }

    private String mapProviderStatus(String status) {
        if (status == null) return STATUS_PENDING;
        return switch (status.toUpperCase(Locale.ROOT)) {
            case "COMPLETED" -> STATUS_PAID;
            case "VOIDED", "EXPIRED" -> STATUS_REJECTED;
            default -> STATUS_PENDING;
        };
    }

    private boolean isFinalStatus(String status) {
        return STATUS_PAID.equals(status) || STATUS_REJECTED.equals(status);
    }

    private BigDecimal getBaseUsdAmount(Integer planMonths) {
        BigDecimal base = catalogProperties.getBasePlansUsd().get(String.valueOf(planMonths));
        if (base == null) throw new IllegalArgumentException("No existe precio base en USD para " + planMonths + " meses");
        return base;
    }

    private MembershipCatalogProperties.CountryCatalog getCountryCatalog(String countryCode) {
        MembershipCatalogProperties.CountryCatalog c = catalogProperties.getCatalog().get(countryCode);
        if (c == null || !c.isEnabled()) throw new IllegalArgumentException("Pais no habilitado: " + countryCode);
        return c;
    }

    private void validateExistingEmailCountry(String payerEmail, String requestedCountryCode) {
        String normalizedEmail = payerEmail == null ? "" : payerEmail.trim().toLowerCase(Locale.ROOT);
        if (normalizedEmail.isBlank()) {
            return;
        }

        AccessCode existing = accessCodeRepository.findByEmail(normalizedEmail);
        if (existing != null) {
            validateExistingAccessCountry(existing, requestedCountryCode);
        }
    }

    private void validateExistingAccessCountry(AccessCode existing, String requestedCountryCode) {
        String existingCountryCode = normalizeCountryCodeFromAccess(existing.getPais());
        String normalizedRequestedCountryCode = requestedCountryCode == null ? "" : requestedCountryCode.trim().toUpperCase(Locale.ROOT);
        if (!existingCountryCode.isBlank()
                && !normalizedRequestedCountryCode.isBlank()
                && !existingCountryCode.equals(normalizedRequestedCountryCode)) {
            throw new IllegalArgumentException(
                    "El email ya tiene un codigo asociado a " + expandCountry(existingCountryCode)
                            + " y no puede comprar una membresia para " + expandCountry(normalizedRequestedCountryCode)
            );
        }
    }

    private String expandCountry(String code) {
        return switch (code) {
            case "AR" -> "Argentina";
            case "UY" -> "Uruguay";
            case "CO" -> "Colombia";
            default -> code;
        };
    }

    private String normalizeCountryCodeFromAccess(String country) {
        if (country == null) {
            return "";
        }

        return switch (country.trim().toUpperCase(Locale.ROOT)) {
            case "ARGENTINA", "AR" -> "AR";
            case "URUGUAY", "UY" -> "UY";
            case "COLOMBIA", "CO" -> "CO";
            default -> country.trim().toUpperCase(Locale.ROOT);
        };
    }

    private String generateUniqueCode(int planMonths) {
        int len = planMonths <= 3 ? 5 : 6;
        String alpha = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
        while (true) {
            StringBuilder sb = new StringBuilder(len);
            for (int i = 0; i < len; i++) sb.append(alpha.charAt(ThreadLocalRandom.current().nextInt(alpha.length())));
            if (accessCodeRepository.findByCode(sb.toString()) == null) return sb.toString();
        }
    }

    private String text(JsonNode node, String field) {
        if (node == null) return null;
        JsonNode child = node.get(field);
        return child == null || child.isNull() ? null : child.asText();
    }

    private String firstNonBlank(String... values) {
        for (String v : values) if (v != null && !v.isBlank()) return v.trim();
        return null;
    }

    private void requirePayPalEnabled() {
        if (!payPalProperties.isEnabled()) throw new IllegalStateException("PayPal no esta habilitado.");
    }

    private CreatePayPalPaymentResponse toCreateResponse(PayPalPaymentOrder o) {
        return new CreatePayPalPaymentResponse(o.getExternalId(), o.getPaypalOrderId(), o.getStatus(),
                o.getApprovalUrl(), o.getCountryCode(), o.getCurrencyCode(), o.getPlanMonths(),
                o.getAmount(), o.getBaseUsdAmount(), o.getExchangeRateApplied());
    }

    private PayPalPaymentStatusResponse toStatusResponse(PayPalPaymentOrder o) {
        return new PayPalPaymentStatusResponse(o.getExternalId(), o.getPaypalOrderId(), o.getStatus(),
                o.getStatusDetail(), o.getAccessCode(), o.getPayerPhone(), o.getCountryCode(), o.getCurrencyCode(),
                o.getPlanMonths(), o.getAmount(), o.getBaseUsdAmount(), o.getExchangeRateApplied(), o.getPaidAt());
    }
}
