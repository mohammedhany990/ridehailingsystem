package com.ridehailing.authservice.controller;

import com.ridehailing.authservice.common.ApiResponse;
import com.ridehailing.authservice.dto.AdminStatsResponse;
import com.ridehailing.authservice.entity.Role;
import com.ridehailing.authservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminStatsControllerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminStatsController controller;

    @Test
    void statsReturnsCountsByRole() {
        when(userRepository.count()).thenReturn(12L);
        when(userRepository.countByRole(Role.RIDER)).thenReturn(7L);
        when(userRepository.countByRole(Role.DRIVER)).thenReturn(4L);
        when(userRepository.countByRole(Role.ADMIN)).thenReturn(1L);
        when(userRepository.countByEnabledTrue()).thenReturn(10L);

        var response = controller.stats();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<AdminStatsResponse> body = response.getBody();
        AdminStatsResponse stats = body.getData();
        assertEquals(12L, stats.totalUsers());
        assertEquals(7L, stats.riders());
        assertEquals(4L, stats.drivers());
        assertEquals(1L, stats.admins());
        assertEquals(10L, stats.activeUsers());
    }
}
