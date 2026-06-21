package com.ridehailing.authservice.otp;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OtpData {
    private final String otp;
    private final int attempts;
}