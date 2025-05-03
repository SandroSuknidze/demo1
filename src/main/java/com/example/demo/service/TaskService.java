package com.example.demo.service;

import com.example.demo.dto.task.TaskRequestDto;
import com.example.demo.dto.task.TaskResponseDto;
import com.example.demo.model.enums.Priority;
import com.example.demo.model.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for managing tasks.
 */
public interface TaskService {

    /**
     * Get a task by ID.
     * Access is restricted based on user role.
     *
     * @param id the ID of the task to get
     * @return the task with the specified ID
     */
    TaskResponseDto getTaskById(Long id);

    /**
     * Create a new task.
     * Only accessible to project owners or admins.
     *
     * @param requestDto the task data to create
     * @return the created task
     */
    TaskResponseDto createTask(TaskRequestDto requestDto);

    /**
     * Update an existing task.
     * Access is restricted based on user role.
     *
     * @param id the ID of the task to update
     * @param requestDto the new task data
     * @return the updated task
     */
    TaskResponseDto updateTask(Long id, TaskRequestDto requestDto);

    /**
     * Update the status of a task.
     * Regular users can only update status of tasks assigned to them.
     * Managers can update status of tasks in their projects.
     * Admins can update any task.
     *
     * @param id the ID of the task to update
     * @param status the new status
     * @return the updated task
     */
    TaskResponseDto updateTaskStatus(Long id, TaskStatus status);

    /**
     * Delete a task.
     * Only accessible to project owners or admins.
     *
     * @param id the ID of the task to delete
     */
    void deleteTask(Long id);

    /**
     * Check if the current user is assigned to the specified task.
     *
     * @param taskId the ID of the task to check
     * @return true if the current user is assigned to the task, false otherwise
     */
    boolean isAssignedToTask(Long taskId);

    /**
     * Check if the current user has access to the specified task.
     * Admins have access to all tasks.
     * Managers have access to tasks in their own projects.
     * Users have access to their own assigned tasks.
     *
     * @param taskId the ID of the task to check
     * @return true if the current user has access, false otherwise
     */
    boolean hasTaskAccess(Long taskId);

    /**
     * Get all tasks with pagination.
     * For admins: returns all tasks.
     * For managers: returns tasks in their own projects.
     * For users: returns only their own assigned tasks.
     *
     * @param pageable the pagination information
     * @return a page of tasks based on user role
     */
    Page<TaskResponseDto> getAllTasks(Pageable pageable);

    /**
     * Get all tasks in a specific project with pagination.
     * Access is restricted based on user role.
     *
     * @param projectId the ID of the project to get tasks for
     * @param pageable the pagination information
     * @return a page of tasks in the project
     */
    Page<TaskResponseDto> getTasksByProjectId(Long projectId, Pageable pageable);

    /**
     * Get all tasks assigned to a specific user with pagination.
     * Access is restricted based on user role.
     *
     * @param userId the ID of the user to get tasks for
     * @param pageable the pagination information
     * @return a page of tasks assigned to the user
     */
    Page<TaskResponseDto> getTasksByAssignedUserId(Long userId, Pageable pageable);

    /**
     * Get all tasks with a specific status with pagination.
     * Access is restricted based on user role.
     *
     * @param status the status to filter by
     * @param pageable the pagination information
     * @return a page of tasks with the specified status
     */
    Page<TaskResponseDto> getTasksByStatus(TaskStatus status, Pageable pageable);

    /**
     * Get all tasks with a specific priority with pagination.
     * Access is restricted based on user role.
     *
     * @param priority the priority to filter by
     * @param pageable the pagination information
     * @return a page of tasks with the specified priority
     */
    Page<TaskResponseDto> getTasksByPriority(Priority priority, Pageable pageable);

    /**
     * Get all tasks with a specific status and priority with pagination.
     * Access is restricted based on user role.
     *
     * @param status the status to filter by
     * @param priority the priority to filter by
     * @param pageable the pagination information
     * @return a page of tasks with the specified status and priority
     */
    Page<TaskResponseDto> getTasksByStatusAndPriority(TaskStatus status, Priority priority, Pageable pageable);
}
