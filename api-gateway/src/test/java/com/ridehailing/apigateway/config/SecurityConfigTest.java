package com.ridehailing.apigateway.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    private SecretKey key;
    private ReactiveJwtDecoder decoder;

    @BeforeEach
    void setUp() {
        byte[] secret = new byte[48];
        for (int i = 0; i < secret.length; i++) secret[i] = (byte) (i + 1);
        String base64 = Base64.getEncoder().encodeToString(secret);
        key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64));
        decoder = NimbusReactiveJwtDecoder.withSecretKey(key).build();
    }

    @Test
    void decodesAValidTokenIssuedByAuthServiceShape() {
        String token = Jwts.builder()
                .setSubject("user-123")
                .claim("userId", "11111111-1111-1111-1111-111111111111")
                .claim("role", "RIDER")
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        var jwt = decoder.decode(token).block();

        assertThat(jwt).isNotNull();
        assertThat(jwt.getSubject()).isEqualTo("user-123");
        assertThat(jwt.getClaimAsString("role")).isEqualTo("RIDER");
    }

    @Test
    void rejectsAnExpiredToken() {
        String token = Jwts.builder()
                .setSubject("user-123")
                .setIssuedAt(Date.from(Instant.now().minusSeconds(7200)))
                .setExpiration(Date.from(Instant.now().minusSeconds(3600)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.security.oauth2.jwt.JwtException.class,
                () -> decoder.decode(token).block());
    }

    @Test
    void rejectsATokenSignedWithADifferentKey() {
        byte[] other = new byte[48];
        for (int i = 0; i < other.length; i++) other[i] = (byte) (i + 99);
        SecretKey otherKey = Keys.hmacShaKeyFor(other);
        String token = Jwts.builder()
                .setSubject("user-123")
                .setExpiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(otherKey, SignatureAlgorithm.HS256)
                .compact();

        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.security.oauth2.jwt.JwtException.class,
                () -> decoder.decode(token).block());
    }
}
