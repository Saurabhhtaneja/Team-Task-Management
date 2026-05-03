package com.example.team_task_manager.service;

import com.example.team_task_manager.dto.ProjectRequest;
import com.example.team_task_manager.dto.ProjectResponse;
import com.example.team_task_manager.security.UserDetailsImpl;
import java.util.List;

public interface ProjectService {
    ProjectResponse createProject(ProjectRequest request, UserDetailsImpl currentUser);
    ProjectResponse updateProject(Long id, ProjectRequest request, UserDetailsImpl currentUser);
    ProjectResponse getProjectById(Long id, UserDetailsImpl currentUser);
    List<ProjectResponse> getAllProjects(UserDetailsImpl currentUser);
    void deleteProject(Long id, UserDetailsImpl currentUser);
    ProjectResponse addMember(Long projectId, Long userId, UserDetailsImpl currentUser);
    ProjectResponse removeMember(Long projectId, Long userId, UserDetailsImpl currentUser);
}