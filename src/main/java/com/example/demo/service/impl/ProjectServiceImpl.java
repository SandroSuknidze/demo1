package com.example.demo.service.impl;

import com.example.demo.dto.project.ProjectRequestDto;
import com.example.demo.dto.project.ProjectResponseDto;
import com.example.demo.exception.AccessDeniedException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.ProjectMapper;
import com.example.demo.model.entity.Project;
import com.example.demo.model.entity.Task;
import com.example.demo.model.entity.User;
import com.example.demo.model.enums.Role;
import com.example.demo.repository.ProjectRepository;
import com.example.demo.repository.TaskRepository;
import com.example.demo.service.ProjectService;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the ProjectService interface.
 */
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final ProjectMapper projectMapper;
    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponseDto> getAllProjects() {
        User currentUser = userService.getCurrentUser();
        List<Project> projects;

        if (currentUser.getRole() == Role.ADMIN) {
            projects = projectRepository.findAll();
        } else {
            projects = projectRepository.findByOwner(currentUser);
        }

        return projects.stream()
                .map(projectMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponseDto getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        if (!hasProjectAccess(id)) {
            throw new AccessDeniedException("You don't have access to this project");
        }

        return projectMapper.toResponseDto(project);
    }

    @Override
    @Transactional
    public ProjectResponseDto createProject(ProjectRequestDto requestDto) {
        User currentUser = userService.getCurrentUser();

        Project project = projectMapper.toEntity(requestDto, currentUser);
        Project savedProject = projectRepository.save(project);
        return projectMapper.toResponseDto(savedProject);
    }

    @Override
    @Transactional
    public ProjectResponseDto updateProject(Long id, ProjectRequestDto requestDto) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        if (!userService.isAdmin() && !isProjectOwner(id)) {
            throw new AccessDeniedException("You don't have permission to update this project");
        }

        Project updatedProject = projectMapper.updateEntity(project, requestDto);
        Project savedProject = projectRepository.save(updatedProject);
        return projectMapper.toResponseDto(savedProject);
    }

    @Override
    @Transactional
    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Project", "id", id);
        }

        if (!userService.isAdmin() && !isProjectOwner(id)) {
            throw new AccessDeniedException("You don't have permission to delete this project");
        }

        List<Task> tasks = taskRepository.findByProjectId(id, Pageable.unpaged()).getContent();
        for (Task task : tasks) {
            taskRepository.deleteById(task.getId());
        }

        projectRepository.deleteById(id);
    }

    @Override
    public boolean isProjectOwner(Long projectId) {
        User currentUser = userService.getCurrentUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        return project.getOwner().getId().equals(currentUser.getId());
    }

    @Override
    public boolean hasProjectAccess(Long projectId) {
        User currentUser = userService.getCurrentUser();

        if (currentUser.getRole() == Role.ADMIN) {
            return true;
        }

        if (currentUser.getRole() == Role.MANAGER && isProjectOwner(projectId)) {
            return true;
        }

        if (currentUser.getRole() == Role.USER) {
            return !taskRepository.findByAssignedUserAndProjectId(currentUser, projectId).isEmpty();
        }

        return false;
    }
}
