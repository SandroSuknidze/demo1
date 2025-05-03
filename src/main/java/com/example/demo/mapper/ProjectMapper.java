package com.example.demo.mapper;

import com.example.demo.dto.project.ProjectRequestDto;
import com.example.demo.dto.project.ProjectResponseDto;
import com.example.demo.model.entity.Project;
import com.example.demo.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Mapper class for converting between Project entity and DTOs.
 */
@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(target = "owner", source = "owner")
    ProjectResponseDto toResponseDto(Project project);

    @Mapping(target = "owner", source = "owner")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    Project toEntity(ProjectRequestDto requestDto, User owner);


    @Mapping(target = "name", source = "requestDto.name")
    @Mapping(target = "description", source = "requestDto.description")
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    Project updateEntity(Project project, ProjectRequestDto requestDto);
}


//@Component
//@RequiredArgsConstructor
//public class ProjectMapper {
//
//    private final UserMapper userMapper;
//
//    /**
//     * Convert a Project entity to a ProjectResponseDto.
//     *
//     * @param project the Project entity to convert
//     * @return the ProjectResponseDto
//     */
//    public ProjectResponseDto toResponseDto(Project project) {
//        if (project == null) {
//            return null;
//        }
//
//        return ProjectResponseDto.builder()
//                .id(project.getId())
//                .name(project.getName())
//                .description(project.getDescription())
//                .owner(userMapper.toResponseDto(project.getOwner()))
//                .createDate(project.getCreateDate())
//                .updateDate(project.getUpdateDate())
//                .build();
//    }
//
//    /**
//     * Convert a ProjectRequestDto to a new Project entity.
//     *
//     * @param requestDto the ProjectRequestDto to convert
//     * @param owner the User who will own the project
//     * @return the Project entity
//     */
//    public Project toEntity(ProjectRequestDto requestDto, User owner) {
//        if (requestDto == null) {
//            return null;
//        }
//
//        return Project.builder()
//                .name(requestDto.getName())
//                .description(requestDto.getDescription())
//                .owner(owner)
//                .build();
//    }
//
//    /**
//     * Update an existing Project entity with data from a ProjectRequestDto.
//     *
//     * @param project the Project entity to update
//     * @param requestDto the ProjectRequestDto containing the new data
//     * @return the updated Project entity
//     */
//    public Project updateEntity(Project project, ProjectRequestDto requestDto) {
//        if (project == null || requestDto == null) {
//            return project;
//        }
//
//        project.setName(requestDto.getName());
//        project.setDescription(requestDto.getDescription());
//
//        return project;
//    }
//}