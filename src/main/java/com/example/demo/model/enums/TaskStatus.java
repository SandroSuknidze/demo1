package com.example.demo.model.enums;

/**
 * Enum representing the status of a task in the system.
 * Indicates the current state of a task in its lifecycle.
 */
public enum TaskStatus {
    /**
     * Task is created but work has not started yet.
     */
    TODO,
    
    /**
     * Task is currently being worked on.
     */
    IN_PROGRESS,
    
    /**
     * Task has been completed.
     */
    DONE
}