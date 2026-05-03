package com.example.team_task_manager.service;


import com.example.team_task_manager.dto.TaskRequest;
import com.example.team_task_manager.dto.TaskStatusUpdateRequest;
import com.example.team_task_manager.dto.TaskResponse;
import com.example.team_task_manager.entity.*;
import com.example.team_task_manager.entity.Task.TaskStatus;
import com.example.team_task_manager.exception.*;
import com.example.team_task_manager.repository.*;
import com.example.team_task_manager.security.UserDetailsImpl;
import com.example.team_task_manager.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final UserServiceImpl userService;

    @Override
    @Transactional
    public TaskResponse createTask(TaskRequest request, UserDetailsImpl currentUser) {
        Project project = getProject(request.getProjectId());
        assertProjectAccess(project, currentUser);

        User creator = getUser(currentUser.getId());
        User assignee = request.getAssigneeId() != null ? getUser(request.getAssigneeId()) : null;

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : TaskStatus.TODO)
                .priority(request.getPriority() != null ? request.getPriority() : Task.TaskPriority.MEDIUM)
                .project(project)
                .createdBy(creator)
                .assignee(assignee)
                .dueDate(request.getDueDate())
                .build();

        return toResponse(taskRepository.save(task));
    }

    @Override
    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request, UserDetailsImpl currentUser) {
        Task task = getTask(id);
        assertTaskAccess(task, currentUser);

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        if (request.getDueDate() != null) task.setDueDate(request.getDueDate());
        if (request.getAssigneeId() != null) task.setAssignee(getUser(request.getAssigneeId()));

        return toResponse(taskRepository.save(task));
    }

    @Override
    @Transactional
    public TaskResponse updateTaskStatus(Long id, TaskStatusUpdateRequest request, UserDetailsImpl currentUser) {
        Task task = getTask(id);
        // Assignee or project owner/admin can update status
        boolean isAdmin = isAdmin(currentUser);
        boolean isAssignee = task.getAssignee() != null && task.getAssignee().getId().equals(currentUser.getId());
        boolean isOwner = task.getProject().getOwner().getId().equals(currentUser.getId());
        if (!isAdmin && !isAssignee && !isOwner) {
            throw new UnauthorizedException("You cannot update this task's status");
        }
        task.setStatus(request.getStatus());
        return toResponse(taskRepository.save(task));
    }

    @Override
    public TaskResponse getTaskById(Long id, UserDetailsImpl currentUser) {
        Task task = getTask(id);
        assertProjectAccess(task.getProject(), currentUser);
        return toResponse(task);
    }

    @Override
    public List<TaskResponse> getTasksByProject(Long projectId, UserDetailsImpl currentUser) {
        Project project = getProject(projectId);
        assertProjectAccess(project, currentUser);
        return taskRepository.findByProjectId(projectId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskResponse> getMyTasks(UserDetailsImpl currentUser) {
        User user = getUser(currentUser.getId());
        return taskRepository.findByAssignee(user).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteTask(Long id, UserDetailsImpl currentUser) {
        Task task = getTask(id);
        assertTaskAccess(task, currentUser);
        taskRepository.delete(task);
    }

    // ── helpers ──────────────────────────────────────────────────────────

    private Task getTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));
    }

    private Project getProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private boolean isAdmin(UserDetailsImpl currentUser) {
        return currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private void assertProjectAccess(Project project, UserDetailsImpl currentUser) {
        if (isAdmin(currentUser)) return;
        boolean isOwner = project.getOwner().getId().equals(currentUser.getId());
        boolean isMember = project.getMembers().stream()
                .anyMatch(m -> m.getId().equals(currentUser.getId()));
        if (!isOwner && !isMember) {
            throw new UnauthorizedException("You don't have access to this project");
        }
    }

    private void assertTaskAccess(Task task, UserDetailsImpl currentUser) {
        if (isAdmin(currentUser)) return;
        boolean isOwner = task.getProject().getOwner().getId().equals(currentUser.getId());
        boolean isCreator = task.getCreatedBy().getId().equals(currentUser.getId());
        if (!isOwner && !isCreator) {
            throw new UnauthorizedException("You don't have permission to modify this task");
        }
    }

    public TaskResponse toResponse(Task task) {
        boolean overdue = task.getDueDate() != null
                && task.getDueDate().isBefore(LocalDateTime.now())
                && task.getStatus() != TaskStatus.DONE;

        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .projectId(task.getProject().getId())
                .projectName(task.getProject().getName())
                .assignee(task.getAssignee() != null ? userService.toResponse(task.getAssignee()) : null)
                .createdBy(userService.toResponse(task.getCreatedBy()))
                .dueDate(task.getDueDate())
                .createdAt(task.getCreatedAt())
                .overdue(overdue)
                .build();
    }
}