package com.ridehailing.authservice.controller;

import com.ridehailing.authservice.common.ApiResponse;
import com.ridehailing.authservice.dto.*;
import com.ridehailing.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(
        name = "Authentication",
        description = "Authentication APIs"
)
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register new user")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {

        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login using email and password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.<AuthResponse>builder()
                        .success(true)
                        .message("Login successful")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/google")
    @Operation(summary = "Login or register using a Google ID token")
    public ResponseEntity<ApiResponse<AuthResponse>> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {

        AuthResponse response = authService.loginWithGoogle(request.getIdToken());

        return ResponseEntity.ok(
                ApiResponse.<AuthResponse>builder()
                        .success(true)
                        .message("Login successful")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestHeader("Refresh-Token") String refreshToken    ) {

        AuthResponse response = authService.refreshToken(refreshToken);

        return ResponseEntity.ok(
                ApiResponse.<AuthResponse>builder()
                        .success(true)
                        .message("Token refreshed successfully")
                        .data(response)
                        .build()
        );
    }

//    @GetMapping("/oauth2/success")
//    @Operation(
//            summary = "OAuth2 login success callback"
//    )
//    public ResponseEntity<ApiResponse<AuthResponse>> oauth2Success(
//            @AuthenticationPrincipal
//            OAuth2User oauth2User
//    ) {
//
//        String email =
//                oauth2User.getAttribute("email");
//
//        String name =
//                oauth2User.getAttribute("name");
//
//        String oauthId =
//                oauth2User.getAttribute("sub");
//
//        AuthResponse response =
//                authService.loginWithOAuth2(
//                        name,
//                        email,
//                        oauthId,
//                        "google"
//                );
//
//        return ResponseEntity.ok(
//                ApiResponse.<AuthResponse>builder()
//                        .success(true)
//                        .message(
//                                "OAuth2 login successful"
//                        )
//                        .data(response)
//                        .build()
//        );
//    }

    @GetMapping("/validate")
    @Operation(summary = "Validate JWT token")
    public ResponseEntity<ApiResponse<String>> validate() {

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Token is valid")
                        .data("valid")
                        .build()
        );
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email using OTP")
    public ResponseEntity<ApiResponse<String>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {

        authService.verifyEmail(request);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Email verified successfully")
                        .build()
        );
    }

    @PostMapping("/resend-verification-otp")
    @Operation(summary = "Resend email verification OTP")
    public ResponseEntity<ApiResponse<String>> resendVerificationOtp(@Valid @RequestBody ResendVerificationOtpRequest request) {

        return ResponseEntity.ok(authService.resendVerificationOtp(request));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Send password reset OTP")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {

        return ResponseEntity.ok(
                authService.sendResetPasswordOtp(request)
        );
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using OTP")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {

        return ResponseEntity.ok(authService.resetPassword(request)
        );
    }
}