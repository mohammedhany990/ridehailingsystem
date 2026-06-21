package com.ridehailing.authservice.otp;

import java.time.Duration;
import java.util.Optional;

public interface OtpStore {

    void saveOtp(String key, String hashedOtp, Duration ttl);

    Optional<OtpData> getOtp(String key);

    void incrementAttempts(String key);

    void delete(String key);

    boolean exists(String key);
}