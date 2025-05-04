package com.example.demo.controller;

import com.example.demo.dto.project.ProjectRequestDto;
import com.example.demo.dto.project.ProjectResponseDto;
import com.example.demo.dto.task.TaskResponseDto;
import com.example.demo.service.ProjectService;
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

import java.util.List;


@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
@Tag(name = "Projects", description = "Project management API")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final ProjectService projectService;
    private final TaskService taskService;

    /**
     * Get all projects.
     * Access is restricted based on a user role.
     *
     * @return a list of projects based on a user role
     */
    @Operation(summary = "Get all projects", description = "Returns a list of projects based on user role")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved projects",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ProjectResponseDto.class))),
        @ApiResponse(responseCode = "403", description = "Access denied", 
                content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", 
                content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<ProjectResponseDto>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    /**
     * Get a project by ID.
     * Access is restricted based on a user role.
     *
     * @param id the ID of the project to get
     * @return the project with the specified ID
     */
    @Operation(summary = "Get project by ID", description = "Returns a project by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved project",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ProjectResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "Project not found", 
                content = @Content),
        @ApiResponse(responseCode = "403", description = "Access denied", 
                content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", 
                content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDto> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    /**
     * Create a new project.
     * Only accessible to managers and admins.
     *
     * @param requestDto the project data to create
     * @return the created project
     */
    @Operation(summary = "Create a new project", description = "Creates a new project")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Project successfully created",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ProjectResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input", 
                content = @Content),
        @ApiResponse(responseCode = "403", description = "Access denied", 
                content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", 
                content = @Content)
    })
    @PostMapping
    public ResponseEntity<ProjectResponseDto> createProject(@Valid @RequestBody ProjectRequestDto requestDto) {
        return new ResponseEntity<>(projectService.createProject(requestDto), HttpStatus.CREATED);
    }

    /**
     * Update an existing project.
     * Only accessible to the project owner or admins.
     *
     * @param id the ID of the project to update
     * @param requestDto the new project data
     * @return the updated project
     */
    @Operation(summary = "Update a project", description = "Updates an existing project")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Project successfully updated",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = ProjectResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "Project not found", 
                content = @Content),
        @ApiResponse(responseCode = "400", description = "Invalid input", 
                content = @Content),
        @ApiResponse(responseCode = "403", description = "Access denied", 
                content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", 
                content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponseDto> updateProject(@PathVariable Long id, @Valid @RequestBody ProjectRequestDto requestDto) {
        return ResponseEntity.ok(projectService.updateProject(id, requestDto));
    }

    /**
     * Delete a project.
     * Only accessible to the project owner or admins.
     *
     * @param id the ID of the project to delete
     * @return a response with no content
     */
    @Operation(summary = "Delete a project", description = "Deletes an existing project")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Project successfully deleted",
                content = @Content),
        @ApiResponse(responseCode = "404", description = "Project not found", 
                content = @Content),
        @ApiResponse(responseCode = "403", description = "Access denied", 
                content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", 
                content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all tasks in a project with pagination.
     * Access is restricted based on a user role.
     *
     * @param id the ID of the project to get tasks for
     * @param pageable pagination information
     * @return a page of tasks in the project
     */
    @Operation(summary = "Get project tasks", description = "Returns all tasks in a project with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved tasks",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "404", description = "Project not found", 
                content = @Content),
        @ApiResponse(responseCode = "403", description = "Access denied", 
                content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", 
                content = @Content)
    })
    @GetMapping("/{id}/tasks")
    public ResponseEntity<Page<TaskResponseDto>> getProjectTasks(
            @PathVariable Long id,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(taskService.getTasksByProjectId(id, pageable));
    }
}
