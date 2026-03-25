package com.example.bdMetro.payments.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

import com.example.bdMetro.payments.config.MercadoPagoProperties;

@Service
public class MercadoPagoWebhookSignatureService {

    private final MercadoPagoProperties properties;

    public MercadoPagoWebhookSignatureService(MercadoPagoProperties properties) {
        this.properties = properties;
    }

    public boolean isValid(String xSignature, String xRequestId, String dataId) {
        if (isBlank(properties.getWebhookSecret()) || isBlank(xSignature) || isBlank(xRequestId) || isBlank(dataId)) {
            return false;
        }

        String ts = null;
        String v1 = null;
        for (String part : xSignature.split(",")) {
            String[] tokens = part.trim().split("=", 2);
            if (tokens.length != 2) {
                continue;
            }
            if ("ts".equals(tokens[0])) {
                ts = tokens[1];
            } else if ("v1".equals(tokens[0])) {
                v1 = tokens[1];
            }
        }

        if (isBlank(ts) || isBlank(v1)) {
            return false;
        }

        String manifest = "id:" + dataId.toLowerCase(Locale.ROOT)
                + ";request-id:" + xRequestId
                + ";ts:" + ts + ";";

        String expected = hmacSha256Hex(manifest, properties.getWebhookSecret());
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                v1.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String hmacSha256Hex(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signature = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte current : signature) {
                hex.append(String.format("%02x", current));
            }
            return hex.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo validar la firma de Mercado Pago", exception);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
