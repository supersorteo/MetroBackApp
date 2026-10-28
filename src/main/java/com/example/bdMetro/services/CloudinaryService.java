package com.example.bdMetro.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryService.class);
    private final Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true));
    }

    public String upload(byte[] bytes, String publicId) {
        try {
            Map result = cloudinary.uploader().upload(bytes, ObjectUtils.asMap(
                    "public_id", publicId,
                    "folder", "metro/logos",
                    "resource_type", "image",
                    "overwrite", true));
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("Error subiendo imagen a Cloudinary: " + e.getMessage(), e);
        }
    }

    public void delete(String input) {
        if (input == null || input.isBlank()) return;
        try {
            String publicId = extractPublicId(input);
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
        } catch (IOException e) {
            log.warn("[Cloudinary] No se pudo eliminar imagen publicId derivado de '{}': {}", input, e.getMessage());
        }
    }

    public String extractPublicId(String input) {
        if (input == null) return input;
        if (input.contains("/upload/")) {
            // URL completa de Cloudinary
            String after = input.substring(input.indexOf("/upload/") + "/upload/".length());
            if (after.matches("v\\d+/.*")) {
                after = after.substring(after.indexOf("/") + 1);
            }
            int dot = after.lastIndexOf(".");
            return dot > 0 ? after.substring(0, dot) : after;
        } else {
            // Solo nombre de archivo (ej: "userCode_123456.jpg") — reconstruir public_id
            int dot = input.lastIndexOf(".");
            String nameWithoutExt = dot > 0 ? input.substring(0, dot) : input;
            return "metro/logos/" + nameWithoutExt;
        }
    }
}
