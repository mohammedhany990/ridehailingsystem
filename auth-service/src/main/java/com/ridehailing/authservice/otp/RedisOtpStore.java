package com.ridehailing.authservice.otp;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RedisOtpStore implements OtpStore {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String FIELD_OTP = "otp";
    private static final String FIELD_ATTEMPTS = "attempts";

    @Override
    public void saveOtp(String key, String hashedOtp, Duration ttl) {
        redisTemplate.opsForHash().put(key, FIELD_OTP, hashedOtp);
        redisTemplate.opsForHash().put(key, FIELD_ATTEMPTS, 0);
        redisTemplate.expire(key, ttl);
    }

    @Override
    public Optional<OtpData> getOtp(String key) {
        Object otp = redisTemplate.opsForHash().get(key, FIELD_OTP);
        Object attempts = redisTemplate.opsForHash().get(key, FIELD_ATTEMPTS);

        if (otp == null || attempts == null) {
            return Optional.empty();
        }

        return Optional.of(new OtpData(
                otp.toString(),
                Integer.parseInt(attempts.toString())
        ));
    }

    @Override
    public void incrementAttempts(String key) {
        redisTemplate.opsForHash().increment(key, FIELD_ATTEMPTS, 1);
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}