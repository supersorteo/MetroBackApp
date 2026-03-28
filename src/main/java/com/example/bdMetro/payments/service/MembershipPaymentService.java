package com.example.bdMetro.payments.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bdMetro.entity.AccessCode;
import com.example.bdMetro.payments.config.MembershipCatalogProperties;
import com.example.bdMetro.payments.config.MercadoPagoProperties;
import com.example.bdMetro.payments.dto.CreateMembershipPaymentRequest;
import com.example.bdMetro.payments.dto.CreateMembershipPaymentResponse;
import com.example.bdMetro.payments.dto.MembershipCatalogResponse;
import com.example.bdMetro.payments.dto.MembershipPaymentStatusResponse;
import com.example.bdMetro.payments.entity.MembershipPaymentOrder;
import com.example.bdMetro.payments.repository.MembershipPaymentOrderRepository;
import com.example.bdMetro.repository.AccessCodeRepository;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class MembershipPaymentService {

    private static final Logger log = LoggerFactory.getLogger(MembershipPaymentService.class);
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_PAID = "PAID";
    private static final String STATUS_REJECTED = "REJECTED";

    private final MembershipPaymentOrderRepository orderRepository;
    private final AccessCodeRepository accessCodeRepository;
    private final MembershipCatalogProperties catalogProperties;
    private final MercadoPagoProperties mercadoPagoProperties;
    private final MercadoPagoClient mercadoPagoClient;
    private final MercadoPagoWebhookSignatureService webhookSignatureService;
    private final FxRateService fxRateService;
    private final String appBaseUrl;

    public MembershipPaymentService(
            MembershipPaymentOrderRepository orderRepository,
            AccessCodeRepository accessCodeRepository,
            MembershipCatalogProperties catalogProperties,
            MercadoPagoProperties mercadoPagoProperties,
            MercadoPagoClient mercadoPagoClient,
            MercadoPagoWebhookSignatureService webhookSignatureService,
            FxRateService fxRateService,
            @Value("${app.base-url:http://localhost:8080}") String appBaseUrl
    ) {
        this.orderRepository = orderRepository;
        this.accessCodeRepository = accessCodeRepository;
        this.catalogProperties = catalogProperties;
        this.mercadoPagoProperties = mercadoPagoProperties;
        this.mercadoPagoClient = mercadoPagoClient;
        this.webhookSignatureService = webhookSignatureService;
        this.fxRateService = fxRateService;
        this.appBaseUrl = appBaseUrl;
    }

    public MembershipCatalogResponse getCatalog() {
        Map<String, MembershipCatalogResponse.CountryCatalogItem> countries = new LinkedHashMap<>();
        for (Map.Entry<String, MembershipCatalogProperties.CountryCatalog> entry : catalogProperties.getCatalog().entrySet()) {
            MembershipCatalogProperties.CountryCatalog country = entry.getValue();
            if (!country.isEnabled()) {
                continue;
            }
            countries.put(entry.getKey(), new MembershipCatalogResponse.CountryCatalogItem(
                    country.getDisplayName(),
                    country.getCurrency(),
                    country.getDocumentLabel(),
                    buildLocalizedPlans(country),
                    new LinkedHashMap<>(catalogProperties.getBasePlansUsd())
            ));
        }
        return new MembershipCatalogResponse(countries);
    }

    @Transactional
    public CreateMembershipPaymentResponse createCheckout(CreateMembershipPaymentRequest request) {
        requireMercadoPagoEnabled();

        String countryCode = normalizeCountry(request.countryCode());
        MembershipCatalogProperties.CountryCatalog countryCatalog = getCountryCatalog(countryCode);
        PlanQuote quote = getPlanQuote(countryCode, request.planMonths());
        String externalId = "MTR-" + UUID.randomUUID();
        String callbackUrl = resolveCallbackUrl(request.callbackUrl());
        String notificationUrl = appBaseUrl + catalogProperties.getNotificationPath();

        MembershipPaymentOrder order = new MembershipPaymentOrder();
        order.setExternalId(externalId);
        order.setStatus(STATUS_PENDING);
        order.setStatusDetail("Orden creada localmente");
        order.setCountryCode(countryCode);
        order.setCurrencyCode(countryCatalog.getCurrency());
        order.setPlanMonths(request.planMonths());
        order.setBaseUsdAmount(quote.baseUsdAmount());
        order.setExchangeRateApplied(quote.exchangeRateApplied());
        order.setAmount(quote.localizedAmount());
        order.setPayerName(request.payerName().trim());
        order.setPayerEmail(request.payerEmail().trim().toLowerCase(Locale.ROOT));
        order.setPayerPhone(request.payerPhone().trim());
        order.setPayerDocument(sanitizeDocument(request.payerDocument()));
        order.setProvince(request.province().trim());
        order.setCallbackUrl(callbackUrl);
        order.setNotificationUrl(notificationUrl);
        orderRepository.save(order);

        JsonNode preference = mercadoPagoClient.createPreference(buildPreferencePayload(order, countryCatalog));
        order.setProviderPaymentId(text(preference, "id"));
        order.setRedirectUrl(resolveInitPoint(preference, order.getCallbackUrl()));
        order.setStatusDetail("Preferencia de Mercado Pago creada");
        orderRepository.save(order);

        return toCreateResponse(order);
    }

    @Transactional
    public MembershipPaymentStatusResponse getOrderStatus(String externalId, String paymentId) {
        MembershipPaymentOrder order = orderRepository.findByExternalId(externalId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la orden " + externalId));

        if (isFinalStatus(order.getStatus())) {
            return toStatusResponse(order);
        }

        // Si MP nos redirigió con el payment_id real, lo guardamos
        if (paymentId != null && !paymentId.isBlank() && isNumeric(paymentId)) {
            log.info("[STATUS] paymentId recibido del redirect de MP: {}", paymentId);
            order.setProviderPaymentId(paymentId);
            orderRepository.save(order);
        }

        // Consultamos el estado real en MP si tenemos un ID numérico
        if (order.getProviderPaymentId() != null && isNumeric(order.getProviderPaymentId())) {
            JsonNode payment = mercadoPagoClient.getPayment(order.getProviderPaymentId());
            applyMercadoPagoState(order, payment, payment.toString());
            orderRepository.save(order);
        }

        return toStatusResponse(order);
    }

    @Transactional
    public void processMercadoPagoWebhook(String xSignature, String xRequestId, String dataId, String type, String rawBody) {
        requireMercadoPagoEnabled();

        if (!"payment".equalsIgnoreCase(type) || dataId == null || dataId.isBlank()) {
            return;
        }

        if (!webhookSignatureService.isValid(xSignature, xRequestId, dataId)) {
            throw new IllegalArgumentException("La firma del webhook de Mercado Pago no es valida");
        }

        JsonNode payment = mercadoPagoClient.getPayment(dataId);
        String externalReference = text(payment, "external_reference");
        if (externalReference == null || externalReference.isBlank()) {
            throw new IllegalArgumentException("El pago de Mercado Pago no tiene external_reference");
        }

        MembershipPaymentOrder order = orderRepository.findByExternalId(externalReference)
                .orElseThrow(() -> new IllegalArgumentException("No existe la orden " + externalReference));

        applyMercadoPagoState(order, payment, rawBody != null && !rawBody.isBlank() ? rawBody : payment.toString());
        orderRepository.save(order);
    }

    private Map<String, Object> buildPreferencePayload(
            MembershipPaymentOrder order,
            MembershipCatalogProperties.CountryCatalog countryCatalog
    ) {
        String successUrl = resolveSuccessUrl(order.getCallbackUrl());
        String failureUrl = resolveFailureUrl(order.getCallbackUrl());
        String pendingUrl = resolvePendingUrl(order.getCallbackUrl());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("items", List.of(Map.of(
                "id", "membership-" + order.getPlanMonths(),
                "title", "Membresia Metro " + order.getPlanMonths() + " meses",
                "description", "Acceso a precios y tareas por pais",
                "quantity", 1,
                "currency_id", countryCatalog.getCurrency(),
                "unit_price", order.getAmount()
        )));
        payload.put("payer", buildPayerPayload(order));
        payload.put("external_reference", order.getExternalId());
        payload.put("notification_url", order.getNotificationUrl());
        payload.put("back_urls", Map.of(
                "success", successUrl,
                "failure", failureUrl,
                "pending", pendingUrl
        ));
        if (successUrl != null && !successUrl.contains("localhost") && !successUrl.contains("127.0.0.1")) {
            payload.put("auto_return", "approved");
        }
        return payload;
    }

    private Map<String, Object> buildPayerPayload(MembershipPaymentOrder order) {
        Map<String, Object> payer = new LinkedHashMap<>();
        payer.put("name", order.getPayerName());
        payer.put("email", order.getPayerEmail());
        if (order.getPayerDocument() != null && !order.getPayerDocument().isBlank()) {
            payer.put("identification", Map.of(
                    "type", resolveDocumentType(order.getCountryCode()),
                    "number", order.getPayerDocument()
            ));
        }
        return payer;
    }

    private String resolveDocumentType(String countryCode) {
        return switch (countryCode) {
            case "AR" -> "DNI";
            case "UY" -> "CI";
            case "CO" -> "CC";
            default -> "DNI";
        };
    }

    private void applyMercadoPagoState(MembershipPaymentOrder order, JsonNode payment, String rawPayload) {
        String paymentId = text(payment, "id");
        if (paymentId != null) {
            order.setProviderPaymentId(paymentId);
        }

        String mercadoPagoStatus = text(payment, "status");
        order.setStatus(mapProviderStatus(mercadoPagoStatus));
        order.setStatusDetail(firstNonBlank(text(payment, "status_detail"), mercadoPagoStatus, "Estado actualizado por Mercado Pago"));

        if (rawPayload != null) {
            order.setRawProviderPayload(rawPayload);
        }

        if (STATUS_PAID.equals(order.getStatus())) {
            issueOrRenewAccess(order);
            if (order.getPaidAt() == null) {
                order.setPaidAt(LocalDateTime.now());
            }
        }
    }

    private void issueOrRenewAccess(MembershipPaymentOrder order) {
        AccessCode existingByEmail = accessCodeRepository.findByEmail(order.getPayerEmail());
        if (existingByEmail != null) {
            LocalDate baseDate = existingByEmail.getFechaVencimiento() != null && existingByEmail.getFechaVencimiento().isAfter(LocalDate.now())
                    ? existingByEmail.getFechaVencimiento()
                    : LocalDate.now();

            existingByEmail.setTelefono(order.getPayerPhone());
            existingByEmail.setPais(expandCountry(order.getCountryCode()));
            existingByEmail.setProvincia(order.getProvince());
            existingByEmail.setFechaRegistro(LocalDate.now());
            existingByEmail.setFechaVencimiento(baseDate.plusMonths(order.getPlanMonths()));
            accessCodeRepository.save(existingByEmail);
            order.setAccessCode(existingByEmail.getCode());
            log.info("[CODIGO] Renovado para email={} | codigo={} | vence={}",
                    order.getPayerEmail(), existingByEmail.getCode(), existingByEmail.getFechaVencimiento());
            return;
        }

        AccessCode accessCode = new AccessCode();
        accessCode.setCode(generateUniqueCode(order.getPlanMonths()));
        accessCode.setEmail(order.getPayerEmail());
        accessCode.setTelefono(order.getPayerPhone());
        accessCode.setPais(expandCountry(order.getCountryCode()));
        accessCode.setProvincia(order.getProvince());
        accessCode.setFechaRegistro(LocalDate.now());
        accessCode.setFechaVencimiento(LocalDate.now().plusMonths(order.getPlanMonths()));
        accessCodeRepository.save(accessCode);
        order.setAccessCode(accessCode.getCode());
        log.info("[CODIGO] Generado nuevo | email={} | codigo={} | vence={}",
                order.getPayerEmail(), accessCode.getCode(), accessCode.getFechaVencimiento());
    }

    private String generateUniqueCode(int planMonths) {
        int codeLength = planMonths <= 3 ? 5 : 6;
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";

        while (true) {
            StringBuilder code = new StringBuilder(codeLength);
            for (int index = 0; index < codeLength; index++) {
                code.append(alphabet.charAt(ThreadLocalRandom.current().nextInt(alphabet.length())));
            }
            if (accessCodeRepository.findByCode(code.toString()) == null) {
                return code.toString();
            }
        }
    }

    private MembershipCatalogProperties.CountryCatalog getCountryCatalog(String countryCode) {
        MembershipCatalogProperties.CountryCatalog catalog = catalogProperties.getCatalog().get(countryCode);
        if (catalog == null || !catalog.isEnabled()) {
            throw new IllegalArgumentException("El pais " + countryCode + " no esta habilitado para pagos");
        }
        return catalog;
    }

    private PlanQuote getPlanQuote(String countryCode, Integer planMonths) {
        MembershipCatalogProperties.CountryCatalog catalog = getCountryCatalog(countryCode);
        BigDecimal baseUsdAmount = catalogProperties.getBasePlansUsd().get(String.valueOf(planMonths));
        if (baseUsdAmount == null) {
            throw new IllegalArgumentException("No existe precio base en USD para " + planMonths + " meses");
        }
        BigDecimal exchangeRate = fxRateService.getRate(catalog.getCurrency());
        BigDecimal localizedAmount = baseUsdAmount.multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
        return new PlanQuote(baseUsdAmount, exchangeRate, localizedAmount);
    }

    private Map<String, BigDecimal> buildLocalizedPlans(MembershipCatalogProperties.CountryCatalog country) {
        Map<String, BigDecimal> localizedPlans = new LinkedHashMap<>();
        for (Map.Entry<String, BigDecimal> entry : catalogProperties.getBasePlansUsd().entrySet()) {
            localizedPlans.put(entry.getKey(), fxRateService.convertFromUsd(entry.getValue(), country.getCurrency()));
        }
        return localizedPlans;
    }

    private String resolveCallbackUrl(String callbackUrl) {
        if (callbackUrl != null && !callbackUrl.isBlank()) {
            return callbackUrl.trim();
        }
        if (catalogProperties.getDefaultCallbackUrl() != null && !catalogProperties.getDefaultCallbackUrl().isBlank()) {
            return catalogProperties.getDefaultCallbackUrl().trim();
        }
        return appBaseUrl + "/payment-result";
    }

    private String resolveSuccessUrl(String callbackUrl) {
        return firstNonBlank(mercadoPagoProperties.getSuccessUrl(), callbackUrl);
    }

    private String resolveFailureUrl(String callbackUrl) {
        return firstNonBlank(mercadoPagoProperties.getFailureUrl(), callbackUrl);
    }

    private String resolvePendingUrl(String callbackUrl) {
        return firstNonBlank(mercadoPagoProperties.getPendingUrl(), callbackUrl);
    }

    private String resolveInitPoint(JsonNode preference, String callbackUrl) {
        String sandboxInitPoint = text(preference, "sandbox_init_point");
        String initPoint        = text(preference, "init_point");

        boolean isLocal = callbackUrl != null &&
                (callbackUrl.contains("localhost") || callbackUrl.contains("127.0.0.1"));
        boolean useSandbox = isLocal || mercadoPagoProperties.isSandbox();

        if (useSandbox && sandboxInitPoint != null && !sandboxInitPoint.isBlank()) {
            log.info("[MP] Usando sandbox_init_point (sandbox={}, local={})", mercadoPagoProperties.isSandbox(), isLocal);
            return sandboxInitPoint;
        }
        if (initPoint != null && !initPoint.isBlank()) {
            return initPoint;
        }
        if (sandboxInitPoint != null && !sandboxInitPoint.isBlank()) {
            return sandboxInitPoint;
        }
        throw new IllegalStateException("Mercado Pago no devolvio una URL de checkout");
    }

    private void requireMercadoPagoEnabled() {
        if (!mercadoPagoProperties.isEnabled()) {
            throw new IllegalStateException("Mercado Pago no esta habilitado. Configure el access token antes de usar pagos.");
        }
    }

    private String normalizeCountry(String countryCode) {
        return countryCode == null ? "" : countryCode.trim().toUpperCase(Locale.ROOT);
    }

    private String sanitizeDocument(String document) {
        return document == null ? "" : document.replaceAll("[^A-Za-z0-9]", "");
    }

    private boolean isFinalStatus(String status) {
        return STATUS_PAID.equalsIgnoreCase(status) || STATUS_REJECTED.equalsIgnoreCase(status);
    }

    private String expandCountry(String countryCode) {
        return switch (countryCode) {
            case "AR" -> "Argentina";
            case "UY" -> "Uruguay";
            case "CO" -> "Colombia";
            default -> countryCode;
        };
    }

    private String mapProviderStatus(String providerStatus) {
        if (providerStatus == null || providerStatus.isBlank()) {
            return STATUS_PENDING;
        }
        return switch (providerStatus.toLowerCase(Locale.ROOT)) {
            case "approved" -> STATUS_PAID;
            case "rejected", "cancelled", "cancelled_by_user", "refunded", "charged_back" -> STATUS_REJECTED;
            default -> STATUS_PENDING;
        };
    }

    private String text(JsonNode root, String field) {
        JsonNode node = root.get(field);
        return node == null || node.isNull() ? null : node.asText();
    }

    private boolean isNumeric(String value) {
        return value != null && value.matches("\\d+");
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private CreateMembershipPaymentResponse toCreateResponse(MembershipPaymentOrder order) {
        return new CreateMembershipPaymentResponse(
                order.getExternalId(),
                order.getProviderPaymentId(),
                order.getStatus(),
                order.getStatusDetail(),
                order.getRedirectUrl(),
                order.getCountryCode(),
                order.getCurrencyCode(),
                order.getPlanMonths(),
                order.getAmount(),
                order.getBaseUsdAmount(),
                order.getExchangeRateApplied()
        );
    }

    private MembershipPaymentStatusResponse toStatusResponse(MembershipPaymentOrder order) {
        return new MembershipPaymentStatusResponse(
                order.getExternalId(),
                order.getProviderPaymentId(),
                order.getStatus(),
                order.getStatusDetail(),
                order.getAccessCode(),
                order.getRedirectUrl(),
                order.getCountryCode(),
                order.getCurrencyCode(),
                order.getPlanMonths(),
                order.getAmount(),
                order.getBaseUsdAmount(),
                order.getExchangeRateApplied(),
                order.getPaidAt(),
                order.getPayerPhone()
        );
    }

    private record PlanQuote(
            BigDecimal baseUsdAmount,
            BigDecimal exchangeRateApplied,
            BigDecimal localizedAmount
    ) {
    }
}
