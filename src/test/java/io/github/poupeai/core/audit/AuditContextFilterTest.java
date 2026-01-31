package io.github.poupeai.core.audit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuditContextFilterTest {

    private AuditContextFilter filter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new AuditContextFilter();
    }

    @AfterEach
    void tearDown() {
        AuditContext.clear();
        MDC.clear();
    }

    @Test
    @DisplayName("Should extract source IP from X-Forwarded-For header")
    void shouldExtractForwardedForHeader() throws Exception {
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.100, 10.0.0.1");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should use remote addr when X-Forwarded-For is not present")
    void shouldUseRemoteAddrWhenNoForwardedFor() throws Exception {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should truncate long User-Agent headers")
    void shouldTruncateLongUserAgent() throws Exception {
        String longUserAgent = "A".repeat(300);
        when(request.getHeader("User-Agent")).thenReturn(longUserAgent);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should clear context after filter chain completes")
    void shouldClearContextAfterFilterChain() throws Exception {
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        filter.doFilterInternal(request, response, filterChain);

        assertNull(AuditContext.getSourceIp());
        assertNull(AuditContext.getCorrelationId());
    }

    @Test
    @DisplayName("Should clear context even when exception occurs in filter chain")
    void shouldClearContextOnException() throws Exception {
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        doThrow(new RuntimeException("Test exception")).when(filterChain).doFilter(request, response);

        assertThrows(RuntimeException.class, () -> filter.doFilterInternal(request, response, filterChain));

        assertNull(AuditContext.getSourceIp());
        assertNull(AuditContext.getCorrelationId());
    }
}
