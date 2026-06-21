package com.ridehailing.authservice.otp;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisOtpRateLimiter implements OtpRateLimiter {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean isBlocked(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public void markRequest(String key, Duration ttl) {
        redisTemplate.opsForValue().set(key, "1", ttl);
    }
}
