package com.example.demo.repository;

import com.example.demo.model.entity.Task;
import com.example.demo.model.entity.User;
import com.example.demo.model.enums.Priority;
import com.example.demo.model.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * Repository interface for Task entity.
 * Provides methods to interact with the tasks table in the database.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Find all tasks in a specific project with pagination.
     * Non-paginated access can be achieved using findByProjectId(id, Pageable.unpaged())
     *
     * @param projectId the ID of the project to find tasks for
     * @param pageable the pagination information
     * @return a page of tasks in the project
     */
    Page<Task> findByProjectId(Long projectId, Pageable pageable);

    /**
     * Find all tasks assigned to a specific user with pagination.
     * Non-paginated access can be achieved using findByAssignedUserId(id, Pageable.unpaged())
     *
     * @param assignedUserId the ID of the user assigned to the tasks
     * @param pageable the pagination information
     * @return a page of tasks assigned to the user
     */
    Page<Task> findByAssignedUserId(Long assignedUserId, Pageable pageable);

    /**
     * Find all tasks with a specific status with pagination.
     * Non-paginated access can be achieved using findByStatus(status, Pageable.unpaged())
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
     * Find all tasks in projects with the specified IDs with pagination.
     *
     * @param projectIds the IDs of the projects to find tasks for
     * @param pageable the pagination information
     * @return a page of tasks in the specified projects
     */
    Page<Task> findByProjectIdIn(List<Long> projectIds, Pageable pageable);
    
    /**
     * Find tasks with a specific status in projects with specified IDs with pagination.
     *
     * @param status the status to find tasks for
     * @param projectIds the IDs of the projects to find tasks for
     * @param pageable the pagination information
     * @return a page of tasks with the specified status in the specified projects
     */
    Page<Task> findByStatusAndProjectIdIn(TaskStatus status, List<Long> projectIds, Pageable pageable);
    
    /**
     * Find tasks with a specific status assigned to a specific user with pagination.
     *
     * @param status the status to find tasks for
     * @param assignedUserId the ID of the user assigned to the tasks
     * @param pageable the pagination information
     * @return a page of tasks with the specified status assigned to the specified user
     */
    Page<Task> findByStatusAndAssignedUserId(TaskStatus status, Long assignedUserId, Pageable pageable);
    
    /**
     * Find tasks with a specific priority in projects with specified IDs with pagination.
     *
     * @param priority the priority to find tasks for
     * @param projectIds the IDs of the projects to find tasks for
     * @param pageable the pagination information
     * @return a page of tasks with the specified priority in the specified projects
     */
    Page<Task> findByPriorityAndProjectIdIn(Priority priority, List<Long> projectIds, Pageable pageable);
    
    /**
     * Find tasks with a specific priority assigned to a specific user with pagination.
     *
     * @param priority the priority to find tasks for
     * @param assignedUserId the ID of the user assigned to the tasks
     * @param pageable the pagination information
     * @return a page of tasks with the specified priority assigned to the specified user
     */
    Page<Task> findByPriorityAndAssignedUserId(Priority priority, Long assignedUserId, Pageable pageable);
    
    /**
     * Find tasks with a specific status and priority in projects with specified IDs with pagination.
     *
     * @param status the status to find tasks for
     * @param priority the priority to find tasks for
     * @param projectIds the IDs of the projects to find tasks for
     * @param pageable the pagination information
     * @return a page of tasks with the specified status and priority in the specified projects
     */
    Page<Task> findByStatusAndPriorityAndProjectIdIn(TaskStatus status, Priority priority, List<Long> projectIds, Pageable pageable);
    
    /**
     * Find tasks with a specific status and priority assigned to a specific user with pagination.
     *
     * @param status the status to find tasks for
     * @param priority the priority to find tasks for
     * @param assignedUserId the ID of the user assigned to the tasks
     * @param pageable the pagination information
     * @return a page of tasks with the specified status and priority assigned to the specified user
     */
    Page<Task> findByStatusAndPriorityAndAssignedUserId(TaskStatus status, Priority priority, Long assignedUserId, Pageable pageable);

    /**
     * Find tasks assigned to a specific user that belong to any of the specified projects.
     *
     * @param userId the ID of the user assigned to the tasks
     * @param projectIds the list of project IDs to search within
     * @param pageable the pagination information
     * @return a page of tasks assigned to the user within the specified projects
     */
    Page<Task> findByAssignedUserIdAndProjectIdIn(Long userId, List<Long> projectIds, Pageable pageable);

    /**
     * Find tasks assigned to a specific user in a specific project.
     *
     * @param assignedUser the user assigned to the tasks
     * @param projectId the ID of the project to find tasks for
     * @return a list of tasks assigned to the user in the specified project
     */
    List<Task> findByAssignedUserAndProjectId(User assignedUser, Long projectId);
}
