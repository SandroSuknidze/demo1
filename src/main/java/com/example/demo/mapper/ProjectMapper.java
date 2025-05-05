package com.example.demo.mapper;

import com.example.demo.dto.project.ProjectRequestDto;
import com.example.demo.dto.project.ProjectResponseDto;
import com.example.demo.model.entity.Project;
import com.example.demo.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper class for converting between Project entity and DTOs.
 */
@Mapper(componentModel = "spring")
public interface ProjectMapper {

    /**
     * Convert a Project entity to a ProjectResponseDto.
     *
     * @param project the Project entity to convert
     * @return the ProjectResponseDto
     */
    @Mapping(target = "owner", source = "owner")
    ProjectResponseDto toResponseDto(Project project);


    /**
     * Convert a ProjectRequestDto to a new Project entity.
     *
     * @param requestDto the ProjectRequestDto to convert
     * @param owner the User who will own the project
     * @return the Project entity
     */
    @Mapping(target = "owner", source = "owner")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    Project toEntity(ProjectRequestDto requestDto, User owner);


    /**
     * Update an existing Project entity with data from a ProjectRequestDto.
     *
     * @param project the Project entity to update
     * @param requestDto the ProjectRequestDto containing the new data
     * @return the updated Project entity
     */
    @Mapping(target = "name", source = "requestDto.name")
    @Mapping(target = "description", source = "requestDto.description")
    @Mapping(target = "owner", source = "project.owner")
    @Mapping(target = "id", source = "project.id")
    @Mapping(target = "createDate", source = "project.createDate")
    @Mapping(target = "updateDate", ignore = true)
    Project updateEntity(Project project, ProjectRequestDto requestDto);
}
