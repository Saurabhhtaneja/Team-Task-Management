package com.example.team_task_manager.service;



import com.example.team_task_manager.dto.DashboardResponse;
import com.example.team_task_manager.security.UserDetailsImpl;

public interface DashboardService {
    DashboardResponse getDashboard(UserDetailsImpl currentUser);
}
