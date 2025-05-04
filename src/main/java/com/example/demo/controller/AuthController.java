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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Tag(name = "Authentication", description = "Authentication API for login and registration")
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
    @Operation(summary = "Authenticate user", description = "Authenticates a user and returns a JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully authenticated",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = JwtResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", 
                content = @Content)
    })
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
    @Operation(summary = "Register new user", description = "Registers a new user with USER role")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User successfully registered",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = UserResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input or email already in use", 
                content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> registerUser(@Valid @RequestBody RegisterRequestDto registerRequest) {
        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setEmail(registerRequest.getEmail());
        userRequestDto.setPassword(registerRequest.getPassword());
        userRequestDto.setRole(Role.USER);

        return ResponseEntity.ok(userService.createUser(userRequestDto));
    }
}
