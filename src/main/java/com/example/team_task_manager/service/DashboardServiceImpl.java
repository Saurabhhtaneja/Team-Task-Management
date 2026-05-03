package com.example.team_task_manager.service;


import com.example.team_task_manager.dto.DashboardResponse;
import com.example.team_task_manager.entity.*;
import com.example.team_task_manager.entity.Task.TaskStatus;
import com.example.team_task_manager.repository.*;
import com.example.team_task_manager.security.UserDetailsImpl;
import com.example.team_task_manager.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskServiceImpl taskService;
    private final ProjectServiceImpl projectService;

    @Override
    public DashboardResponse getDashboard(UserDetailsImpl currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow();

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        List<Project> projects = isAdmin
                ? projectRepository.findAll()
                : projectRepository.findAllProjectsForUser(user);

        List<Task> allTasks = isAdmin
                ? taskRepository.findAll()
                : taskRepository.findAllTasksVisibleToUser(user);

        List<Task> myTasks   = taskRepository.findByAssignee(user);
        List<Task> overdue   = isAdmin
                ? taskRepository.findAllOverdueTasks(LocalDateTime.now())
                : taskRepository.findOverdueTasksForUser(user, LocalDateTime.now());

        return DashboardResponse.builder()
                .totalProjects(projects.size())
                .totalTasks(allTasks.size())
                .todoTasks(allTasks.stream().filter(t -> t.getStatus() == TaskStatus.TODO).count())
                .inProgressTasks(allTasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count())
                .inReviewTasks(allTasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_REVIEW).count())
                .doneTasks(allTasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count())
                .overdueTasks(overdue.size())
                .myAssignedTasks(myTasks.stream().map(taskService::toResponse).collect(Collectors.toList()))
                .overdueTaskList(overdue.stream().map(taskService::toResponse).collect(Collectors.toList()))
                .recentProjects(projects.stream()
                        .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                        .limit(5)
                        .map(projectService::toResponse)
                        .collect(Collectors.toList()))
                .build();
    }
}
