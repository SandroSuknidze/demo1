package com.example.demo.controller;

import com.example.demo.dto.user.UserRequestDto;
import com.example.demo.dto.user.UserResponseDto;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management API")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    /**
     * Get all users.
     * Only accessible to admins.
     *
     * @return a list of all users
     */
    @Operation(summary = "Get all users", description = "Returns a list of all users (admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved users",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = UserResponseDto.class))),
        @ApiResponse(responseCode = "403", description = "Access denied", 
                content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", 
                content = @Content)
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Get a user by ID.
     * Accessible to admins or the user themselves.
     *
     * @param id the ID of the user to get
     * @return the user with the specified ID
     */
    @Operation(summary = "Get user by ID", description = "Returns a user by their ID (admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved user",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = UserResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "User not found", 
                content = @Content),
        @ApiResponse(responseCode = "403", description = "Access denied", 
                content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", 
                content = @Content)
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * Create a new user.
     * Only accessible to admins.
     *
     * @param requestDto the user data to create
     * @return the created user
     */
    @Operation(summary = "Create a new user", description = "Creates a new user (admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User successfully created",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = UserResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input", 
                content = @Content),
        @ApiResponse(responseCode = "403", description = "Access denied", 
                content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", 
                content = @Content)
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto requestDto) {
        return new ResponseEntity<>(userService.createUser(requestDto), HttpStatus.CREATED);
    }

    /**
     * Delete a user.
     * Only accessible to admins.
     *
     * @param id the ID of the user to delete
     * @return a response with no content
     */
    @Operation(summary = "Delete a user", description = "Deletes an existing user (admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User successfully deleted",
                content = @Content),
        @ApiResponse(responseCode = "404", description = "User not found", 
                content = @Content),
        @ApiResponse(responseCode = "403", description = "Access denied", 
                content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", 
                content = @Content)
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get the current user.
     *
     * @return the current user
     */
    @Operation(summary = "Get current user", description = "Returns the currently authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved current user",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = UserResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", 
                content = @Content)
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser() {
        return ResponseEntity.ok(userService.getUserById(userService.getCurrentUser().getId()));
    }
}
