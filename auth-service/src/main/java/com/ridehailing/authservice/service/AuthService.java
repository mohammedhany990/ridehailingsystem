package com.ridehailing.authservice.service;

import com.ridehailing.authservice.common.ApiResponse;
import com.ridehailing.authservice.dto.*;
import com.ridehailing.authservice.entity.Role;
import com.ridehailing.authservice.entity.User;
import com.ridehailing.authservice.exception.*;
import com.ridehailing.authservice.otp.OtpService;
import com.ridehailing.authservice.otp.OtpType;
import com.ridehailing.authservice.repository.UserRepository;
import com.ridehailing.authservice.security.jwt.JwtTokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;

    @Transactional
    public ApiResponse<String> register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email is already in use");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .enabled(false)
                .build();

        User savedUser = userRepository.save(user);

        otpService.send(
                savedUser,
                OtpType.EMAIL_VERIFICATION
        );

        return ApiResponse.<String>builder()
                .success(true)
                .message("User registered successfully, an OTP has been sent to your email")
                .build();
    }


    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = getUserByEmail(request.getEmail());

        return buildAuthResponse(user);
    }

    public AuthResponse refreshToken(String refreshToken) {
        String email = jwtTokenService.extractUsername(refreshToken);

        User user = getUserByEmail(email);

        if (!jwtTokenService.isTokenValid(refreshToken, user)) {
            throw new InvalidRefreshToken("Invalid refresh token");
        }

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse loginWithOAuth2(String name, String email, String oauthId, String provider) {

        User user = userRepository.findByEmail(email)
                .map(existingUser -> {

                    if (existingUser.getOauthProvider() == null) {
                        existingUser.setOauthProvider(provider);
                        existingUser.setOauthId(oauthId);

                        return userRepository.save(existingUser);
                    }

                    return existingUser;
                })
                .orElseGet(() -> {

                    User newUser = User.builder()
                            .name(name)
                            .email(email)
                            .phone(null)
                            .oauthProvider(provider)
                            .oauthId(oauthId)
                            .role(Role.RIDER)
                            .enabled(true)
                            .build();

                    return userRepository.save(newUser);
                });

        return buildAuthResponse(user);
    }


    @Transactional
    public void verifyEmail(VerifyEmailRequest req) {

        User user = getUserByEmail(req.getEmail());

        otpService.verify(
                user,
                OtpType.EMAIL_VERIFICATION,
                req.getOtp()
        );

        user.setEnabled(true);

        userRepository.save(user);
    }

    public ApiResponse<String> resendVerificationOtp(ResendVerificationOtpRequest request) {

        User user = getUserByEmail(request.getEmail());

        if (user.isEnabled()) {
            throw new EmailVerifiedException("Email already verified");
        }

        otpService.send(
                user,
                OtpType.EMAIL_VERIFICATION
        );

        return ApiResponse.<String>builder()
                .success(true)
                .message("OTP sent successfully")
                .build();
    }

    public ApiResponse<String> sendResetPasswordOtp(ForgotPasswordRequest request) {

        userRepository.findByEmail(request.getEmail())
                .ifPresent(user ->
                        otpService.send(
                                user,
                                OtpType.PASSWORD_RESET
                        )
                );

        return ApiResponse.<String>builder()
                .success(true)
                .message("If the account exists, an OTP has been sent")
                .build();
    }

    @Transactional
    public ApiResponse<String> resetPassword(ResetPasswordRequest request) {

        User user = getUserByEmail(request.getEmail());

        otpService.verify(
                user,
                OtpType.PASSWORD_RESET,
                request.getOtp()
        );

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);

        return ApiResponse.<String>builder()
                .success(true)
                .message("Password reset successfully")
                .build();
    }


    private AuthResponse buildAuthResponse(User user) {

        String accessToken =
                jwtTokenService.generateToken(user);

        String refreshToken =
                jwtTokenService.generateRefreshToken(user);

        return AuthResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }


}
