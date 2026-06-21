package com.ridehailing.authservice.dto;

import com.ridehailing.authservice.entity.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    String name;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    String email;

    @NotBlank(message = "Phone is required")
    String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String password;

    @NotNull(message = "Role is required")
    Role role;

}
