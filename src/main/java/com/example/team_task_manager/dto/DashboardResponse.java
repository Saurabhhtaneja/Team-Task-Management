package com.example.team_task_manager.dto;


import lombok.*;
import java.util.List;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class DashboardResponse {
    private long totalProjects;
    private long totalTasks;
    private long todoTasks;
    private long inProgressTasks;
    private long inReviewTasks;
    private long doneTasks;
    private long overdueTasks;
    private List<TaskResponse> myAssignedTasks;
    private List<TaskResponse> overdueTaskList;
    private List<ProjectResponse> recentProjects;
}
