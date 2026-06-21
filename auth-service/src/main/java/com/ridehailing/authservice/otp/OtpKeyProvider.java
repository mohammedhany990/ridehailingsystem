package com.ridehailing.authservice.otp;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OtpKeyProvider {

    public String otpKey(OtpType type, UUID userId) {
        return type.getKeyPrefix() + ":otp:" + userId;
    }

    public String resendKey(OtpType type, UUID userId) {
        return type.getKeyPrefix() + ":resend:" + userId;
    }
}