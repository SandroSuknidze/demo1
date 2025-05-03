package com.example.demo.controller;

import com.example.demo.dto.auth.JwtResponseDto;
import com.example.demo.dto.auth.LoginRequestDto;
import com.example.demo.dto.auth.RegisterRequestDto;
import com.example.demo.dto.user.UserRequestDto;
import com.example.demo.dto.user.UserResponseDto;
import com.example.demo.model.entity.User;
import com.example.demo.model.enums.Role;
import com.example.demo.security.jwt.JwtUtils;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserService userService;

    /**
     * Authenticate a user and generate a JWT token.
     *
     * @param loginRequest the login request
     * @return the JWT response
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> authenticateUser(@Valid @RequestBody LoginRequestDto loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        
        User user = userService.getCurrentUser();
        
        return ResponseEntity.ok(new JwtResponseDto(
                jwt,
                user.getId(),
                user.getEmail(),
                user.getRole()));
    }

    /**
     * Register a new user.
     *
     * @param registerRequest the registration request
     * @return the created user
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> registerUser(@Valid @RequestBody RegisterRequestDto registerRequest) {
        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setEmail(registerRequest.getEmail());
        userRequestDto.setPassword(registerRequest.getPassword());
        userRequestDto.setRole(Role.USER);
        
        return ResponseEntity.ok(userService.createUser(userRequestDto));
    }
}