package com.ridehailing.authservice.controller;

import com.ridehailing.authservice.common.ApiResponse;
import com.ridehailing.authservice.dto.AdminStatsResponse;
import com.ridehailing.authservice.entity.Role;
import com.ridehailing.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<AdminStatsResponse>> stats() {
        AdminStatsResponse stats = new AdminStatsResponse(
                userRepository.count(),
                userRepository.countByRole(Role.RIDER),
                userRepository.countByRole(Role.DRIVER),
                userRepository.countByRole(Role.ADMIN),
                userRepository.countByEnabledTrue()
        );
        return ResponseEntity.ok(ApiResponse.success("Admin stats fetched", stats));
    }
}
