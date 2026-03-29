package com.example.bdMetro.util;

import java.util.Locale;

public final class CountryCatalog {

    private CountryCatalog() {
    }

    public static String normalizeCode(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            return normalized;
        }

        return switch (normalized) {
            case "ar", "argentina" -> "AR";
            case "uy", "uruguay" -> "UY";
            case "co", "colombia" -> "CO";
            default -> normalized.toUpperCase(Locale.ROOT);
        };
    }

    public static String displayName(String value) {
        String code = normalizeCode(value);
        if (code == null || code.isBlank()) {
            return code;
        }

        return switch (code) {
            case "AR" -> "Argentina";
            case "UY" -> "Uruguay";
            case "CO" -> "Colombia";
            default -> code;
        };
    }

    public static String adminKey(String value) {
        String code = normalizeCode(value);
        if (code == null || code.isBlank()) {
            return code;
        }

        return switch (code) {
            case "AR" -> "argentina";
            case "UY" -> "uruguay";
            case "CO" -> "colombia";
            default -> code.toLowerCase(Locale.ROOT);
        };
    }
}
