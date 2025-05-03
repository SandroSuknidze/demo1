package com.example.demo.mapper;

import com.example.demo.dto.task.TaskRequestDto;
import com.example.demo.dto.task.TaskResponseDto;
import com.example.demo.model.entity.Project;
import com.example.demo.model.entity.Task;
import com.example.demo.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Mapper class for converting between Task entity and DTOs.
 */
@Component
@RequiredArgsConstructor
public class TaskMapper {

    private final UserMapper userMapper;
    private final ProjectMapper projectMapper;

    /**
     * Convert a Task entity to a TaskResponseDto.
     *
     * @param task the Task entity to convert
     * @return the TaskResponseDto
     */
    public TaskResponseDto toResponseDto(Task task) {
        if (task == null) {
            return null;
        }
        
        return TaskResponseDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .dueDate(task.getDueDate())
                .priority(task.getPriority())
                .project(projectMapper.toResponseDto(task.getProject()))
                .assignedUser(userMapper.toResponseDto(task.getAssignedUser()))
                .createDate(task.getCreateDate())
                .updateDate(task.getUpdateDate())
                .build();
    }

    /**
     * Convert a TaskRequestDto to a new Task entity.
     *
     * @param requestDto the TaskRequestDto to convert
     * @param project the Project the task belongs to
     * @param assignedUser the User assigned to the task (can be null)
     * @return the Task entity
     */
    public Task toEntity(TaskRequestDto requestDto, Project project, User assignedUser) {
        if (requestDto == null) {
            return null;
        }
        
        return Task.builder()
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .status(requestDto.getStatus())
                .dueDate(requestDto.getDueDate())
                .priority(requestDto.getPriority())
                .project(project)
                .assignedUser(assignedUser)
                .build();
    }

    /**
     * Update an existing Task entity with data from a TaskRequestDto.
     *
     * @param task the Task entity to update
     * @param requestDto the TaskRequestDto containing the new data
     * @param project the Project the task belongs to
     * @param assignedUser the User assigned to the task (can be null)
     * @return the updated Task entity
     */
    public Task updateEntity(Task task, TaskRequestDto requestDto, Project project, User assignedUser) {
        if (task == null || requestDto == null) {
            return task;
        }
        
        task.setTitle(requestDto.getTitle());
        task.setDescription(requestDto.getDescription());
        task.setStatus(requestDto.getStatus());
        task.setDueDate(requestDto.getDueDate());
        task.setPriority(requestDto.getPriority());
        task.setProject(project);
        task.setAssignedUser(assignedUser);
        
        return task;
    }
}