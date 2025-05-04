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
import com.example.demo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private UserService userService;

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private TaskServiceImpl taskService;

    private User adminUser;
    private User managerUser;
    private User regularUser;
    private Project project;
    private Task task;
    private TaskRequestDto taskRequestDto;
    private TaskResponseDto taskResponseDto;

    @BeforeEach
    void setUp() {
        // Setup test data
        adminUser = User.builder()
                .id(1L)
                .email("admin@example.com")
                .password("password")
                .role(Role.ADMIN)
                .build();

        managerUser = User.builder()
                .id(2L)
                .email("manager@example.com")
                .password("password")
                .role(Role.MANAGER)
                .build();

        regularUser = User.builder()
                .id(3L)
                .email("user@example.com")
                .password("password")
                .role(Role.USER)
                .build();

        project = Project.builder()
                .id(1L)
                .name("Test Project")
                .description("Test Project Description")
                .owner(managerUser)
                .createDate(LocalDateTime.now())
                .build();

        task = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Task Description")
                .status(TaskStatus.TODO)
                .dueDate(LocalDate.now().plusDays(7))
                .priority(Priority.MEDIUM)
                .project(project)
                .assignedUser(regularUser)
                .createDate(LocalDateTime.now())
                .build();

        taskRequestDto = TaskRequestDto.builder()
                .title("Test Task")
                .description("Test Task Description")
                .status(TaskStatus.TODO)
                .dueDate(LocalDate.now().plusDays(7))
                .priority(Priority.MEDIUM)
                .projectId(1L)
                .assignedUserId(3L)
                .build();

        taskResponseDto = TaskResponseDto.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Task Description")
                .status(TaskStatus.TODO)
                .dueDate(LocalDate.now().plusDays(7))
                .priority(Priority.MEDIUM)
                .build();
    }

    @Test
    void getTaskById_WhenTaskExists_AndUserIsAdmin_ShouldReturnTask() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userService.getCurrentUser()).thenReturn(adminUser);
        // Admin users always have access to tasks
        when(taskMapper.toResponseDto(task)).thenReturn(taskResponseDto);

        // Act
        TaskResponseDto result = taskService.getTaskById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Task", result.getTitle());
        // We don't verify the exact number of calls because hasTaskAccess also calls findById
    }

    @Test
    void getTaskById_WhenTaskDoesNotExist_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> taskService.getTaskById(99L));
        verify(taskRepository).findById(99L);
    }

    @Test
    void getTaskById_WhenUserIsNotAuthorized_ShouldThrowAccessDeniedException() {
        // Arrange
        // Create a task that the regular user doesn't have access to
        // by setting a different user as the assigned user
        User otherUser = User.builder()
                .id(4L)
                .email("other@example.com")
                .password("password")
                .role(Role.USER)
                .build();

        Task inaccessibleTask = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Task Description")
                .status(TaskStatus.TODO)
                .dueDate(LocalDate.now().plusDays(7))
                .priority(Priority.MEDIUM)
                .project(project)
                .assignedUser(otherUser)
                .createDate(LocalDateTime.now())
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(inaccessibleTask));
        when(userService.getCurrentUser()).thenReturn(regularUser);
        // Regular users only have access to tasks assigned to them

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> taskService.getTaskById(1L));
    }

    @Test
    void createTask_WhenUserIsAdmin_ShouldCreateTask() {
        // Arrange
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userService.isAdmin()).thenReturn(true);
        when(userRepository.findById(3L)).thenReturn(Optional.of(regularUser));
        when(taskMapper.toEntity(eq(taskRequestDto), eq(project), eq(regularUser))).thenReturn(task);
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toResponseDto(task)).thenReturn(taskResponseDto);

        // Act
        TaskResponseDto result = taskService.createTask(taskRequestDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Task", result.getTitle());
        verify(taskRepository).save(task);
    }

    @Test
    void createTask_WhenUserIsProjectOwner_ShouldCreateTask() {
        // Arrange
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userService.isAdmin()).thenReturn(false);
        when(projectService.isProjectOwner(1L)).thenReturn(true);
        when(userRepository.findById(3L)).thenReturn(Optional.of(regularUser));
        when(taskMapper.toEntity(eq(taskRequestDto), eq(project), eq(regularUser))).thenReturn(task);
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toResponseDto(task)).thenReturn(taskResponseDto);

        // Act
        TaskResponseDto result = taskService.createTask(taskRequestDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Task", result.getTitle());
        verify(taskRepository).save(task);
    }

    @Test
    void createTask_WhenUserIsNotAuthorized_ShouldThrowAccessDeniedException() {
        // Arrange
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userService.isAdmin()).thenReturn(false);
        when(projectService.isProjectOwner(1L)).thenReturn(false);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> taskService.createTask(taskRequestDto));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTaskStatus_WhenUserIsAdmin_ShouldUpdateStatus() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toResponseDto(task)).thenReturn(taskResponseDto);

        // Act
        TaskResponseDto result = taskService.updateTaskStatus(1L, TaskStatus.IN_PROGRESS);

        // Assert
        assertNotNull(result);
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
        verify(taskRepository).save(task);
    }

    @Test
    void updateTaskStatus_WhenUserIsAssignedToTask_ShouldUpdateStatus() {
        // Arrange
        // Create a task that is assigned to the regular user
        Task assignedTask = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Task Description")
                .status(TaskStatus.TODO)
                .dueDate(LocalDate.now().plusDays(7))
                .priority(Priority.MEDIUM)
                .project(project)
                .assignedUser(regularUser)
                .createDate(LocalDateTime.now())
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(assignedTask));
        when(userService.getCurrentUser()).thenReturn(regularUser);
        when(taskRepository.save(assignedTask)).thenReturn(assignedTask);
        when(taskMapper.toResponseDto(assignedTask)).thenReturn(taskResponseDto);

        // Act
        TaskResponseDto result = taskService.updateTaskStatus(1L, TaskStatus.IN_PROGRESS);

        // Assert
        assertNotNull(result);
        assertEquals(TaskStatus.IN_PROGRESS, assignedTask.getStatus());
        verify(taskRepository).save(assignedTask);
    }

    @Test
    void updateTaskStatus_WhenUserIsNotAuthorized_ShouldThrowAccessDeniedException() {
        // Arrange
        // Create a task that is not assigned to the regular user
        User otherUser = User.builder()
                .id(4L)
                .email("other@example.com")
                .password("password")
                .role(Role.USER)
                .build();

        Task unassignedTask = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Task Description")
                .status(TaskStatus.TODO)
                .dueDate(LocalDate.now().plusDays(7))
                .priority(Priority.MEDIUM)
                .project(project)
                .assignedUser(otherUser)
                .createDate(LocalDateTime.now())
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(unassignedTask));
        when(userService.getCurrentUser()).thenReturn(regularUser);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> taskService.updateTaskStatus(1L, TaskStatus.IN_PROGRESS));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void deleteTask_WhenUserIsAdmin_ShouldDeleteTask() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userService.isAdmin()).thenReturn(true);

        // Act
        taskService.deleteTask(1L);

        // Assert
        verify(taskRepository).deleteById(1L);
    }

    @Test
    void deleteTask_WhenUserIsProjectOwner_ShouldDeleteTask() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userService.isAdmin()).thenReturn(false);
        when(projectService.isProjectOwner(1L)).thenReturn(true);

        // Act
        taskService.deleteTask(1L);

        // Assert
        verify(taskRepository).deleteById(1L);
    }

    @Test
    void deleteTask_WhenUserIsNotAuthorized_ShouldThrowAccessDeniedException() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userService.isAdmin()).thenReturn(false);
        when(projectService.isProjectOwner(1L)).thenReturn(false);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> taskService.deleteTask(1L));
        verify(taskRepository, never()).deleteById(anyLong());
    }

    @Test
    void getAllTasks_WhenUserIsAdmin_ShouldReturnAllTasks() {
        // Arrange
        Pageable pageable = Pageable.unpaged();
        Page<Task> taskPage = new PageImpl<>(List.of(task));
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(taskRepository.findAll(pageable)).thenReturn(taskPage);
        when(taskMapper.toResponseDto(task)).thenReturn(taskResponseDto);

        // Act
        Page<TaskResponseDto> result = taskService.getAllTasks(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(taskRepository).findAll(pageable);
    }

    @Test
    void getAllTasks_WhenUserIsManager_ShouldReturnTasksInOwnedProjects() {
        // Arrange
        Pageable pageable = Pageable.unpaged();
        Page<Task> taskPage = new PageImpl<>(List.of(task));
        when(userService.getCurrentUser()).thenReturn(managerUser);
        when(projectRepository.findByOwner(managerUser)).thenReturn(List.of(project));
        when(taskRepository.findByProjectIdIn(List.of(1L), pageable)).thenReturn(taskPage);
        when(taskMapper.toResponseDto(task)).thenReturn(taskResponseDto);

        // Act
        Page<TaskResponseDto> result = taskService.getAllTasks(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(taskRepository).findByProjectIdIn(List.of(1L), pageable);
    }

    @Test
    void getAllTasks_WhenUserIsRegularUser_ShouldReturnAssignedTasks() {
        // Arrange
        Pageable pageable = Pageable.unpaged();
        Page<Task> taskPage = new PageImpl<>(List.of(task));
        when(userService.getCurrentUser()).thenReturn(regularUser);
        when(taskRepository.findByAssignedUserId(3L, pageable)).thenReturn(taskPage);
        when(taskMapper.toResponseDto(task)).thenReturn(taskResponseDto);

        // Act
        Page<TaskResponseDto> result = taskService.getAllTasks(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(taskRepository).findByAssignedUserId(3L, pageable);
    }
}
