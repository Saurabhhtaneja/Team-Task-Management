package com.example.team_task_manager.dto;


import com.example.team_task_manager.entity.Task.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private Long projectId;
    private String projectName;
    private UserResponse assignee;
    private UserResponse createdBy;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private boolean overdue;
}
