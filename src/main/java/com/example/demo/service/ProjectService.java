package com.example.demo.service;

import com.example.demo.dto.project.ProjectRequestDto;
import com.example.demo.dto.project.ProjectResponseDto;

import java.util.List;

/**
 * Service interface for managing projects.
 */
public interface ProjectService {

    /**
     * Get all projects.
     * For admins: returns all projects.
     * For managers: returns only their own projects.
     * For users: returns projects they have tasks assigned to.
     *
     * @return a list of projects based on user role
     */
    List<ProjectResponseDto> getAllProjects();

    /**
     * Get a project by ID.
     * Access is restricted based on user role.
     *
     * @param id the ID of the project to get
     * @return the project with the specified ID
     */
    ProjectResponseDto getProjectById(Long id);

    /**
     * Create a new project.
     * Only accessible to managers and admins.
     *
     * @param requestDto the project data to create
     * @return the created project
     */
    ProjectResponseDto createProject(ProjectRequestDto requestDto);

    /**
     * Update an existing project.
     * Only accessible to the project owner or admins.
     *
     * @param id the ID of the project to update
     * @param requestDto the new project data
     * @return the updated project
     */
    ProjectResponseDto updateProject(Long id, ProjectRequestDto requestDto);

    /**
     * Delete a project.
     * Only accessible to the project owner or admins.
     *
     * @param id the ID of the project to delete
     */
    void deleteProject(Long id);

    /**
     * Check if the current user is the owner of the specified project.
     *
     * @param projectId the ID of the project to check
     * @return true if the current user is the owner, false otherwise
     */
    boolean isProjectOwner(Long projectId);

    /**
     * Check if the current user has access to the specified project.
     * Admins have access to all projects.
     * Managers have access to their own projects.
     * Users have access to projects they have tasks assigned to.
     *
     * @param projectId the ID of the project to check
     * @return true if the current user has access, false otherwise
     */
    boolean hasProjectAccess(Long projectId);
}