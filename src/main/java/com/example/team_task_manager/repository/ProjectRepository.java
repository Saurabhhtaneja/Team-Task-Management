package com.example.team_task_manager.repository;


import com.example.team_task_manager.entity.Project;
import com.example.team_task_manager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByOwner(User owner);

    @Query("SELECT p FROM Project p WHERE p.owner = :user OR :user MEMBER OF p.members")
    List<Project> findAllProjectsForUser(@Param("user") User user);

    boolean existsByNameAndOwner(String name, User owner);
}
