package com.example.demo.dto.project;

import com.example.demo.dto.user.UserResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponseDto {
    
    private Long id;
    private String name;
    private String description;
    private UserResponseDto owner;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
}