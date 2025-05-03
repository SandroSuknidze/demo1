package com.example.demo.model.enums;

/**
 * Enum representing user roles in the system.
 * Each role has different access levels and permissions.
 */
public enum Role {
    /**
     * Admin role with full access to all resources.
     */
    ADMIN,
    
    /**
     * Manager role with access to manage their own projects and related tasks.
     */
    MANAGER,
    
    /**
     * User role with access to view and update only their own assigned tasks.
     */
    USER
}