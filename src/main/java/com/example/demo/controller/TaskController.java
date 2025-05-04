package com.example.demo.controller;

import com.example.demo.dto.task.TaskRequestDto;
import com.example.demo.dto.task.TaskResponseDto;
import com.example.demo.dto.task.TaskStatusUpdateRequestDto;
import com.example.demo.model.enums.Priority;
import com.example.demo.model.enums.TaskStatus;
import com.example.demo.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /**
     * Get all tasks with pagination.
     * Access is restricted based on a user role.
     *
     * @param pageable pagination information
     * @return a page of tasks based on a user role
     */
    @GetMapping
    public ResponseEntity<Page<TaskResponseDto>> getAllTasks(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(taskService.getAllTasks(pageable));
    }

    /**
     * Get a task by ID.
     * Access is restricted based on a user role.
     *
     * @param id the ID of the task to get
     * @return the task with the specified ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDto> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    /**
     * Get all tasks assigned to a user with pagination.
     * Access is restricted based on a user role.
     *
     * @param userId the ID of the user to get tasks for
     * @param pageable pagination information
     * @return a page of tasks assigned to the user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<TaskResponseDto>> getTasksByAssignedUserId(
            @PathVariable Long userId,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(taskService.getTasksByAssignedUserId(userId, pageable));
    }

    /**
     * Create a new task.
     * Only accessible to project owners or admins.
     *
     * @param requestDto the task data to create
     * @return the created task
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<TaskResponseDto> createTask(@Valid @RequestBody TaskRequestDto requestDto) {
        return new ResponseEntity<>(taskService.createTask(requestDto), HttpStatus.CREATED);
    }

    /**
     * Update an existing task.
     * Access is restricted based on a user role.
     *
     * @param id the ID of the task to update
     * @param requestDto the new task data
     * @return the updated task
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<TaskResponseDto> updateTask(@PathVariable Long id, @Valid @RequestBody TaskRequestDto requestDto) {
        return ResponseEntity.ok(taskService.updateTask(id, requestDto));
    }

    /**
     * Update the status of a task.
     * @param id the ID of the task to update the status for.
     * @param status the new status to set for the task.
     * @return the updated task with the new status.
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<TaskResponseDto> updateTaskStatus(@PathVariable Long id, @RequestBody TaskStatusUpdateRequestDto status) {
        return ResponseEntity.ok(taskService.updateTaskStatus(id, status.getStatus()));
    }
    /**
     * Delete a task.
     * Only accessible to project owners or admins.
     *
     * @param id the ID of the task to delete
     * @return a response with no content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }


    /**
     * Get paginated tasks with optional status and priority filters
     *
     * @param status optional status to filter by
     * @param priority optional priority to filter by
     * @param pageable pagination information
     * @return a page of tasks filtered by the specified criteria
     */
    @GetMapping("/filter")
    public ResponseEntity<Page<TaskResponseDto>> getFilteredTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Priority priority,
            @PageableDefault(size = 2, sort = "id") Pageable pageable) {
        
        if (status != null && priority != null) {
            return ResponseEntity.ok(taskService.getTasksByStatusAndPriority(status, priority, pageable));
        } else if (status != null) {
            return ResponseEntity.ok(taskService.getTasksByStatus(status, pageable));
        } else if (priority != null) {
            return ResponseEntity.ok(taskService.getTasksByPriority(priority, pageable));
        } else {
            return ResponseEntity.ok(taskService.getAllTasks(pageable));
        }
    }

}