package com.example.bdMetro.payments.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bdMetro.entity.AccessCode;
import com.example.bdMetro.payments.config.DlocalProperties;
import com.example.bdMetro.payments.config.MembershipCatalogProperties;
import com.example.bdMetro.payments.dto.CreateMembershipPaymentRequest;
import com.example.bdMetro.payments.dto.CreateMembershipPaymentResponse;
import com.example.bdMetro.payments.dto.DlocalCreatePaymentRequest;
import com.example.bdMetro.payments.dto.DlocalPaymentResponse;
import com.example.bdMetro.payments.dto.MembershipCatalogResponse;
import com.example.bdMetro.payments.dto.MembershipPaymentStatusResponse;
import com.example.bdMetro.payments.entity.MembershipPaymentOrder;
import com.example.bdMetro.payments.repository.MembershipPaymentOrderRepository;
import com.example.bdMetro.repository.AccessCodeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MembershipPaymentService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_PAID = "PAID";
    private static final String STATUS_REJECTED = "REJECTED";

    private final MembershipPaymentOrderRepository orderRepository;
    private final AccessCodeRepository accessCodeRepository;
    private final MembershipCatalogProperties catalogProperties;
    private final DlocalProperties dlocalProperties;
    private final DlocalClient dlocalClient;
    private final DlocalSignatureService signatureService;
    private final FxRateService fxRateService;
    private final ObjectMapper objectMapper;
    private final String appBaseUrl;

    public MembershipPaymentService(
            MembershipPaymentOrderRepository orderRepository,
            AccessCodeRepository accessCodeRepository,
            MembershipCatalogProperties catalogProperties,
            DlocalProperties dlocalProperties,
            DlocalClient dlocalClient,
            DlocalSignatureService signatureService,
            FxRateService fxRateService,
            ObjectMapper objectMapper,
            @Value("${app.base-url:http://localhost:8080}") String appBaseUrl
    ) {
        this.orderRepository = orderRepository;
        this.accessCodeRepository = accessCodeRepository;
        this.catalogProperties = catalogProperties;
        this.dlocalProperties = dlocalProperties;
        this.dlocalClient = dlocalClient;
        this.signatureService = signatureService;
        this.fxRateService = fxRateService;
        this.objectMapper = objectMapper;
        this.appBaseUrl = appBaseUrl;
    }

    public MembershipCatalogResponse getCatalog() {
        Map<String, MembershipCatalogResponse.CountryCatalogItem> countries = new LinkedHashMap<>();
        for (Map.Entry<String, MembershipCatalogProperties.CountryCatalog> entry : catalogProperties.getCatalog().entrySet()) {
            MembershipCatalogProperties.CountryCatalog country = entry.getValue();
            if (!country.isEnabled()) {
                continue;
            }
            Map<String, BigDecimal> localizedPlans = buildLocalizedPlans(country);
            countries.put(entry.getKey(), new MembershipCatalogResponse.CountryCatalogItem(
                    country.getDisplayName(),
                    country.getCurrency(),
                    country.getDocumentLabel(),
                    localizedPlans,
                    new LinkedHashMap<>(catalogProperties.getBasePlansUsd())
            ));
        }
        return new MembershipCatalogResponse(countries);
    }

    @Transactional
    public CreateMembershipPaymentResponse createCheckout(CreateMembershipPaymentRequest request) {
        requireDlocalEnabled();

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

        DlocalCreatePaymentRequest providerRequest = new DlocalCreatePaymentRequest(
                quote.localizedAmount(),
                countryCatalog.getCurrency(),
                countryCode,
                "REDIRECT",
                blankToNull(request.paymentMethodId()),
                new DlocalCreatePaymentRequest.Payer(order.getPayerName(), order.getPayerEmail(), order.getPayerDocument()),
                externalId,
                "Membresia Metro " + request.planMonths() + " meses - " + countryCode,
                notificationUrl,
                callbackUrl
        );

        DlocalPaymentResponse providerResponse = dlocalClient.createPayment(providerRequest);
        order.setDlocalPaymentId(providerResponse.id());
        order.setStatus(providerResponse.status());
        order.setStatusDetail(providerResponse.statusDetail());
        order.setRedirectUrl(providerResponse.redirectUrl());
        orderRepository.save(order);

        return toCreateResponse(order);
    }

    @Transactional
    public MembershipPaymentStatusResponse getOrderStatus(String externalId) {
        MembershipPaymentOrder order = orderRepository.findByExternalId(externalId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la orden " + externalId));

        if (order.getDlocalPaymentId() != null && !isFinalStatus(order.getStatus())) {
            DlocalPaymentResponse providerPayment = dlocalClient.getPayment(order.getDlocalPaymentId());
            applyProviderState(order, providerPayment, null);
            orderRepository.save(order);
        }

        return toStatusResponse(order);
    }

    @Transactional
    public void processWebhook(String authorizationHeader, String xDate, String rawBody) {
        requireDlocalEnabled();

        if (!signatureService.isValidAuthorization(authorizationHeader, xDate, rawBody)) {
            throw new IllegalArgumentException("La firma del webhook de dLocal no es válida");
        }

        JsonNode root = parseBody(rawBody);
        String providerPaymentId = text(root, "id");
        String orderId = text(root, "order_id");
        if (providerPaymentId == null && orderId == null) {
            throw new IllegalArgumentException("Webhook de dLocal sin identificadores");
        }

        MembershipPaymentOrder order = findOrder(providerPaymentId, orderId);
        DlocalPaymentResponse confirmedPayment = order.getDlocalPaymentId() != null
                ? dlocalClient.getPayment(order.getDlocalPaymentId())
                : mapProviderPayment(root);

        applyProviderState(order, confirmedPayment, rawBody);
        orderRepository.save(order);
    }

    private void applyProviderState(MembershipPaymentOrder order, DlocalPaymentResponse payment, String rawPayload) {
        if (payment.id() != null) {
            order.setDlocalPaymentId(payment.id());
        }
        order.setStatus(payment.status());
        order.setStatusDetail(payment.statusDetail());
        if (payment.redirectUrl() != null) {
            order.setRedirectUrl(payment.redirectUrl());
        }
        if (rawPayload != null) {
            order.setRawProviderPayload(rawPayload);
        }

        if (STATUS_PAID.equalsIgnoreCase(payment.status())) {
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

    private MembershipPaymentOrder findOrder(String providerPaymentId, String orderId) {
        Optional<MembershipPaymentOrder> byProvider = providerPaymentId == null
                ? Optional.empty()
                : orderRepository.findByDlocalPaymentId(providerPaymentId);
        if (byProvider.isPresent()) {
            return byProvider.get();
        }
        return orderRepository.findByExternalId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("No existe una orden para el webhook recibido"));
    }

    private MembershipCatalogProperties.CountryCatalog getCountryCatalog(String countryCode) {
        MembershipCatalogProperties.CountryCatalog catalog = catalogProperties.getCatalog().get(countryCode);
        if (catalog == null || !catalog.isEnabled()) {
            throw new IllegalArgumentException("El país " + countryCode + " no está habilitado para pagos");
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
        BigDecimal localizedAmount = baseUsdAmount.multiply(exchangeRate).setScale(2, java.math.RoundingMode.HALF_UP);
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
        return appBaseUrl + "/payments/result";
    }

    private void requireDlocalEnabled() {
        if (!dlocalProperties.isEnabled()) {
            throw new IllegalStateException("dLocal no está habilitado. Configure las credenciales antes de usar pagos.");
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

    private DlocalPaymentResponse mapProviderPayment(JsonNode root) {
        return new DlocalPaymentResponse(
                text(root, "id"),
                root.path("amount").isNumber() ? root.path("amount").decimalValue() : null,
                text(root, "currency"),
                text(root, "country"),
                text(root, "status"),
                text(root, "status_detail"),
                text(root, "status_code"),
                text(root, "redirect_url"),
                text(root, "order_id")
        );
    }

    private JsonNode parseBody(String rawBody) {
        try {
            return objectMapper.readTree(rawBody);
        } catch (Exception exception) {
            throw new IllegalArgumentException("No se pudo parsear el webhook de dLocal", exception);
        }
    }

    private String text(JsonNode root, String field) {
        JsonNode node = root.get(field);
        return node == null || node.isNull() ? null : node.asText();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private CreateMembershipPaymentResponse toCreateResponse(MembershipPaymentOrder order) {
        return new CreateMembershipPaymentResponse(
                order.getExternalId(),
                order.getDlocalPaymentId(),
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
                order.getDlocalPaymentId(),
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
                order.getPaidAt()
        );
    }

    private record PlanQuote(
            BigDecimal baseUsdAmount,
            BigDecimal exchangeRateApplied,
            BigDecimal localizedAmount
    ) {
    }
}
