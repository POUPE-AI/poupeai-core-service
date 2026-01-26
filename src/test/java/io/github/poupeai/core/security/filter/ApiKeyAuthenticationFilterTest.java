package io.github.poupeai.core.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiKeyAuthenticationFilterTest {

    private ApiKeyAuthenticationFilter filter;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    private final String HEADER_NAME = "X-API-KEY";
    private final String SECRET_KEY = "minha-chave-secreta-123";

    @BeforeEach
    void setUp() {
        filter = new ApiKeyAuthenticationFilter(HEADER_NAME, SECRET_KEY);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should authenticate with ROLE_SYSTEM when the key is valid")
    void doFilterInternalShouldAuthenticateWhenKeyIsValid() throws ServletException, IOException {
        when(request.getHeader(HEADER_NAME)).thenReturn(SECRET_KEY);

        filter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(auth);
        assertTrue(auth.isAuthenticated());

        assertEquals("system-internal", auth.getPrincipal());

        boolean hasRoleSystem = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SYSTEM"));
        assertTrue(hasRoleSystem);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should not authenticate if the key is invalid")
    void doFilterInternalShouldNotAuthenticateWhenKeyIsInvalid() throws ServletException, IOException {
        when(request.getHeader(HEADER_NAME)).thenReturn("chave-errada");

        filter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should not authenticate when the header is null")
    void doFilterInternal_ShouldNotAuthenticate_WhenHeaderIsNull() throws ServletException, IOException {
        when(request.getHeader(HEADER_NAME)).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth);

        verify(filterChain).doFilter(request, response);
    }
}
