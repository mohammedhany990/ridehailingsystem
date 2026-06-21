package com.ridehailing.authservice.otp;

import java.time.Duration;

public interface OtpRateLimiter {
    boolean isBlocked(String key);
    void markRequest(String key, Duration ttl);
}