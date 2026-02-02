package io.github.poupeai.core.audit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@Slf4j
public class AuditContextFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    private static final String MDC_TRACE_ID = "trace.correlation_id";
    private static final String MDC_USER_ID = "user.id";
    private static final String MDC_USER_NAME = "user.name";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String correlationId = request.getHeader(CORRELATION_ID_HEADER);
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = UUID.randomUUID().toString();
            }
            MDC.put(MDC_TRACE_ID, correlationId);

            try {
                AuditContext.setCorrelationId(UUID.fromString(correlationId));
            } catch (Exception e) {
                AuditContext.setCorrelationId(UUID.randomUUID());
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
                String subject = jwt.getClaimAsString("sub");
                String email = jwt.getClaimAsString("email");

                if (subject != null) MDC.put(MDC_USER_ID, subject);
                if (email != null) MDC.put(MDC_USER_NAME, email);
            }

            populateNetworkMetadata(request);

            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
            AuditContext.clear();
        }
    }

    private void populateNetworkMetadata(HttpServletRequest request) {
        String sourceIp = request.getHeader("X-Forwarded-For");
        if (sourceIp == null) sourceIp = request.getRemoteAddr();
        else sourceIp = sourceIp.split(",")[0].trim();

        AuditContext.setSourceIp(sourceIp);
        MDC.put("source.ip", sourceIp);

        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && userAgent.length() > 255) {
            userAgent = userAgent.substring(0, 255);
        }
        AuditContext.setUserAgent(userAgent);
        MDC.put("user_agent.original", userAgent);
    }
}
