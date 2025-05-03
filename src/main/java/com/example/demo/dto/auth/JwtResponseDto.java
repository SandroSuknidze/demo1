package com.example.demo.dto.auth;

import com.example.demo.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponseDto {
    
    private String token;
    private String type = "Bearer";
    private Long id;
    private String email;
    private Role role;
    
    public JwtResponseDto(String token, Long id, String email, Role role) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.role = role;
    }
}