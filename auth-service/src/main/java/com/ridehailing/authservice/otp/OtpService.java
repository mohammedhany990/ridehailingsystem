package com.ridehailing.authservice.otp;

import com.ridehailing.authservice.email.EmailDetails;
import com.ridehailing.authservice.email.EmailService;
import com.ridehailing.authservice.entity.User;
import com.ridehailing.authservice.exception.InvalidOtpException;
import com.ridehailing.authservice.exception.OtpExpiredException;
import com.ridehailing.authservice.exception.TooManyAttemptsException;
import com.ridehailing.authservice.exception.TooManyRequestsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpStore otpStore;
    private final OtpRateLimiter rateLimiter;
    private final EmailService emailService;
    private final OtpKeyProvider keyProvider;

    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private static final int MAX_ATTEMPTS = 5;

    public void send(User user, OtpType type) {

        String resendKey = keyProvider.resendKey(type, user.getId());

        if (rateLimiter.isBlocked(resendKey)) {
            throw new TooManyRequestsException("Please wait before requesting another OTP");
        }

        String otp = OtpGenerator.generateOtp();
        String hashedOtp = hash(otp, user.getId());

        otpStore.saveOtp(
                keyProvider.otpKey(type, user.getId()),
                hashedOtp,
                OTP_TTL
        );

        rateLimiter.markRequest(resendKey, Duration.ofSeconds(60));

        emailService.sendHtmlEmail(
                EmailDetails.builder()
                        .to(user.getEmail())
                        .subject(type.getEmailSubject())
                        .message(String.format(type.getTemplate(), otp))
                        .build()
        );

        log.info("OTP sent to {}", user.getEmail());
    }

    public void verify(User user, OtpType type, String submittedOtp) {

        String key = keyProvider.otpKey(type, user.getId());

        OtpData data = otpStore.getOtp(key)
                .orElseThrow(() -> new OtpExpiredException("OTP expired or not found"));

        if (data.getAttempts() >= MAX_ATTEMPTS) {
            cleanup(user, type);
            throw new TooManyAttemptsException("Too many attempts");
        }

        if (!data.getOtp().equals(hash(submittedOtp, user.getId()))) {
            otpStore.incrementAttempts(key);
            throw new InvalidOtpException("Invalid OTP");
        }

        cleanup(user, type);
    }

    private void cleanup(User user, OtpType type) {
        otpStore.delete(keyProvider.otpKey(type, user.getId()));
        otpStore.delete(keyProvider.resendKey(type, user.getId()));
    }

    private String hash(String otp, UUID userId) {
        return DigestUtils.sha256Hex(otp + userId);
    }
}