package com.example.demo.service.impl;

import com.example.demo.dto.task.TaskRequestDto;
import com.example.demo.dto.task.TaskResponseDto;
import com.example.demo.exception.AccessDeniedException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.TaskMapper;
import com.example.demo.model.entity.Project;
import com.example.demo.model.entity.Task;
import com.example.demo.model.entity.User;
import com.example.demo.model.enums.Priority;
import com.example.demo.model.enums.Role;
import com.example.demo.model.enums.TaskStatus;
import com.example.demo.repository.ProjectRepository;
import com.example.demo.repository.TaskRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ProjectService;
import com.example.demo.service.TaskService;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the TaskService interface.
 */
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;
    private final UserService userService;
    private final ProjectService projectService;


    // Non-paginated methods have been removed

    @Override
    @Transactional(readOnly = true)
    public TaskResponseDto getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        if (!hasTaskAccess(id)) {
            throw new AccessDeniedException("You don't have access to this task");
        }

        return taskMapper.toResponseDto(task);
    }

    @Override
    @Transactional
    public TaskResponseDto createTask(TaskRequestDto requestDto) {
        Project project = projectRepository.findById(requestDto.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", requestDto.getProjectId()));

        if (!userService.isAdmin() && !projectService.isProjectOwner(requestDto.getProjectId())) {
            throw new AccessDeniedException("You don't have permission to create tasks in this project");
        }

        User assignedUser = null;
        if (requestDto.getAssignedUserId() != null) {
            assignedUser = userRepository.findById(requestDto.getAssignedUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", requestDto.getAssignedUserId()));

            if (requestDto.getAssignedUserId() != null && assignedUser.getRole() != Role.USER) {
                throw new IllegalArgumentException("Only users can be assigned to a task");
            }
        }

        Task task = taskMapper.toEntity(requestDto, project, assignedUser);
        Task savedTask = taskRepository.save(task);
        return taskMapper.toResponseDto(savedTask);
    }

    @Override
    @Transactional
    public TaskResponseDto updateTask(Long id, TaskRequestDto requestDto) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        User currentUser = userService.getCurrentUser();
        if (currentUser.getRole() == Role.USER && !isAssignedToTask(id)) {
            throw new AccessDeniedException("You don't have permission to update this task");
        } else if (currentUser.getRole() == Role.MANAGER && !projectService.isProjectOwner(task.getProject().getId())) {
            throw new AccessDeniedException("You don't have permission to update tasks in this project");
        }

        if (!task.getProject().getId().equals(requestDto.getProjectId())) {
            throw new IllegalArgumentException("Cannot change the project of an existing task");
        }

        User assignedUser = null;
        if (requestDto.getAssignedUserId() != null) {
            assignedUser = userRepository.findById(requestDto.getAssignedUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", requestDto.getAssignedUserId()));
        }

        Task updatedTask = taskMapper.updateEntity(task, requestDto, task.getProject(), assignedUser);
        Task savedTask = taskRepository.save(updatedTask);
        return taskMapper.toResponseDto(savedTask);
    }

    @Override
    @Transactional
    public TaskResponseDto updateTaskStatus(Long id, TaskStatus status) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        User currentUser = userService.getCurrentUser();
        
        if (currentUser.getRole() == Role.USER && !isAssignedToTask(id)) {
            throw new AccessDeniedException("You can only update status of tasks assigned to you");
        }

        else if (currentUser.getRole() == Role.MANAGER &&
                !projectService.isProjectOwner(task.getProject().getId())) {
            throw new AccessDeniedException("You don't have permission to update tasks in this project");
        }

        task.setStatus(status);
        Task updatedTask = taskRepository.save(task);

        return taskMapper.toResponseDto(updatedTask);
    }

    @Override
    @Transactional
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        if (!userService.isAdmin() && !projectService.isProjectOwner(task.getProject().getId())) {
            throw new AccessDeniedException("Only project owners and admins can delete tasks");
        }

        taskRepository.deleteById(id);
    }

    @Override
    public boolean isAssignedToTask(Long taskId) {
        User currentUser = userService.getCurrentUser();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        return task.getAssignedUser() != null && task.getAssignedUser().getId().equals(currentUser.getId());
    }

    @Override
    public boolean hasTaskAccess(Long taskId) {
        User currentUser = userService.getCurrentUser();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        if (currentUser.getRole() == Role.ADMIN) {
            return true;
        }

        if (currentUser.getRole() == Role.MANAGER &&
                projectService.isProjectOwner(task.getProject().getId())) {
            return true;
        }

        return currentUser.getRole() == Role.USER && isAssignedToTask(taskId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponseDto> getAllTasks(Pageable pageable) {
        User currentUser = userService.getCurrentUser();
        Page<Task> tasksPage;

        if (currentUser.getRole() == Role.ADMIN) {
            tasksPage = taskRepository.findAll(pageable);
        } else if (currentUser.getRole() == Role.MANAGER) {
            List<Project> projects = projectRepository.findByOwner(currentUser);

            if (projects.isEmpty()) {
                return new PageImpl<>(List.of(), pageable, 0);
            }
    
            List<Long> projectIds = projects.stream()
                    .map(Project::getId)
                    .collect(Collectors.toList());
    
            tasksPage = taskRepository.findByProjectIdIn(projectIds, pageable);
        } else {
            tasksPage = taskRepository.findByAssignedUserId(currentUser.getId(), pageable);
        }
    
        return tasksPage.map(taskMapper::toResponseDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponseDto> getTasksByStatus(TaskStatus status, Pageable pageable) {
        User currentUser = userService.getCurrentUser();
        Page<Task> tasksPage;
    
        if (currentUser.getRole() == Role.ADMIN) {
            tasksPage = taskRepository.findByStatus(status, pageable);
        } else if (currentUser.getRole() == Role.MANAGER) {
            List<Project> projects = projectRepository.findByOwner(currentUser);
    
            if (projects.isEmpty()) {
                return new org.springframework.data.domain.PageImpl<>(List.of(), pageable, 0);
            }
    
            List<Long> projectIds = projects.stream()
                    .map(Project::getId)
                    .collect(Collectors.toList());
    
            tasksPage = taskRepository.findByStatusAndProjectIdIn(status, projectIds, pageable);
        } else {
            tasksPage = taskRepository.findByStatusAndAssignedUserId(status, currentUser.getId(), pageable);
        }
    
        return tasksPage.map(taskMapper::toResponseDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponseDto> getTasksByPriority(Priority priority, Pageable pageable) {
        User currentUser = userService.getCurrentUser();
        Page<Task> tasksPage;
    
        if (currentUser.getRole() == Role.ADMIN) {
            tasksPage = taskRepository.findByPriority(priority, pageable);
        } else if (currentUser.getRole() == Role.MANAGER) {
            List<Project> projects = projectRepository.findByOwner(currentUser);
    
            if (projects.isEmpty()) {
                return new org.springframework.data.domain.PageImpl<>(List.of(), pageable, 0);
            }
    
            List<Long> projectIds = projects.stream()
                    .map(Project::getId)
                    .collect(Collectors.toList());
    
            tasksPage = taskRepository.findByPriorityAndProjectIdIn(priority, projectIds, pageable);
        } else {
            tasksPage = taskRepository.findByPriorityAndAssignedUserId(priority, currentUser.getId(), pageable);
        }
    
        return tasksPage.map(taskMapper::toResponseDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponseDto> getTasksByStatusAndPriority(TaskStatus status, Priority priority, Pageable pageable) {
        User currentUser = userService.getCurrentUser();
        Page<Task> tasksPage;
    
        if (currentUser.getRole() == Role.ADMIN) {
            tasksPage = taskRepository.findByStatusAndPriority(status, priority, pageable);
        } else if (currentUser.getRole() == Role.MANAGER) {
            List<Project> projects = projectRepository.findByOwner(currentUser);
    
            if (projects.isEmpty()) {
                return new org.springframework.data.domain.PageImpl<>(List.of(), pageable, 0);
            }
    
            List<Long> projectIds = projects.stream()
                    .map(Project::getId)
                    .collect(Collectors.toList());
    
            tasksPage = taskRepository.findByStatusAndPriorityAndProjectIdIn(status, priority, projectIds, pageable);
        } else {
            tasksPage = taskRepository.findByStatusAndPriorityAndAssignedUserId(status, priority, currentUser.getId(), pageable);
        }
    
        return tasksPage.map(taskMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponseDto> getTasksByProjectId(Long projectId, Pageable pageable) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        if (!projectService.hasProjectAccess(projectId)) {
            throw new AccessDeniedException("You don't have access to this project");
        }

        Page<Task> tasksPage = taskRepository.findByProjectId(projectId, pageable);
        return tasksPage.map(taskMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponseDto> getTasksByAssignedUserId(Long userId, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        User currentUser = userService.getCurrentUser();

        if (currentUser.getRole() == Role.USER && !currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("You can only view your own tasks");
        }

        Page<Task> tasksPage;

        if (currentUser.getRole() == Role.MANAGER && !currentUser.getId().equals(userId)) {
            List<Project> managerProjects = projectRepository.findByOwner(currentUser);
            List<Long> projectIds = managerProjects.stream()
                    .map(Project::getId)
                    .collect(Collectors.toList());

            if (projectIds.isEmpty()) {
                return new PageImpl<>(List.of(), pageable, 0);
            }

            tasksPage = taskRepository.findByAssignedUserIdAndProjectIdIn(userId, projectIds, pageable);
        } else {
            tasksPage = taskRepository.findByAssignedUserId(userId, pageable);
        }

        return tasksPage.map(taskMapper::toResponseDto);
    }
}