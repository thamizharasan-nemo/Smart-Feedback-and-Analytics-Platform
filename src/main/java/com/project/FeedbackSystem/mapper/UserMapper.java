package com.example.FeedbackSystem.mapper;

import com.example.FeedbackSystem.DTO.EntityDTO.requestDTOs.UserRequestDTO;
import com.example.FeedbackSystem.DTO.EntityDTO.responseDTOs.UserResponseDTO;
import com.example.FeedbackSystem.model.User;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    private final PasswordEncoder passwordEncoder;

    public UserMapper(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public User toEntity(User user, @Valid UserRequestDTO userRequestDTO){
        user.setUsername(userRequestDTO.getUsername());
        user.setEmail(userRequestDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userRequestDTO.getPassword()));
        user.setRole(userRequestDTO.getRole());

        if(userRequestDTO.getRole().equals(User.Role.ADMIN)){
            user.setIdentityNo(userRequestDTO.getAdminId());
        }
        if(userRequestDTO.getRole().equals(User.Role.STUDENT)) {
            user.setIdentityNo(userRequestDTO.getRollNo());
        }

        return user;
    }

    public UserResponseDTO toResponse(User user){
        return new UserResponseDTO(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getIdentityNo(),
                String.valueOf(user.getUserCreatedAt())
        );
    }
}
