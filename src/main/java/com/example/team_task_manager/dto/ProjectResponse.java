package com.example.team_task_manager.dto;


import com.example.team_task_manager.entity.Project.ProjectStatus;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private ProjectStatus status;
    private UserResponse owner;
    private Set<UserResponse> members;
    private LocalDateTime deadline;
    private LocalDateTime createdAt;
    private long totalTasks;
    private long completedTasks;
    private long pendingTasks;
}