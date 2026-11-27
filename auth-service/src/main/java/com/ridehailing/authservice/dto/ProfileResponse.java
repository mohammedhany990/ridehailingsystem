package com.ridehailing.authservice.dto;

import com.ridehailing.authservice.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ProfileResponse {

    private UUID id;
    private String name;
    private String email;
    private String phone;
    private Double rating;
    private String role;
    private String avatarUrl;
    private LocalDateTime createdAt;

    public static ProfileResponse from(User user) {
        return ProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .rating(user.getRating())
                .role(user.getRole().name())
                .avatarUrl(user.getAvatarFilename() != null
                        ? "/api/auth/profile/avatar/" + user.getId()
                        : null)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
