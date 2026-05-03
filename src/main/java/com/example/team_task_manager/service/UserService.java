package com.example.team_task_manager.service;


import com.example.team_task_manager.dto.UserResponse;
import com.example.team_task_manager.dto.UserResponse;
import com.example.team_task_manager.security.UserDetailsImpl;
import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers();
    UserResponse getUserById(Long id);
    UserResponse getCurrentUser(UserDetailsImpl userDetails);
    void deleteUser(Long id);
}
