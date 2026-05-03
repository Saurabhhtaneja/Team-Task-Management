package com.example.team_task_manager.dto;


import com.example.team_task_manager.entity.Task.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
public class TaskStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private TaskStatus status;
}