package com.ridehailing.authservice.controller;

import com.ridehailing.authservice.common.ApiResponse;
import com.ridehailing.authservice.dto.ProfileResponse;
import com.ridehailing.authservice.dto.UpdateProfileRequest;
import com.ridehailing.authservice.exception.AuthenticationRequiredException;
import com.ridehailing.authservice.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/auth/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "User profile management")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    @Operation(summary = "Get the current user's profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> me(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(
                "Profile retrieved", profileService.getProfile(emailOf(principal))));
    }

    @PutMapping("/me")
    @Operation(summary = "Update name and phone")
    public ResponseEntity<ApiResponse<ProfileResponse>> update(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Profile updated", profileService.updateProfile(emailOf(principal), request)));
    }

    @PostMapping("/me/avatar")
    @Operation(summary = "Upload a profile photo (JPG/PNG/WEBP, max 5 MB)")
    public ResponseEntity<ApiResponse<ProfileResponse>> uploadAvatar(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success(
                "Avatar updated", profileService.saveAvatar(emailOf(principal), file)));
    }

    @GetMapping("/avatar/{userId}")
    @Operation(summary = "Get a user's avatar image")
    public ResponseEntity<Resource> avatar(@PathVariable UUID userId) {
        ProfileService.AvatarResource avatar = profileService.loadAvatar(userId);
        return ResponseEntity.ok()
                .contentType(avatar.mediaType())
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePrivate())
                .body(avatar.resource());
    }

    private String emailOf(UserDetails principal) {
        if (principal == null) {
            throw new AuthenticationRequiredException("Authentication required");
        }
        return principal.getUsername();
    }
}
