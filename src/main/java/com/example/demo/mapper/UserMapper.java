package com.example.demo.mapper;

import com.example.demo.dto.user.UserRequestDto;
import com.example.demo.dto.user.UserResponseDto;
import com.example.demo.model.entity.User;
import org.mapstruct.Mapper;

/**
 * Mapper class for converting between User entity and DTOs.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Convert a User entity to a UserResponseDto.
     *
     * @param user the User entity to convert
     * @return the UserResponseDto
     */
    UserResponseDto toResponseDto(User user);


    /**
     * Convert a UserRequestDto to a new User entity.
     *
     * @param requestDto the UserRequestDto to convert
     * @return the User entity
     */
    User toEntity(UserRequestDto requestDto);
}