# Paso 5 — Timeouts clientes externos + Paso 7 — Railway replicas (28/10/2026)

## Contexto
Los clientes REST de MercadoPago y PayPal usan Apache HttpClient sin timeouts. Si esos servicios tardan, el thread de Tomcat queda bloqueado indefinidamente (~2 min por default del OS). Con 1000 usuarios, un par de pagos lentos agotan el pool.

`FxRateService` ya tiene fallback propio — no necesita cambios.

---

## Paso 5 — Timeouts en MercadoPago y PayPal

**Archivo**: `src/main/java/com/example/bdMetro/payments/config/PaymentsConfig.java`

Reemplazar el método `apacheFactory()`:
```java
private HttpComponentsClientHttpRequestFactory apacheFactory() {
    RequestConfig requestConfig = RequestConfig.custom()
        .setConnectionRequestTimeout(Timeout.ofMilliseconds(3000))
        .setResponseTimeout(Timeout.ofMilliseconds(10000))
        .build();
    CloseableHttpClient httpClient = HttpClients.custom()
        .setDefaultRequestConfig(requestConfig)
        .build();
    return new HttpComponentsClientHttpRequestFactory(httpClient);
}
```

Imports adicionales (los demás ya existen):
```java
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.util.Timeout;
```

**Sin dependencias nuevas** — `httpclient5` ya está en `pom.xml`.

**Qué pasa con timeout**:
- Checkout MP/PP lento: error en < 11s → 500 al usuario (mejor que 2 min colgado)
- Webhook MP (async): timeout capturado por `WebhookAsyncDispatcher` → log de error → MP reintenta

---

## Paso 7 — Railway replicas (sin Redis)

### Riesgo con caches Caffeine in-memory

Con 2 instancias, `@CacheEvict` en una NO invalida el cache de la otra.

| Cache | TTL actual | Riesgo |
|-------|-----------|--------|
| `accessCodes` | 60s | Código deshabilitado sirve 60s más — bajo |
| `tareas` | 10min | Tarea nueva no visible 10min en otra instancia — medio |
| `provincias` | 60min | Datos estáticos — sin riesgo |

### Cambio en `CacheConfig.java`:
```java
manager.registerCustomCache("tareas",
    Caffeine.newBuilder()
        .expireAfterWrite(2, TimeUnit.MINUTES)   // era 10 MINUTES
        .maximumSize(50)
        .build());
```

### Configurar replicas en Railway (sin código):
Railway Dashboard → servicio bdMetro → Settings → Replicas → **2**

---

## Archivos a modificar

| Archivo | Cambio |
|---------|--------|
| `payments/config/PaymentsConfig.java` | Timeout 3s/10s al HttpClient |
| `configuration/CacheConfig.java` | TTL `tareas` 10min → 2min |

## Verificación

- Local: checkout normal funciona igual; si MP no responde, error en < 11s
- Railway con 2 replicas: login funciona, tarea nueva visible en ≤ 2min en ambas instancias
