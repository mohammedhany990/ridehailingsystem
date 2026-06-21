package com.ridehailing.authservice.dto;


import com.ridehailing.authservice.entity.Role;
import com.ridehailing.authservice.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private UUID userId;
    private String email;
    private String name;
    private Role role;
}
