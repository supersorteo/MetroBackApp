# Próximos pasos — test local + push + mejoras opcionales (28/10/2026)

## Estado actual — cambios ya aplicados hoy

| Cambio | Archivo |
|--------|---------|
| `open-in-view=false` | `application.properties` |
| Gzip en producción | `application-production.properties` |
| Heartbeat: 2 SQLs → 1 UPDATE directo | `UserSessionRepository.java` + `AuthenticationService.java` |
| Flyway deshabilitado en local | `application.properties` |

---

## Paso 1 — Verificar local

Arrancar la app y confirmar en los logs:
- No hay error de Flyway
- No hay `WARN spring.jpa.open-in-view`
- `GET /api/auth/codes/{code}` con sessionToken válido → respuesta normal
- `GET /api/auth/codes/{code}` con sessionToken inválido → `sessionKicked: true`

---

## Paso 2 — Push a Railway

```bash
git add src/main/resources/application.properties
git add src/main/resources/application-production.properties
git add src/main/java/com/example/bdMetro/repository/UserSessionRepository.java
git add src/main/java/com/example/bdMetro/services/AuthenticationService.java
git commit -m "perf: open-in-view=false, gzip, heartbeat UPDATE directo, flyway disabled local"
git push
```

Verificar en Railway después del deploy:
- App arranca sin errores
- Respuestas JSON traen header `Content-Encoding: gzip`

---

## Paso 3 (Opcional) — `@Async` en webhook de MercadoPago

**Problema**: `MembershipPaymentController` llama `processMercadoPagoWebhook` de forma sincrónica. Si MP tarda en responder, el thread de Tomcat queda bloqueado.

### Nuevo archivo: `payments/service/WebhookAsyncDispatcher.java`
```java
@Component
public class WebhookAsyncDispatcher {
    private final MembershipPaymentService service;
    public WebhookAsyncDispatcher(MembershipPaymentService service) { this.service = service; }

    @Async
    public void dispatchMercadoPago(String xSig, String xReqId, String dataId, String type, String body) {
        service.processMercadoPagoWebhook(xSig, xReqId, dataId, type, body);
    }
}
```

### Cambio en `CacheConfig.java` — agregar `@EnableAsync` a la clase existente.

### Cambio en `MembershipPaymentController.java` (~línea 60):
```java
// Reemplazar:
membershipPaymentService.processMercadoPagoWebhook(xSignature, xRequestId, dataId, type, rawBody);
// Con:
webhookAsyncDispatcher.dispatchMercadoPago(xSignature, xRequestId, dataId, type, rawBody);
```
Y agregar inyección de `WebhookAsyncDispatcher` en el constructor del controller.

---

## Puntuación esperada al terminar todo

| Área | Hoy (3 fases) | Tras estos cambios |
|------|:-------------:|:-----------------:|
| Escalabilidad | 7/10 | **8/10** |
| Calidad | 7.5/10 | **8/10** |
| +1000 usuarios | 6/10 | **8/10** |
