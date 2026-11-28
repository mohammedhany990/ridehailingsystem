package com.ridehailing.authservice.security.filter;

import com.ridehailing.authservice.security.jwt.JwtTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter filter;

    private final UserDetails userDetails = new User(
            "rider@e2e.test", "x", Collections.emptyList());

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private MockHttpServletRequest bearerRequest(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath(path);
        request.addHeader("Authorization", "Bearer token123");
        return request;
    }

    @Test
    void authenticatesProfileEndpointWhenBearerTokenValid() throws ServletException, IOException {
        when(jwtTokenService.extractUsername("token123")).thenReturn("rider@e2e.test");
        when(userDetailsService.loadUserByUsername("rider@e2e.test")).thenReturn(userDetails);
        when(jwtTokenService.isTokenValid("token123", userDetails)).thenReturn(true);

        filter.doFilterInternal(bearerRequest("/api/auth/profile/me"),
                new MockHttpServletResponse(), filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("rider@e2e.test",
                SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void authenticatesAvatarEndpointWhenBearerTokenValid() throws ServletException, IOException {
        when(jwtTokenService.extractUsername("token123")).thenReturn("rider@e2e.test");
        when(userDetailsService.loadUserByUsername("rider@e2e.test")).thenReturn(userDetails);
        when(jwtTokenService.isTokenValid("token123", userDetails)).thenReturn(true);

        filter.doFilterInternal(bearerRequest("/api/auth/profile/me/avatar"),
                new MockHttpServletResponse(), filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void skipsPublicLoginEndpoint() throws ServletException, IOException {
        lenient().when(jwtTokenService.extractUsername("token123")).thenReturn("rider@e2e.test");

        filter.doFilterInternal(bearerRequest("/api/auth/login"),
                new MockHttpServletResponse(), filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void skipsPublicRegisterEndpoint() throws ServletException, IOException {
        lenient().when(jwtTokenService.extractUsername("token123")).thenReturn("rider@e2e.test");

        filter.doFilterInternal(bearerRequest("/api/auth/register"),
                new MockHttpServletResponse(), filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void leavesContextEmptyWithoutBearerHeader() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/auth/profile/me");

        filter.doFilterInternal(request, new MockHttpServletResponse(), filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
