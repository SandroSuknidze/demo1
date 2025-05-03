package com.example.demo.model.enums;

/**
 * Enum representing the priority level of a task in the system.
 * Indicates the importance and urgency of a task.
 */
public enum Priority {
    /**
     * Low priority task can be completed when time permits.
     */
    LOW,
    
    /**
     * Medium priority task should be completed in a reasonable timeframe.
     */
    MEDIUM,
    
    /**
     * High priority task, requires immediate attention and should be completed as soon as possible.
     */
    HIGH
}