package com.ridehailing.authservice.otp;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OtpType {

    EMAIL_VERIFICATION(
            "email-verification",
            "Verify Your Email",
            """
            <div style="font-family:Arial,sans-serif">
                <h2>Email Verification</h2>
                <p>Your verification code is:</p>
                <h1>%s</h1>
                <p>This code will expire in 5 minutes.</p>
            </div>
            """
    ),

    PASSWORD_RESET(
            "password-reset",
            "Reset Your Password",
            """
            <div style="font-family:Arial,sans-serif">
                <h2>Password Reset</h2>
                <p>Your password reset code is:</p>
                <h1>%s</h1>
                <p>This code will expire in 5 minutes.</p>
            </div>
            """
    );

    private final String keyPrefix;
    private final String emailSubject;
    private final String template;
}