package com.example.bdMetro.payments.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateMembershipPaymentRequest(
        @NotBlank @Pattern(regexp = "^[A-Za-z]{2}$") String countryCode,
        @Min(3) @Max(12) Integer planMonths,
        @NotBlank @Size(max = 100) String payerName,
        @NotBlank @Email @Size(max = 120) String payerEmail,
        /* @NotBlank */ @Size(max = 40) String payerPhone,
        /* @NotBlank */ @Size(max = 40) String payerDocument,
        @NotBlank @Size(max = 120) String province,
        @Size(max = 500) String callbackUrl,
        @Size(max = 30) String paymentMethodId
) {
}
