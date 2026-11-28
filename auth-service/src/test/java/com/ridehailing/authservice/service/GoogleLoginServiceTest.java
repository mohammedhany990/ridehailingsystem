package com.ridehailing.authservice.service;

import com.ridehailing.authservice.dto.AuthResponse;
import com.ridehailing.authservice.entity.Role;
import com.ridehailing.authservice.entity.User;
import com.ridehailing.authservice.exception.GoogleLoginNotConfiguredException;
import com.ridehailing.authservice.exception.InvalidGoogleTokenException;
import com.ridehailing.authservice.oauth.GoogleTokenVerifier;
import com.ridehailing.authservice.oauth.GoogleUserInfo;
import com.ridehailing.authservice.otp.OtpService;
import com.ridehailing.authservice.repository.UserRepository;
import com.ridehailing.authservice.security.jwt.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleLoginServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenService jwtTokenService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private OtpService otpService;
    @Mock private GoogleTokenVerifier googleTokenVerifier;

    @InjectMocks private AuthService authService;

    @BeforeEach
    void stubTokens() {
        lenient().when(jwtTokenService.generateToken(any(User.class))).thenReturn("access-token");
        lenient().when(jwtTokenService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");
    }

    @Test
    void newGoogleUserIsCreatedAsEnabledRider() {
        GoogleUserInfo info = new GoogleUserInfo("sub-123", "rider@gmail.com", "Rider Name");
        when(googleTokenVerifier.verify("valid-token")).thenReturn(info);
        when(userRepository.findByEmail("rider@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        AuthResponse response = authService.loginWithGoogle("valid-token");

        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("rider@gmail.com", response.getEmail());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertEquals("google", saved.getOauthProvider());
        assertEquals("sub-123", saved.getOauthId());
        assertEquals(Role.RIDER, saved.getRole());
        assertTrue(saved.isEnabled());
    }

    @Test
    void existingUserWithoutProviderIsLinkedToGoogle() {
        GoogleUserInfo info = new GoogleUserInfo("sub-456", "member@example.com", "Member");
        User existing = User.builder()
                .id(UUID.randomUUID())
                .name("Member")
                .email("member@example.com")
                .role(Role.RIDER)
                .enabled(true)
                .build();
        when(googleTokenVerifier.verify("valid-token")).thenReturn(info);
        when(userRepository.findByEmail("member@example.com")).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse response = authService.loginWithGoogle("valid-token");

        assertEquals("access-token", response.getAccessToken());
        assertEquals("google", existing.getOauthProvider());
        assertEquals("sub-456", existing.getOauthId());
        verify(userRepository).save(existing);
    }

    @Test
    void existingGoogleUserLogsInWithoutChanges() {
        GoogleUserInfo info = new GoogleUserInfo("sub-789", "returning@gmail.com", "Returning");
        User existing = User.builder()
                .id(UUID.randomUUID())
                .name("Returning")
                .email("returning@gmail.com")
                .oauthProvider("google")
                .oauthId("sub-789")
                .role(Role.RIDER)
                .enabled(true)
                .build();
        when(googleTokenVerifier.verify("valid-token")).thenReturn(info);
        when(userRepository.findByEmail("returning@gmail.com")).thenReturn(Optional.of(existing));

        AuthResponse response = authService.loginWithGoogle("valid-token");

        assertEquals("access-token", response.getAccessToken());
        verify(userRepository, never()).save(any());
    }

    @Test
    void invalidTokenIsRejected() {
        when(googleTokenVerifier.verify("bad-token")).thenThrow(new InvalidGoogleTokenException("Invalid Google ID token"));

        assertThrows(InvalidGoogleTokenException.class, () -> authService.loginWithGoogle("bad-token"));
        verifyNoInteractions(userRepository);
    }

    @Test
    void unconfiguredServerIsRejected() {
        when(googleTokenVerifier.verify(anyString())).thenThrow(new GoogleLoginNotConfiguredException("Google login is not configured on the server"));

        assertThrows(GoogleLoginNotConfiguredException.class, () -> authService.loginWithGoogle("any-token"));
        verifyNoInteractions(userRepository);
    }
}
