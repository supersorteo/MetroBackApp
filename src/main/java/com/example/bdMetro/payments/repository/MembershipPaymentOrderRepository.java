package com.example.bdMetro.payments.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.bdMetro.payments.entity.MembershipPaymentOrder;

public interface MembershipPaymentOrderRepository extends JpaRepository<MembershipPaymentOrder, Long> {

    Optional<MembershipPaymentOrder> findByExternalId(String externalId);

    Optional<MembershipPaymentOrder> findByDlocalPaymentId(String dlocalPaymentId);
}
