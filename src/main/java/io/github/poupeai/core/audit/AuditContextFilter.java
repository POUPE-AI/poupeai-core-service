package io.github.poupeai.core.audit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@Slf4j
public class AuditContextFilter extends OncePerRequestFilter {

    private static final String X_FORWARDED_FOR_HEADER = "X-Forwarded-For";
    private static final String USER_AGENT_HEADER = "User-Agent";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            populateAuditContext(request);
            filterChain.doFilter(request, response);
        } finally {
            AuditContext.clear();
        }
    }

    private void populateAuditContext(HttpServletRequest request) {
        try {
            String sourceIp = request.getHeader(X_FORWARDED_FOR_HEADER);
            if (sourceIp == null || sourceIp.isBlank()) {
                sourceIp = request.getRemoteAddr();
            } else {
                sourceIp = sourceIp.split(",")[0].trim();
            }
            AuditContext.setSourceIp(sourceIp);

            String userAgent = request.getHeader(USER_AGENT_HEADER);
            if (userAgent != null) {
                AuditContext.setUserAgent(userAgent.length() > 255 ? userAgent.substring(0, 255) : userAgent);
            }

            String traceId = MDC.get("traceId");
            if (traceId != null && !traceId.isBlank()) {
                try {
                    AuditContext.setCorrelationId(UUID.fromString(traceId));
                } catch (IllegalArgumentException e) {
                    log.debug("TraceId não é um UUID válido: {}", traceId);
                }
            }
        } catch (Exception e) {
            log.warn("Falha ao popular contexto de auditoria: {}", e.getMessage());
        }
    }
}
