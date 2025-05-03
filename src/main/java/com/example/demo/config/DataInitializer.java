package com.example.demo.config;

import com.example.demo.model.entity.Project;
import com.example.demo.model.entity.Task;
import com.example.demo.model.entity.User;
import com.example.demo.model.enums.Priority;
import com.example.demo.model.enums.Role;
import com.example.demo.model.enums.TaskStatus;
import com.example.demo.repository.ProjectRepository;
import com.example.demo.repository.TaskRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Component to initialize the database with sample data on application startup.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        // Create users
        User admin = User.builder()
                .email("admin@example.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.ADMIN)
                .build();
        userRepository.save(admin);

        User manager1 = User.builder()
                .email("manager@example.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.MANAGER)
                .build();
        userRepository.save(manager1);

        User manager2 = User.builder()
                .email("manager2@example.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.MANAGER)
                .build();
        userRepository.save(manager2);

        User user1 = User.builder()
                .email("user@example.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.USER)
                .build();
        userRepository.save(user1);

        User user2 = User.builder()
                .email("user2@example.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.USER)
                .build();
        userRepository.save(user2);

        // Create projects
        Project project1 = Project.builder()
                .name("Website Redesign")
                .description("Redesign the company website with a modern look and feel")
                .owner(manager1)
                .build();
        projectRepository.save(project1);

        Project project2 = Project.builder()
                .name("Mobile App Development")
                .description("Develop a mobile app for both iOS and Android platforms")
                .owner(manager2)
                .build();
        projectRepository.save(project2);

        // Create tasks
        Task task1 = Task.builder()
                .title("Design Homepage")
                .description("Create wireframes and mockups for the homepage")
                .status(TaskStatus.TODO)
                .dueDate(LocalDate.now().plusDays(7))
                .priority(Priority.HIGH)
                .project(project1)
                .assignedUser(user1)
                .build();
        taskRepository.save(task1);

        Task task2 = Task.builder()
                .title("Implement User Authentication")
                .description("Set up user registration and login functionality")
                .status(TaskStatus.IN_PROGRESS)
                .dueDate(LocalDate.now().plusDays(14))
                .priority(Priority.MEDIUM)
                .project(project1)
                .assignedUser(user2)
                .build();
        taskRepository.save(task2);

        Task task3 = Task.builder()
                .title("Create App Wireframes")
                .description("Design the user interface for the mobile app")
                .status(TaskStatus.TODO)
                .dueDate(LocalDate.now().plusDays(10))
                .priority(Priority.HIGH)
                .project(project2)
                .assignedUser(user2)
                .build();
        taskRepository.save(task3);

        Task task4 = Task.builder()
                .title("Set Up CI/CD Pipeline")
                .description("Configure continuous integration and deployment")
                .status(TaskStatus.TODO)
                .dueDate(LocalDate.now().plusDays(21))
                .priority(Priority.LOW)
                .project(project2)
                .assignedUser(null)
                .build();
        taskRepository.save(task4);

        System.out.println("Sample data initialized successfully!");
    }
}