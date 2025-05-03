package com.example.demo.dto.user;

import com.example.demo.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    
    private Long id;
    private String email;
    private Role role;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
}