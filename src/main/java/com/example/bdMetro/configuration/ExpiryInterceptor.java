package com.example.bdMetro.configuration;

import com.example.bdMetro.entity.AccessCode;
import com.example.bdMetro.repository.AccessCodeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

@Component
public class ExpiryInterceptor implements HandlerInterceptor {

    private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");
    private final AccessCodeRepository accessCodeRepository;
    private final ObjectMapper objectMapper;

    public ExpiryInterceptor(AccessCodeRepository accessCodeRepository, ObjectMapper objectMapper) {
        this.accessCodeRepository = accessCodeRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!WRITE_METHODS.contains(request.getMethod())) {
            return true;
        }

        String userCode = request.getHeader("X-User-Code");
        if (userCode == null || userCode.isBlank()) {
            return true;
        }

        AccessCode ac = accessCodeRepository.findByCode(userCode);
        if (ac == null || ac.getFechaVencimiento() == null) {
            return true;
        }

        if (ac.getFechaVencimiento().isBefore(LocalDate.now())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                objectMapper.writeValueAsString(Map.of("error", "Código vencido. Reactivá tu membresía para continuar."))
            );
            return false;
        }

        return true;
    }
}
