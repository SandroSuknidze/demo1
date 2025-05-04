package com.example.demo.controller;

import com.example.demo.dto.task.TaskRequestDto;
import com.example.demo.dto.task.TaskResponseDto;
import com.example.demo.dto.task.TaskStatusUpdateRequestDto;
import com.example.demo.model.enums.Priority;
import com.example.demo.model.enums.TaskStatus;
import com.example.demo.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@Tag(name = "Tasks", description = "Task management API")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;

    /**
     * Get all tasks with pagination.
     * Access is restricted based on a user role.
     *
     * @param pageable pagination information
     * @return a page of tasks based on a user role
     */
    @Operation(summary = "Get all tasks", description = "Returns all tasks with pagination based on user role")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved tasks",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", 
                content = @Content)
    })
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
    @Operation(summary = "Get task by ID", description = "Returns a task by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved task",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = TaskResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "Task not found", 
                content = @Content),
        @ApiResponse(responseCode = "403", description = "Access denied", 
                content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", 
                content = @Content)
    })
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
    @Operation(summary = "Get tasks by user ID", description = "Returns all tasks assigned to a specific user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved tasks",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "404", description = "User not found", 
                content = @Content),
        @ApiResponse(responseCode = "403", description = "Access denied", 
                content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", 
                content = @Content)
    })
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
    @Operation(summary = "Create a new task", description = "Creates a new task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Task successfully created",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = TaskResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input", 
                content = @Content),
        @ApiResponse(responseCode = "403", description = "Access denied", 
                content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", 
                content = @Content)
    })
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
    @Operation(summary = "Update a task", description = "Updates an existing task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task successfully updated",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = TaskResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "Task not found", 
                content = @Content),
        @ApiResponse(responseCode = "400", description = "Invalid input", 
                content = @Content),
        @ApiResponse(responseCode = "403", description = "Access denied", 
                content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", 
                content = @Content)
    })
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
    @Operation(summary = "Update task status", description = "Updates the status of an existing task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task status successfully updated",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = TaskResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "Task not found", 
                content = @Content),
        @ApiResponse(responseCode = "400", description = "Invalid status", 
                content = @Content),
        @ApiResponse(responseCode = "403", description = "Access denied", 
                content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", 
                content = @Content)
    })
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
    @Operation(summary = "Delete a task", description = "Deletes an existing task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Task successfully deleted",
                content = @Content),
        @ApiResponse(responseCode = "404", description = "Task not found", 
                content = @Content),
        @ApiResponse(responseCode = "403", description = "Access denied", 
                content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", 
                content = @Content)
    })
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
    @Operation(summary = "Get filtered tasks", description = "Returns tasks filtered by status and/or priority")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved filtered tasks",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", 
                content = @Content)
    })
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
