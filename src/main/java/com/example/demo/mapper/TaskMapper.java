package com.example.demo.mapper;

import com.example.demo.dto.task.TaskRequestDto;
import com.example.demo.dto.task.TaskResponseDto;
import com.example.demo.model.entity.Project;
import com.example.demo.model.entity.Task;
import com.example.demo.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper class for converting between Task entity and DTOs.
 */

@Mapper(componentModel = "spring")
public interface TaskMapper {

    /**
     * Convert a Task entity to a TaskResponseDto.
     *
     * @param task the Task entity to convert
     * @return the TaskResponseDto
     */
    TaskResponseDto toResponseDto(Task task);

    /**
     * Convert a TaskRequestDto to a new Task entity.
     *
     * @param requestDto the TaskRequestDto to convert
     * @param project the Project the task belongs to
     * @param assignedUser the User assigned to the task (can be null)
     * @return the Task entity
     */
    @Mapping(target = "project", source = "project")
    @Mapping(target = "assignedUser", source = "assignedUser")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    @Mapping(target = "description", source = "requestDto.description")
    @Mapping(target = "title", source = "requestDto.title")
    @Mapping(target = "status", source = "requestDto.status")
    @Mapping(target = "dueDate", source = "requestDto.dueDate")
    @Mapping(target = "priority", source = "requestDto.priority")
    Task toEntity(TaskRequestDto requestDto, Project project, User assignedUser);

    /**
     * Update an existing Task entity with data from a TaskRequestDto.
     *
     * @param task the Task entity to update
     * @param requestDto the TaskRequestDto containing the new data
     * @param project the Project the task belongs to
     * @param assignedUser the User assigned to the task (can be null)
     * @return the updated Task entity
     */
    @Mapping(target = "project", source = "project")
    @Mapping(target = "assignedUser", source = "assignedUser")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    @Mapping(target = "description", source = "requestDto.description")
    @Mapping(target = "title", source = "requestDto.title")
    @Mapping(target = "status", source = "requestDto.status")
    @Mapping(target = "dueDate", source = "requestDto.dueDate")
    @Mapping(target = "priority", source = "requestDto.priority")
    Task updateEntity(Task task, TaskRequestDto requestDto, Project project, User assignedUser);
}