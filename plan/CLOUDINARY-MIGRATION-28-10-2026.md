# Plan: Migración uploads → Cloudinary (eliminar volumen Railway)

## Contexto

Los logos de Empresa se guardan en un volumen local de Railway (`/app/uploads/`). Ese volumen bloquea la activación de réplicas. Cloudinary reemplaza el disco: las imágenes suben vía SDK, se sirven desde CDN, y Railway ya no necesita volumen.

- La entidad `Empresa.logoUrl` es un `String` genérico → acepta URLs de Cloudinary sin cambios en DB ni en schema
- Solo UNA entidad tiene imágenes: `Empresa.logoUrl`
- `LegacyCodeController` también recibe un MultipartFile pero es CSV/datos, no imagen → sin cambios ahí

---

## Archivos a modificar / crear

| Archivo | Acción |
|---------|--------|
| `pom.xml` | Agregar `cloudinary-http44:1.37.0` |
| `services/CloudinaryService.java` | NUEVO — upload, delete, extractPublicId |
| `controller/EmpresaClienteController.java` | Reemplazar File I/O por Cloudinary |
| `services/EmpresaService.java` | Eliminar checks/deletes de filesystem |
| `application.properties` | Agregar vars Cloudinary |
| `application-production.properties` | Idem para producción |
| `.env` | Agregar CLOUDINARY_* con credenciales locales |
| `.env-production` | Idem para producción |

---

## Cambio 1 — pom.xml

```xml
<dependency>
    <groupId>com.cloudinary</groupId>
    <artifactId>cloudinary-http44</artifactId>
    <version>1.37.0</version>
</dependency>
```

---

## Cambio 2 — CloudinaryService.java (nuevo)

`src/main/java/com/example/bdMetro/services/CloudinaryService.java`

```java
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

    public void delete(String cloudinaryUrl) {
        if (cloudinaryUrl == null || cloudinaryUrl.isBlank()) return;
        try {
            String publicId = extractPublicId(cloudinaryUrl);
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
        } catch (IOException e) {
            log.warn("[Cloudinary] No se pudo eliminar imagen: {}", cloudinaryUrl, e);
        }
    }

    public String extractPublicId(String url) {
        if (url == null || !url.contains("/upload/")) return url;
        String after = url.substring(url.indexOf("/upload/") + "/upload/".length());
        if (after.matches("v\\d+/.*")) {
            after = after.substring(after.indexOf("/") + 1);
        }
        int dot = after.lastIndexOf(".");
        return dot > 0 ? after.substring(0, dot) : after;
    }
}
```

---

## Cambio 3 — EmpresaClienteController.java

**3a. Agregar inyección de `CloudinaryService`** al constructor.

**3b. `POST /api/upload/image`** — reemplazar `Files.write()`:
```java
// ANTES:
String filename = userCode + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
Path filePath = Paths.get(getUploadDir()).resolve(filename);
Files.createDirectories(filePath.getParent());
Files.write(filePath, file.getBytes());
urls.add(appBaseUrl + "/api/uploads/" + filename);

// DESPUÉS:
String publicId = userCode + "_" + System.currentTimeMillis();
String url = cloudinaryService.upload(file.getBytes(), publicId);
urls.add(url);
```

**3c. `GET /api/uploads/{filename:.+}`** — ELIMINAR el método completo.
Cloudinary sirve las imágenes directamente desde su CDN; no hace falta proxy.

**3d. `DELETE /api/uploads/{filename:.+}`** — reemplazar el cuerpo:
```java
cloudinaryService.delete(filename);  // filename = URL completa de Cloudinary
return ResponseEntity.ok().build();
```
> El frontend pasará la URL completa de Cloudinary en el path param.

**3e. `DELETE /api/empresas/id/{id}`** — reemplazar delete de logo:
```java
// ANTES:
String fn = logoUrl.substring(logoUrl.lastIndexOf("/") + 1);
Files.deleteIfExists(Paths.get(getUploadDir()).resolve(fn));

// DESPUÉS:
cloudinaryService.delete(empresa.getLogoUrl());
```

**3f. Eliminar** el método privado `getUploadDir()` del controlador.

---

## Cambio 4 — EmpresaService.java

**4a. Eliminar** la verificación de existencia de archivo en `saveEmpresa()`:
```java
// ELIMINAR:
if (empresa.getLogoUrl() != null) {
    Path logoPath = Paths.get(getUploadDir()).resolve(extractFilename(empresa.getLogoUrl()));
    if (!Files.exists(logoPath)) { throw ...; }
}
```

**4b. Eliminar** `Files.deleteIfExists()` en `deleteEmpresa()` si existe.

**4c. Eliminar** el método `getUploadDir()` de `EmpresaService`.

---

## Cambio 5 — Properties y .env

**`application.properties`** — agregar:
```properties
# Cloudinary
cloudinary.cloud-name=${CLOUDINARY_CLOUD_NAME}
cloudinary.api-key=${CLOUDINARY_API_KEY}
cloudinary.api-secret=${CLOUDINARY_API_SECRET}
```

**`application-production.properties`** — idem.

**`.env`** (local, gitignored):
```
CLOUDINARY_CLOUD_NAME=tu_cloud_name_aqui
CLOUDINARY_API_KEY=tu_api_key_aqui
CLOUDINARY_API_SECRET=tu_api_secret_aqui
```

**`.env-production`** — idem con valores reales de producción.

---

## Variables a agregar en Railway

```
CLOUDINARY_CLOUD_NAME=xxx
CLOUDINARY_API_KEY=xxx
CLOUDINARY_API_SECRET=xxx
```

---

## Pasos post-deploy

1. Eliminar el volumen del servicio en Railway → se desbloquea "Réplicas"
2. Poner Replicas en **2**

---

## Logos existentes en DB

Los `logoUrl` que apunten a `/api/uploads/xxx` quedarán rotos (el endpoint fue eliminado). Los usuarios deberán re-subir su logo. Sin usuarios reales aún → aceptable.

---

## Verificación

1. Arrancar local → subir logo → URL retornada empieza con `https://res.cloudinary.com/`
2. Verificar imagen en dashboard de Cloudinary bajo `metro/logos/`
3. Eliminar empresa → imagen desaparece de Cloudinary
4. Deploy a Railway → mismo flujo en producción
5. Eliminar volumen en Railway → activar Réplicas → poner en 2
