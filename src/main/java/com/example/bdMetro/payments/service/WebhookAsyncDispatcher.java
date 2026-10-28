package com.example.bdMetro.payments.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class WebhookAsyncDispatcher {

    private static final Logger log = LoggerFactory.getLogger(WebhookAsyncDispatcher.class);

    private final MembershipPaymentService membershipPaymentService;

    public WebhookAsyncDispatcher(MembershipPaymentService membershipPaymentService) {
        this.membershipPaymentService = membershipPaymentService;
    }

    @Async
    public void dispatchMercadoPago(String xSignature, String xRequestId,
                                    String dataId, String type, String rawBody) {
        try {
            membershipPaymentService.processMercadoPagoWebhook(xSignature, xRequestId, dataId, type, rawBody);
        } catch (Exception e) {
            log.error("[MP-WEBHOOK] Error procesando pago dataId={} type={}: {}", dataId, type, e.getMessage(), e);
        }
    }
}
