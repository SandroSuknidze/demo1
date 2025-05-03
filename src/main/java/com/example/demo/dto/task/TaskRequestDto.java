package com.example.demo.dto.task;

import com.example.demo.model.enums.Priority;
import com.example.demo.model.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequestDto {
    
    @NotBlank(message = "Task title is required")
    private String title;
    
    private String description;
    
    @NotNull(message = "Task status is required")
    private TaskStatus status;
    
    private LocalDate dueDate;
    
    @NotNull(message = "Task priority is required")
    private Priority priority;
    
    @NotNull(message = "Project ID is required")
    private Long projectId;
    
    private Long assignedUserId;
}