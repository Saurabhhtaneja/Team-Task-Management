package com.example.team_task_manager.service;

import com.example.team_task_manager.dto.LoginRequest;
import com.example.team_task_manager.dto.MessageResponse;
import com.example.team_task_manager.dto.SignupRequest;
import com.example.team_task_manager.dto.JwtResponse;
import com.example.team_task_manager.dto.MessageResponse;

public interface AuthService {
    JwtResponse login(LoginRequest loginRequest);
    MessageResponse signup(SignupRequest signupRequest);
}