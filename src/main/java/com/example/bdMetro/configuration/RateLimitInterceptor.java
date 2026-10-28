package com.example.bdMetro.configuration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
        .expireAfterAccess(1, TimeUnit.HOURS)
        .maximumSize(10_000)
        .build();

    @Override
    public boolean preHandle(HttpServletRequest req,
                             HttpServletResponse res,
                             Object handler) throws Exception {
        String ip = getClientIp(req);
        Bucket bucket = buckets.get(ip, k ->
            Bucket.builder()
                .addLimit(Bandwidth.simple(30, Duration.ofMinutes(1)))
                .build()
        );
        if (bucket.tryConsume(1)) return true;

        res.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        res.setContentType("application/json");
        res.getWriter().write(
            "{\"status\":429,\"error\":\"Too Many Requests\"," +
            "\"message\":\"Demasiadas solicitudes. Intenta en 1 minuto.\"}"
        );
        return false;
    }

    private String getClientIp(HttpServletRequest req) {
        String forwarded = req.getHeader("X-Forwarded-For");
        return (forwarded != null) ? forwarded.split(",")[0].trim()
                                   : req.getRemoteAddr();
    }
}
