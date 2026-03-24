package com.example.bdMetro.payments.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bdMetro.payments.dto.CreateMembershipPaymentRequest;
import com.example.bdMetro.payments.dto.CreateMembershipPaymentResponse;
import com.example.bdMetro.payments.dto.MembershipCatalogResponse;
import com.example.bdMetro.payments.dto.MembershipPaymentStatusResponse;
import com.example.bdMetro.payments.service.MembershipPaymentService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/payments/memberships")
public class MembershipPaymentController {

    private final MembershipPaymentService membershipPaymentService;

    public MembershipPaymentController(MembershipPaymentService membershipPaymentService) {
        this.membershipPaymentService = membershipPaymentService;
    }

    @GetMapping("/catalog")
    public MembershipCatalogResponse getCatalog() {
        return membershipPaymentService.getCatalog();
    }

    @PostMapping("/checkout")
    public CreateMembershipPaymentResponse createCheckout(@Valid @RequestBody CreateMembershipPaymentRequest request) {
        return membershipPaymentService.createCheckout(request);
    }

    @GetMapping("/{externalId}")
    public MembershipPaymentStatusResponse getStatus(@PathVariable String externalId) {
        return membershipPaymentService.getOrderStatus(externalId);
    }

    @PostMapping(value = "/dlocal/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> receiveWebhook(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("X-Date") String xDate,
            @RequestBody String rawBody
    ) {
        membershipPaymentService.processWebhook(authorization, xDate, rawBody);
        return ResponseEntity.ok().build();
    }
}
