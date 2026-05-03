package com.example.team_task_manager.service;


import com.example.team_task_manager.dto.TaskRequest;
import com.example.team_task_manager.dto.TaskStatusUpdateRequest;
import com.example.team_task_manager.dto.TaskResponse;
import com.example.team_task_manager.security.UserDetailsImpl;
import java.util.List;

public interface TaskService {
    TaskResponse createTask(TaskRequest request, UserDetailsImpl currentUser);
    TaskResponse updateTask(Long id, TaskRequest request, UserDetailsImpl currentUser);
    TaskResponse updateTaskStatus(Long id, TaskStatusUpdateRequest request, UserDetailsImpl currentUser);
    TaskResponse getTaskById(Long id, UserDetailsImpl currentUser);
    List<TaskResponse> getTasksByProject(Long projectId, UserDetailsImpl currentUser);
    List<TaskResponse> getMyTasks(UserDetailsImpl currentUser);
    void deleteTask(Long id, UserDetailsImpl currentUser);
}
