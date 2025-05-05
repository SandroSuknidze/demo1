package com.example.demo.service.impl;

import com.example.demo.dto.project.ProjectRequestDto;
import com.example.demo.dto.project.ProjectResponseDto;
import com.example.demo.dto.user.UserResponseDto;
import com.example.demo.exception.AccessDeniedException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.ProjectMapper;
import com.example.demo.model.entity.Project;
import com.example.demo.model.entity.Task;
import com.example.demo.model.entity.User;
import com.example.demo.model.enums.Role;
import com.example.demo.repository.ProjectRepository;
import com.example.demo.repository.TaskRepository;
import com.example.demo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private User adminUser;
    private User managerUser;
    private User regularUser;
    private Project project;
    private ProjectRequestDto projectRequestDto;
    private ProjectResponseDto projectResponseDto;
    private UserResponseDto ownerResponseDto;

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

        projectRequestDto = ProjectRequestDto.builder()
                .name("Test Project")
                .description("Test Project Description")
                .build();

        ownerResponseDto = UserResponseDto.builder()
                .id(2L)
                .email("manager@example.com")
                .role(Role.MANAGER)
                .build();

        projectResponseDto = ProjectResponseDto.builder()
                .id(1L)
                .name("Test Project")
                .description("Test Project Description")
                .owner(ownerResponseDto)
                .createDate(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllProjects_WhenUserIsAdmin_ShouldReturnAllProjects() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(projectRepository.findAll()).thenReturn(List.of(project));
        when(projectMapper.toResponseDto(project)).thenReturn(projectResponseDto);

        // Act
        List<ProjectResponseDto> result = projectService.getAllProjects();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Project", result.get(0).getName());
        verify(projectRepository).findAll();
    }

    @Test
    void getAllProjects_WhenUserIsManager_ShouldReturnOwnedProjects() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(managerUser);
        when(projectRepository.findByOwner(managerUser)).thenReturn(List.of(project));
        when(projectMapper.toResponseDto(project)).thenReturn(projectResponseDto);

        // Act
        List<ProjectResponseDto> result = projectService.getAllProjects();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Project", result.get(0).getName());
        verify(projectRepository).findByOwner(managerUser);
    }

    @Test
    void getProjectById_WhenProjectExistsAndUserHasAccess_ShouldReturnProject() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(managerUser);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMapper.toResponseDto(project)).thenReturn(projectResponseDto);

        // Act
        ProjectResponseDto result = projectService.getProjectById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Project", result.getName());
        verify(projectRepository, times(2)).findById(1L);
    }

    @Test
    void getProjectById_WhenProjectDoesNotExist_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> projectService.getProjectById(99L));
        verify(projectRepository).findById(99L);
    }

    @Test
    void getProjectById_WhenUserDoesNotHaveAccess_ShouldThrowAccessDeniedException() {
        // Arrange
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userService.getCurrentUser()).thenReturn(regularUser);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> projectService.getProjectById(1L));
        verify(projectRepository).findById(1L);
    }

    @Test
    void createProject_ShouldCreateProject() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(managerUser);
        when(projectMapper.toEntity(projectRequestDto, managerUser)).thenReturn(project);
        when(projectRepository.save(project)).thenReturn(project);
        when(projectMapper.toResponseDto(project)).thenReturn(projectResponseDto);

        // Act
        ProjectResponseDto result = projectService.createProject(projectRequestDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Project", result.getName());
        verify(projectRepository).save(project);
    }

    @Test
    void updateProject_WhenProjectExistsAndUserIsAdmin_ShouldUpdateProject() {
        // Arrange
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userService.isAdmin()).thenReturn(true);
        when(projectMapper.updateEntity(project, projectRequestDto)).thenReturn(project);
        when(projectRepository.save(project)).thenReturn(project);
        when(projectMapper.toResponseDto(project)).thenReturn(projectResponseDto);

        // Act
        ProjectResponseDto result = projectService.updateProject(1L, projectRequestDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Project", result.getName());
        verify(projectRepository).save(project);
    }

    @Test
    void updateProject_WhenProjectExistsAndUserIsOwner_ShouldUpdateProject() {
        // Arrange
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userService.isAdmin()).thenReturn(false);
        when(userService.getCurrentUser()).thenReturn(managerUser);
        when(projectMapper.updateEntity(project, projectRequestDto)).thenReturn(project);
        when(projectRepository.save(project)).thenReturn(project);
        when(projectMapper.toResponseDto(project)).thenReturn(projectResponseDto);

        // Act
        ProjectResponseDto result = projectService.updateProject(1L, projectRequestDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Project", result.getName());
        verify(projectRepository).save(project);
    }

    @Test
    void updateProject_WhenProjectDoesNotExist_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> projectService.updateProject(99L, projectRequestDto));
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void updateProject_WhenUserIsNotAuthorized_ShouldThrowAccessDeniedException() {
        // Arrange
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userService.isAdmin()).thenReturn(false);
        when(userService.getCurrentUser()).thenReturn(regularUser);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> projectService.updateProject(1L, projectRequestDto));
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void deleteProject_WhenProjectExistsAndUserIsAdmin_ShouldDeleteProject() {
        // Arrange
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userService.isAdmin()).thenReturn(true);

        // Act
        projectService.deleteProject(1L);

        // Assert
        verify(projectRepository).delete(project);
    }

    @Test
    void deleteProject_WhenProjectExistsAndUserIsOwner_ShouldDeleteProject() {
        // Arrange
        when(userService.isAdmin()).thenReturn(false);
        when(userService.getCurrentUser()).thenReturn(managerUser);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        // Act
        projectService.deleteProject(1L);

        // Assert
        verify(projectRepository).delete(project);
    }

    @Test
    void deleteProject_WhenProjectDoesNotExist_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> projectService.deleteProject(99L));
        verify(projectRepository, never()).delete(any(Project.class));
    }

    @Test
    void deleteProject_WhenUserIsNotAuthorized_ShouldThrowAccessDeniedException() {
        // Arrange
        when(userService.isAdmin()).thenReturn(false);
        when(userService.getCurrentUser()).thenReturn(regularUser);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> projectService.deleteProject(1L));
        verify(projectRepository, never()).delete(any(Project.class));
    }

    @Test
    void isProjectOwner_WhenUserIsOwner_ShouldReturnTrue() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(managerUser);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        // Act
        boolean result = projectService.isProjectOwner(1L);

        // Assert
        assertTrue(result);
    }

    @Test
    void isProjectOwner_WhenUserIsNotOwner_ShouldReturnFalse() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        // Act
        boolean result = projectService.isProjectOwner(1L);

        // Assert
        assertFalse(result);
    }

    @Test
    void hasProjectAccess_WhenUserIsAdmin_ShouldReturnTrue() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(adminUser);

        // Act
        boolean result = projectService.hasProjectAccess(1L);

        // Assert
        assertTrue(result);
    }

    @Test
    void hasProjectAccess_WhenUserIsManagerAndOwner_ShouldReturnTrue() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(managerUser);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        // Act
        boolean result = projectService.hasProjectAccess(1L);

        // Assert
        assertTrue(result);
    }

    @Test
    void hasProjectAccess_WhenUserIsRegularUserWithAssignedTask_ShouldReturnTrue() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(regularUser);
        when(taskRepository.findByAssignedUserAndProjectId(regularUser, 1L)).thenReturn(List.of(mock(Task.class)));

        // Act
        boolean result = projectService.hasProjectAccess(1L);

        // Assert
        assertTrue(result);
    }

    @Test
    void hasProjectAccess_WhenUserIsRegularUserWithoutAssignedTask_ShouldReturnFalse() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(regularUser);
        when(taskRepository.findByAssignedUserAndProjectId(regularUser, 1L)).thenReturn(Collections.emptyList());

        // Act
        boolean result = projectService.hasProjectAccess(1L);

        // Assert
        assertFalse(result);
    }
}
