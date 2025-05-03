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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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


    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDto> getAllTasks() {
        User currentUser = userService.getCurrentUser();
        List<Task> tasks;

        if (currentUser.getRole() == Role.ADMIN) {
            // Admins can see all tasks
            tasks = taskRepository.findAll();
        } else if (currentUser.getRole() == Role.MANAGER) {
            // Managers can see tasks in their own projects
            List<Project> projects = projectRepository.findByOwner(currentUser);
            tasks = projects.stream()
                    .flatMap(project -> taskRepository.findByProject(project).stream())
                    .collect(Collectors.toList());
        } else {
            // Users can see only their own assigned tasks
            tasks = taskRepository.findByAssignedUser(currentUser);
        }

        return tasks.stream()
                .map(taskMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDto> getTasksByProjectId(Long projectId) {
        // Check if project exists
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        // Check if user has access to this project
        if (!projectService.hasProjectAccess(projectId)) {
            throw new AccessDeniedException("You don't have access to this project");
        }

        List<Task> tasks = taskRepository.findByProject(project);
        return tasks.stream()
                .map(taskMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDto> getTasksByAssignedUserId(Long userId) {
        // Check if user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        User currentUser = userService.getCurrentUser();

        // Check if user has permission to view these tasks
        if (currentUser.getRole() == Role.USER && !currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("You can only view your own tasks");
        }

        List<Task> tasks = taskRepository.findByAssignedUser(user);

        // If manager, filter to only show tasks in their projects
        if (currentUser.getRole() == Role.MANAGER && !currentUser.getId().equals(userId)) {
            List<Project> managerProjects = projectRepository.findByOwner(currentUser);
            tasks = tasks.stream()
                    .filter(task -> managerProjects.contains(task.getProject()))
                    .toList();
        }

        return tasks.stream()
                .map(taskMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponseDto getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        // Check if user has access to this task
        if (!hasTaskAccess(id)) {
            throw new AccessDeniedException("You don't have access to this task");
        }

        return taskMapper.toResponseDto(task);
    }

    @Override
    @Transactional
    public TaskResponseDto createTask(TaskRequestDto requestDto) {
        // Check if project exists
        Project project = projectRepository.findById(requestDto.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", requestDto.getProjectId()));

        // Check if user has permission to create tasks in this project
        if (!userService.isAdmin() && !projectService.isProjectOwner(requestDto.getProjectId())) {
            throw new AccessDeniedException("You don't have permission to create tasks in this project");
        }

        // Check if assigned user exists (if provided)
        User assignedUser = null;
        if (requestDto.getAssignedUserId() != null) {
            assignedUser = userRepository.findById(requestDto.getAssignedUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", requestDto.getAssignedUserId()));
        }

        // Create new task
        Task task = taskMapper.toEntity(requestDto, project, assignedUser);
        Task savedTask = taskRepository.save(task);
        return taskMapper.toResponseDto(savedTask);
    }

    @Override
    @Transactional
    public TaskResponseDto updateTask(Long id, TaskRequestDto requestDto) {
        // Check if task exists
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        // Check if user has permission to update this task
        User currentUser = userService.getCurrentUser();
        if (currentUser.getRole() == Role.USER && !isAssignedToTask(id)) {
            throw new AccessDeniedException("You don't have permission to update this task");
        } else if (currentUser.getRole() == Role.MANAGER && !projectService.isProjectOwner(task.getProject().getId())) {
            throw new AccessDeniedException("You don't have permission to update tasks in this project");
        }

        // Check if project exists and is the same (can't change project)
        if (!task.getProject().getId().equals(requestDto.getProjectId())) {
            throw new IllegalArgumentException("Cannot change the project of an existing task");
        }

        // Check if assigned user exists (if provided)
        User assignedUser = null;
        if (requestDto.getAssignedUserId() != null) {
            assignedUser = userRepository.findById(requestDto.getAssignedUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", requestDto.getAssignedUserId()));
        }

        // Update task
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
            throw new AccessDeniedException("You don't have permission to update this task");
        } else if (currentUser.getRole() == Role.MANAGER && !projectService.isProjectOwner(task.getProject().getId())) {
            throw new AccessDeniedException("You don't have permission to update tasks in this project");
        }

        task.setStatus(status);
        Task updatedTask = taskRepository.save(task);

        return taskMapper.toResponseDto(updatedTask);
    }

    @Override
    @Transactional
    public void deleteTask(Long id) {
        // Check if task exists
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        // Check if user has permission to delete this task
        if (!userService.isAdmin() && !projectService.isProjectOwner(task.getProject().getId())) {
            throw new AccessDeniedException("Only project owners and admins can delete tasks");
        }

        // Delete task
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

        // Admins have access to all tasks
        if (currentUser.getRole() == Role.ADMIN) {
            return true;
        }

        // Managers have access to tasks in their own projects
        if (currentUser.getRole() == Role.MANAGER && 
                projectService.isProjectOwner(task.getProject().getId())) {
            return true;
        }

        // Users have access to their own assigned tasks
        if (currentUser.getRole() == Role.USER && isAssignedToTask(taskId)) {
            return true;
        }

        return false;
    }

    @Override
    public Page<TaskResponseDto> getAllTasks(Pageable pageable) {
        return null;
    }

    @Override
    public Page<TaskResponseDto> getTasksByStatus(TaskStatus status, Pageable pageable) {
        return null;
    }

    @Override
    public Page<TaskResponseDto> getTasksByPriority(Priority priority, Pageable pageable) {
        return null;
    }

    @Override
    public Page<TaskResponseDto> getTasksByStatusAndPriority(TaskStatus status, Priority priority, Pageable pageable) {
        return null;
    }

    @Override
    public Page<TaskResponseDto> getTasksByProjectId(Long projectId, Pageable pageable) {
        return null;
    }

    @Override
    public Page<TaskResponseDto> getTasksByAssignedUserId(Long userId, Pageable pageable) {
        return null;
    }
}