package com.example.team_task_manager.dto;


import com.example.team_task_manager.entity.Project.ProjectStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter @Setter
public class ProjectRequest {

    @NotBlank(message = "Project name is required")
    @Size(min = 2, max = 100, message = "Name must be 2–100 characters")
    private String name;

    @Size(max = 500, message = "Description max 500 characters")
    private String description;

    private ProjectStatus status;

    private LocalDateTime deadline;

    private Set<Long> memberIds;
}