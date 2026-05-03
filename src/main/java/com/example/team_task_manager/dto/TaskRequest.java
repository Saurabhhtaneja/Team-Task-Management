package com.example.team_task_manager.dto;


import com.example.team_task_manager.entity.Task.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
public class TaskRequest {

    @NotBlank(message = "Task title is required")
    @Size(min = 2, max = 200, message = "Title must be 2–200 characters")
    private String title;

    @Size(max = 1000)
    private String description;

    private TaskStatus status;

    private TaskPriority priority;

    @NotNull(message = "Project ID is required")
    private Long projectId;

    private Long assigneeId;

    private LocalDateTime dueDate;
}