package com.example.bdMetro.payments.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.bdMetro.payments.entity.PayPalPaymentOrder;

public interface PayPalPaymentOrderRepository extends JpaRepository<PayPalPaymentOrder, Long> {
    Optional<PayPalPaymentOrder> findByExternalId(String externalId);
    Optional<PayPalPaymentOrder> findByPaypalOrderId(String paypalOrderId);
}
