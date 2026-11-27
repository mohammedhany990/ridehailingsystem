package com.ridehailing.apigateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

    /**
     * Rate limit based on the client's IP address.
     * Suitable for public endpoints such as:
     * - /api/auth/login
     * - /api/auth/register
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {

            HttpHeaders headers = exchange.getRequest().getHeaders();

            String forwardedFor = headers.getFirst("X-Forwarded-For");

            if (forwardedFor != null && !forwardedFor.isBlank()) {
                String clientIp = forwardedFor.split(",")[0].trim();
                return Mono.just(clientIp);
            }

            String clientIp = exchange.getRequest()
                    .getRemoteAddress() != null
                    ? exchange.getRequest()
                      .getRemoteAddress()
                      .getAddress()
                      .getHostAddress()
                    : "unknown";

            return Mono.just(clientIp);
        };
    }

    /**
     * Rate limit based on the authenticated user.
     * Suitable for protected APIs.
     */
    @Primary
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange ->
                exchange.getPrincipal()
                        .filter(JwtAuthenticationToken.class::isInstance)
                        .cast(JwtAuthenticationToken.class)
                        .map(auth -> auth.getToken().getSubject())
                        .defaultIfEmpty("anonymous");
    }
}