package com.ridehailing.authservice.otp;


import lombok.NoArgsConstructor;

import java.security.SecureRandom;

@NoArgsConstructor
public final class OtpGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final int OTP_LENGTH = 6;


    public static String generateOtp() {

        int bound = (int) Math.pow(10, OTP_LENGTH);

        int otp = SECURE_RANDOM.nextInt(bound);

        return String.format("%0" + OTP_LENGTH + "d", otp);
    }
}