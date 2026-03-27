package com.example.bdMetro.payments.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.bdMetro.payments.dto.CreatePayPalPaymentRequest;
import com.example.bdMetro.payments.dto.CreatePayPalPaymentResponse;
import com.example.bdMetro.payments.dto.PayPalPaymentStatusResponse;
import com.example.bdMetro.payments.service.PayPalPaymentService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/payments/paypal")
public class PayPalPaymentController {

    private final PayPalPaymentService payPalPaymentService;

    public PayPalPaymentController(PayPalPaymentService payPalPaymentService) {
        this.payPalPaymentService = payPalPaymentService;
    }

    @PostMapping("/checkout")
    public CreatePayPalPaymentResponse createCheckout(@Valid @RequestBody CreatePayPalPaymentRequest request) {
        return payPalPaymentService.createCheckout(request);
    }

    @PostMapping("/capture")
    public PayPalPaymentStatusResponse capturePayment(
            @RequestParam(required = false) String externalId,
            @RequestParam(required = false) String token
    ) {
        return payPalPaymentService.capturePayment(externalId, token);
    }

    @GetMapping("/{externalId}")
    public PayPalPaymentStatusResponse getStatus(@PathVariable String externalId) {
        return payPalPaymentService.getOrderStatus(externalId);
    }
}
