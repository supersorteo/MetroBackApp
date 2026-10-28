# 3 mejoras de rendimiento para +1000 usuarios — 28/10/2026

## Contexto
Tras las 3 fases completadas (pool 50 conex, Caffeine cache, Flyway, rate limit), la app está en 6/10 para +1000 usuarios. Estas 3 mejoras son las de mayor ROI: máximo impacto, mínimo esfuerzo.

---

## Cambio 1 — `open-in-view=false` (1 línea)

**Archivo**: `src/main/resources/application.properties`

Agregar después de `spring.jpa.show-sql=false`:
```properties
spring.jpa.open-in-view=false
```

**Por qué**: Con `true` (default), Hibernate mantiene la sesión JPA abierta durante todo el ciclo HTTP, reteniendo una conexión del pool incluso después de que el `@Transactional` terminó. Con `false`, la conexión se devuelve al pool en cuanto finaliza la transacción.

---

## Cambio 2 — Compresión gzip (3 líneas)

**Archivo**: `src/main/resources/application-production.properties`

Agregar al final:
```properties
# Gzip
server.compression.enabled=true
server.compression.mime-types=application/json,application/xml,text/html,text/plain,application/javascript,text/css
server.compression.min-response-size=1024
```

**Por qué**: Reduce ~70% el ancho de banda en respuestas JSON (listas de tareas, presupuestos, codes). Solo afecta producción.

---

## Cambio 3 — Heartbeat con UPDATE directo (2 SQLs → 1)

`updateHeartbeat` hace `findById` + `save` = 2 round-trips SQL por heartbeat. Con 1000 usuarios × 1 heartbeat/min = 2000 SQLs/min. Un UPDATE directo lo reduce a la mitad.

**Archivo 1**: `src/main/java/com/example/bdMetro/repository/UserSessionRepository.java`

Agregar el método:
```java
@Modifying
@Query("UPDATE UserSession s SET s.lastHeartbeat = :now WHERE s.id = :id")
int touchHeartbeat(@Param("id") String id, @Param("now") java.time.Instant now);
```
Imports: `org.springframework.data.jpa.repository.Modifying`, `org.springframework.data.jpa.repository.Query`, `org.springframework.data.repository.query.Param`.

**Archivo 2**: `src/main/java/com/example/bdMetro/services/AuthenticationService.java`

Reemplazar el método `updateHeartbeat` (~línea 425):
```java
@Transactional
public boolean updateHeartbeat(String sessionToken) {
    return userSessionRepository.touchHeartbeat(sessionToken, Instant.now()) > 0;
}
```

---

## Verificación

1. Arrancar local — no debe haber `WARN HikariPool: ... open-in-view` en los logs
2. En Railway: header `Content-Encoding: gzip` en respuestas de `/api/auth/codes/{code}`
3. Llamar heartbeat con sessionToken válido → respuesta normal
4. Llamar heartbeat con sessionToken inexistente → `sessionKicked: true` (verifica el `> 0`)

---

## Puntuación esperada tras estos 3 cambios

| Área | Antes | Después |
|------|:-----:|:-------:|
| Escalabilidad | 7/10 | **8/10** |
| Calidad | 7.5/10 | **8/10** |
| +1000 usuarios | 6/10 | **8/10** |
