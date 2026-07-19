package com.ridehailing.apigateway.config;


import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Configuration
public class RateLimiterConfig {
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
                Objects.requireNonNull(exchange.getRequest().getRemoteAddress())
                        .getAddress()
                        .getHostAddress()
        );
    }

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange ->
                exchange.getPrincipal()
                        .cast(JwtAuthenticationToken.class)
                        .map(auth -> auth.getToken().getSubject())
                        .defaultIfEmpty("anonymous");
    }
}