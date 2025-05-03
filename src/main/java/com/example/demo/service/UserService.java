package com.example.demo.service;

import com.example.demo.dto.user.UserRequestDto;
import com.example.demo.dto.user.UserResponseDto;
import com.example.demo.model.entity.User;

import java.util.List;

/**
 * Service interface for managing users.
 */
public interface UserService {

    /**
     * Get all users.
     *
     * @return a list of all users
     */
    List<UserResponseDto> getAllUsers();

    /**
     * Get a user by ID.
     *
     * @param id the ID of the user to get
     * @return the user with the specified ID
     */
    UserResponseDto getUserById(Long id);

    /**
     * Create a new user.
     *
     * @param requestDto the user data to create
     * @return the created user
     */
    UserResponseDto createUser(UserRequestDto requestDto);

    /**
     * Delete a user.
     *
     * @param id the ID of the user to delete
     */
    void deleteUser(Long id);

    /**
     * Get the currently authenticated user.
     *
     * @return the currently authenticated user
     */
    User getCurrentUser();

    /**
     * Check if the current user has admin role.
     *
     * @return true if the current user has admin role, false otherwise
     */
    boolean isAdmin();
}