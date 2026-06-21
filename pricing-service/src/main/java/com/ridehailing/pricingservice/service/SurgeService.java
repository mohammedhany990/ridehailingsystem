package com.ridehailing.pricingservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class SurgeService {

    private static final String SURGE_KEY_PREFIX = "pricing:surge:default";
    private static final BigDecimal DEFAULT_SURGE = BigDecimal.ONE;

    private final StringRedisTemplate redisTemplate;

    /**
     * Returns the effective surge multiplier for a given location.
     * In a production system this would lookup geo-zones. Here we use
     * a simple global key that can be updated via admin API.
     */
    public BigDecimal getSurgeMultiplier(double lat, double lng) {
        String raw = redisTemplate.opsForValue().get(SURGE_KEY_PREFIX);
        if (raw == null) {
            return DEFAULT_SURGE;
        }
        try {
            BigDecimal surge = new BigDecimal(raw);
            log.debug("Surge multiplier for ({},{}) = {}", lat, lng, surge);
            return surge;
        } catch (NumberFormatException e) {
            log.warn("Invalid surge value in Redis: {}", raw);
            return DEFAULT_SURGE;
        }
    }

    /** Admin: set a global surge multiplier (TTL = 1 hour). */
    public void setSurgeMultiplier(BigDecimal surge) {
        redisTemplate.opsForValue().set(SURGE_KEY_PREFIX, surge.toPlainString(), Duration.ofHours(1));
        log.info("Surge multiplier set to {}", surge);
    }
}
