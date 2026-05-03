package com.example.team_task_manager.controller;


import com.example.team_task_manager.dto.DashboardResponse;
import com.example.team_task_manager.security.UserDetailsImpl;
import com.example.team_task_manager.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Dashboard", description = "Dashboard and analytics endpoint")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @Operation(summary = "Get dashboard stats for current user")
    public ResponseEntity<DashboardResponse> getDashboard(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(dashboardService.getDashboard(currentUser));
    }
}