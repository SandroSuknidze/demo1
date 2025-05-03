package com.example.demo.dto.task;

import com.example.demo.dto.project.ProjectResponseDto;
import com.example.demo.dto.user.UserResponseDto;
import com.example.demo.model.enums.Priority;
import com.example.demo.model.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponseDto {
    
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private LocalDate dueDate;
    private Priority priority;
    private ProjectResponseDto project;
    private UserResponseDto assignedUser;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
}