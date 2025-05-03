package com.example.demo.controller;

import com.example.demo.dto.task.TaskRequestDto;
import com.example.demo.dto.task.TaskResponseDto;
import com.example.demo.dto.task.TaskStatusUpdateRequestDto;
import com.example.demo.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /**
     * Get all tasks.
     * Access is restricted based on a user role.
     *
     * @return a list of tasks based on a user role
     */
    @GetMapping
    public ResponseEntity<List<TaskResponseDto>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
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
     * Get all tasks assigned to a user.
     * Access is restricted based on a user role.
     *
     * @param userId the ID of the user to get tasks for
     * @return a list of tasks assigned to the user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TaskResponseDto>> getTasksByAssignedUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(taskService.getTasksByAssignedUserId(userId));
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
}