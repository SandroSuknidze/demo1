package com.example.demo.repository;

import com.example.demo.model.entity.Project;
import com.example.demo.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Project entity.
 * Provides methods to interact with the projects table in the database.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    /**
     * Find all projects owned by a specific user.
     *
     * @param owner the user who owns the projects
     * @return a list of projects owned by the user
     */
    List<Project> findByOwner(User owner);
}