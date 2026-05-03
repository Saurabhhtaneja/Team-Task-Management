package com.example.team_task_manager.service;


import com.example.team_task_manager.dto.ProjectRequest;
import com.example.team_task_manager.dto.ProjectResponse;
import com.example.team_task_manager.entity.*;
import com.example.team_task_manager.entity.Task.TaskStatus;
import com.example.team_task_manager.exception.*;
import com.example.team_task_manager.repository.*;
import com.example.team_task_manager.security.UserDetailsImpl;
import com.example.team_task_manager.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final UserServiceImpl userService;

    @Override
    @Transactional
    public ProjectResponse createProject(ProjectRequest request, UserDetailsImpl currentUser) {
        User owner = getUser(currentUser.getId());

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : Project.ProjectStatus.ACTIVE)
                .owner(owner)
                .deadline(request.getDeadline())
                .build();

        if (request.getMemberIds() != null) {
            Set<User> members = new HashSet<>(userRepository.findAllById(request.getMemberIds()));
            project.setMembers(members);
        }

        return toResponse(projectRepository.save(project));
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(Long id, ProjectRequest request, UserDetailsImpl currentUser) {
        Project project = getProject(id);
        assertOwnerOrAdmin(project, currentUser);

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        if (request.getStatus() != null) project.setStatus(request.getStatus());
        if (request.getDeadline() != null) project.setDeadline(request.getDeadline());
        if (request.getMemberIds() != null) {
            project.setMembers(new HashSet<>(userRepository.findAllById(request.getMemberIds())));
        }

        return toResponse(projectRepository.save(project));
    }

    @Override
    public ProjectResponse getProjectById(Long id, UserDetailsImpl currentUser) {
        Project project = getProject(id);
        assertProjectAccess(project, currentUser);
        return toResponse(project);
    }

    @Override
    public List<ProjectResponse> getAllProjects(UserDetailsImpl currentUser) {
        User user = getUser(currentUser.getId());
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        List<Project> projects = isAdmin
                ? projectRepository.findAll()
                : projectRepository.findAllProjectsForUser(user);

        return projects.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteProject(Long id, UserDetailsImpl currentUser) {
        Project project = getProject(id);
        assertOwnerOrAdmin(project, currentUser);
        projectRepository.delete(project);
    }

    @Override
    @Transactional
    public ProjectResponse addMember(Long projectId, Long userId, UserDetailsImpl currentUser) {
        Project project = getProject(projectId);
        assertOwnerOrAdmin(project, currentUser);
        User user = getUser(userId);
        project.getMembers().add(user);
        return toResponse(projectRepository.save(project));
    }

    @Override
    @Transactional
    public ProjectResponse removeMember(Long projectId, Long userId, UserDetailsImpl currentUser) {
        Project project = getProject(projectId);
        assertOwnerOrAdmin(project, currentUser);
        User user = getUser(userId);
        project.getMembers().remove(user);
        return toResponse(projectRepository.save(project));
    }

    // ── helpers ──────────────────────────────────────────────────────────

    private Project getProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private void assertOwnerOrAdmin(Project project, UserDetailsImpl currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !project.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Only the project owner or admin can perform this action");
        }
    }

    private void assertProjectAccess(Project project, UserDetailsImpl currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) return;
        boolean isMember = project.getMembers().stream()
                .anyMatch(m -> m.getId().equals(currentUser.getId()));
        boolean isOwner = project.getOwner().getId().equals(currentUser.getId());
        if (!isMember && !isOwner) {
            throw new UnauthorizedException("You don't have access to this project");
        }
    }

    public ProjectResponse toResponse(Project project) {
        long total     = taskRepository.countByProjectAndStatus(project, null) == 0
                ? project.getTasks().size()
                : project.getTasks().size();
        long completed = taskRepository.countByProjectAndStatus(project, TaskStatus.DONE);
        long pending   = total - completed;

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus())
                .owner(userService.toResponse(project.getOwner()))
                .members(project.getMembers().stream()
                        .map(userService::toResponse)
                        .collect(Collectors.toSet()))
                .deadline(project.getDeadline())
                .createdAt(project.getCreatedAt())
                .totalTasks(total)
                .completedTasks(completed)
                .pendingTasks(pending)
                .build();
    }
}