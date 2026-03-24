package com.example.bdMetro.payments.service;

import java.nio.charset.StandardCharsets;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

import com.example.bdMetro.payments.config.DlocalProperties;

@Service
public class DlocalSignatureService {

    private final DlocalProperties properties;

    public DlocalSignatureService(DlocalProperties properties) {
        this.properties = properties;
    }

    public String buildAuthorizationHeader(String xDate, String requestBody) {
        return "V2-HMAC-SHA256, Signature: " + hexHmac(signaturePayload(xDate, requestBody));
    }

    public boolean isValidAuthorization(String authorizationHeader, String xDate, String requestBody) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return false;
        }
        return buildAuthorizationHeader(xDate, requestBody).equals(authorizationHeader.trim());
    }

    private byte[] signaturePayload(String xDate, String requestBody) {
        String body = requestBody == null ? "" : requestBody;
        String payload = properties.getXLogin() + xDate + body;
        return payload.getBytes(StandardCharsets.UTF_8);
    }

    private String hexHmac(byte[] payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(properties.getSecretKey().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(payload);
            StringBuilder builder = new StringBuilder(digest.length * 2);
            for (byte current : digest) {
                builder.append(String.format("%02x", current));
            }
            return builder.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo generar la firma de dLocal", exception);
        }
    }
}
