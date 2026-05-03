package com.example.team_task_manager.repository;


import com.example.team_task_manager.entity.Project;
import com.example.team_task_manager.entity.Task;
import com.example.team_task_manager.entity.Task.TaskStatus;
import com.example.team_task_manager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProjectId(Long projectId);

    List<Task> findByAssignee(User assignee);

    List<Task> findByProjectAndStatus(Project project, TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.assignee = :user AND t.dueDate < :now AND t.status != 'DONE'")
    List<Task> findOverdueTasksForUser(@Param("user") User user, @Param("now") LocalDateTime now);

    @Query("SELECT t FROM Task t WHERE t.dueDate < :now AND t.status != 'DONE'")
    List<Task> findAllOverdueTasks(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project = :project AND t.status = :status")
    long countByProjectAndStatus(@Param("project") Project project, @Param("status") TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.project.id IN " +
            "(SELECT p.id FROM Project p WHERE p.owner = :user OR :user MEMBER OF p.members)")
    List<Task> findAllTasksVisibleToUser(@Param("user") User user);
}
