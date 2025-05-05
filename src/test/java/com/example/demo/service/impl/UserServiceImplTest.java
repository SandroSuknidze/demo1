package com.example.demo.service.impl;

import com.example.demo.dto.user.UserRequestDto;
import com.example.demo.dto.user.UserResponseDto;
import com.example.demo.exception.AccessDeniedException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.entity.Task;
import com.example.demo.model.entity.User;
import com.example.demo.model.enums.Role;
import com.example.demo.repository.TaskRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private UserServiceImpl userService;

    private User adminUser;
    private User regularUser;
    private UserRequestDto userRequestDto;
    private UserResponseDto userResponseDto;

    @BeforeEach
    void setUp() {
        // Setup test data
        adminUser = User.builder()
                .id(1L)
                .email("admin@example.com")
                .password("encodedPassword")
                .role(Role.ADMIN)
                .createDate(LocalDateTime.now())
                .build();

        regularUser = User.builder()
                .id(2L)
                .email("user@example.com")
                .password("encodedPassword")
                .role(Role.USER)
                .createDate(LocalDateTime.now())
                .build();

        userRequestDto = UserRequestDto.builder()
                .email("newuser@example.com")
                .password("password")
                .role(Role.USER)
                .build();

        userResponseDto = UserResponseDto.builder()
                .id(2L)
                .email("user@example.com")
                .role(Role.USER)
                .createDate(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Arrange
        List<User> users = List.of(adminUser, regularUser);
        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toResponseDto(adminUser)).thenReturn(
                UserResponseDto.builder().id(1L).email("admin@example.com").role(Role.ADMIN).build());
        when(userMapper.toResponseDto(regularUser)).thenReturn(
                UserResponseDto.builder().id(2L).email("user@example.com").role(Role.USER).build());

        // Act
        List<UserResponseDto> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("admin@example.com", result.get(0).getEmail());
        assertEquals("user@example.com", result.get(1).getEmail());
        verify(userRepository).findAll();
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        // Arrange
        when(userRepository.findById(2L)).thenReturn(Optional.of(regularUser));
        when(userMapper.toResponseDto(regularUser)).thenReturn(userResponseDto);

        // Act
        UserResponseDto result = userService.getUserById(2L);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("user@example.com", result.getEmail());
        verify(userRepository).findById(2L);
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(99L));
        verify(userRepository).findById(99L);
    }

    @Test
    void createUser_WhenEmailIsUnique_ShouldCreateUser() {
        // Arrange
        User newUser = User.builder()
                .email("newuser@example.com")
                .password("rawPassword")
                .role(Role.USER)
                .build();

        User savedUser = User.builder()
                .id(3L)
                .email("newuser@example.com")
                .password("encodedPassword")
                .role(Role.USER)
                .createDate(LocalDateTime.now())
                .build();

        UserResponseDto savedUserDto = UserResponseDto.builder()
                .id(3L)
                .email("newuser@example.com")
                .role(Role.USER)
                .createDate(LocalDateTime.now())
                .build();

        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(userMapper.toEntity(userRequestDto)).thenReturn(newUser);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(newUser)).thenReturn(savedUser);
        when(userMapper.toResponseDto(savedUser)).thenReturn(savedUserDto);

        // Act
        UserResponseDto result = userService.createUser(userRequestDto);

        // Assert
        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("newuser@example.com", result.getEmail());
        verify(userRepository).save(newUser);
        verify(passwordEncoder).encode("password");
    }

    @Test
    void createUser_WhenEmailExists_ShouldThrowIllegalArgumentException() {
        // Arrange
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(userRequestDto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_WhenUserExistsAndCurrentUserIsAdmin_ShouldDeleteUser() {
        // Arrange
        when(userRepository.findById(2L)).thenReturn(Optional.of(regularUser));

        // Mock SecurityContextHolder
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userDetails.getUsername()).thenReturn("admin@example.com");
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));

        // Mock taskRepository
        Page<Task> emptyPage = mock(Page.class);
        when(emptyPage.isEmpty()).thenReturn(true);
        when(taskRepository.findByAssignedUserId(eq(2L), any(Pageable.class))).thenReturn(emptyPage);

        // Act
        userService.deleteUser(2L);

        // Assert
        verify(userRepository).delete(regularUser);
    }

    @Test
    void deleteUser_WhenUserDoesNotExist_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Mock SecurityContextHolder
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userDetails.getUsername()).thenReturn("admin@example.com");
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(99L));
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void deleteUser_WhenCurrentUserIsNotAdmin_ShouldThrowAccessDeniedException() {
        // Arrange
        // Mock SecurityContextHolder
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userDetails.getUsername()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(regularUser));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> userService.deleteUser(1L));
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void getCurrentUser_WhenAuthenticated_ShouldReturnCurrentUser() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(regularUser));

        // Act
        User result = userService.getCurrentUser();

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("user@example.com", result.getEmail());
    }

    @Test
    void getCurrentUser_WhenNotAuthenticated_ShouldReturnNull() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(authentication.isAuthenticated()).thenReturn(false);

        // Act
        User result = userService.getCurrentUser();

        // Assert
        assertNull(result);
    }

    @Test
    void isAdmin_WhenCurrentUserIsAdmin_ShouldReturnTrue() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("admin@example.com");
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));

        // Act
        boolean result = userService.isAdmin();

        // Assert
        assertTrue(result);
    }

    @Test
    void isAdmin_WhenCurrentUserIsNotAdmin_ShouldReturnFalse() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(regularUser));

        // Act
        boolean result = userService.isAdmin();

        // Assert
        assertFalse(result);
    }
}
