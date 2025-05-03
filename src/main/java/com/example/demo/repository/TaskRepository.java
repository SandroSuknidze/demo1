package com.example.demo.repository;

import com.example.demo.model.entity.Project;
import com.example.demo.model.entity.Task;
import com.example.demo.model.entity.User;
import com.example.demo.model.enums.Priority;
import com.example.demo.model.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Task entity.
 * Provides methods to interact with the tasks table in the database.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Find all tasks in a specific project.
     *
     * @param project the project to find tasks for
     * @return a list of tasks in the project
     */
    List<Task> findByProject(Project project);

    /**
     * Find all tasks in a project with the specified ID.
     *
     * @param projectId the ID of the project to find tasks for
     * @return a list of tasks in the project
     */
    List<Task> findByProjectId(Long projectId);

    /**
     * Find all tasks assigned to a specific user.
     *
     * @param assignedUser the user assigned to the tasks
     * @return a list of tasks assigned to the user
     */
    List<Task> findByAssignedUser(User assignedUser);

    /**
     * Find all tasks assigned to a user with the specified ID.
     *
     * @param assignedUserId the ID of the user assigned to the tasks
     * @return a list of tasks assigned to the user
     */
    List<Task> findByAssignedUserId(Long assignedUserId);

    /**
     * Find all tasks with a specific status.
     *
     * @param status the status to find tasks for
     * @return a list of tasks with the specified status
     */
    List<Task> findByStatus(TaskStatus status);

    /**
     * Find all tasks in a specific project with a specific status.
     *
     * @param project the project to find tasks for
     * @param status the status to find tasks for
     * @return a list of tasks in the project with the specified status
     */
    List<Task> findByProjectAndStatus(Project project, TaskStatus status);

    /**
     * Find all tasks assigned to a specific user with a specific status.
     *
     * @param assignedUser the user assigned to the tasks
     * @param status the status to find tasks for
     * @return a list of tasks assigned to the user with the specified status
     */
    List<Task> findByAssignedUserAndStatus(User assignedUser, TaskStatus status);

    /**
     * Find all tasks assigned to a specific user in a specific project.
     *
     * @param assignedUser the user assigned to the tasks
     * @param projectId the ID of the project to find tasks for
     * @return a list of tasks assigned to the user in the specified project
     */
    List<Task> findByAssignedUserAndProjectId(User assignedUser, Long projectId);

    /**
     * Find all tasks with pagination.
     *
     * @param pageable the pagination information
     * @return a page of tasks
     */
    Page<Task> findAll(Pageable pageable);

    /**
     * Find all tasks with a specific status with pagination.
     *
     * @param status the status to find tasks for
     * @param pageable the pagination information
     * @return a page of tasks with the specified status
     */
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    /**
     * Find all tasks with a specific priority with pagination.
     *
     * @param priority the priority to find tasks for
     * @param pageable the pagination information
     * @return a page of tasks with the specified priority
     */
    Page<Task> findByPriority(Priority priority, Pageable pageable);

    /**
     * Find all tasks with a specific status and priority with pagination.
     *
     * @param status the status to find tasks for
     * @param priority the priority to find tasks for
     * @param pageable the pagination information
     * @return a page of tasks with the specified status and priority
     */
    Page<Task> findByStatusAndPriority(TaskStatus status, Priority priority, Pageable pageable);

    /**
     * Find all tasks in a specific project with pagination.
     *
     * @param projectId the ID of the project to find tasks for
     * @param pageable the pagination information
     * @return a page of tasks in the project
     */
    Page<Task> findByProjectId(Long projectId, Pageable pageable);

    /**
     * Find all tasks assigned to a specific user with pagination.
     *
     * @param assignedUserId the ID of the user assigned to the tasks
     * @param pageable the pagination information
     * @return a page of tasks assigned to the user
     */
    Page<Task> findByAssignedUserId(Long assignedUserId, Pageable pageable);
}
