package com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs;

import com.feedbacks.FeedbackSystem.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequestDTO {

    @NotBlank(message = "Username is required")
    @Size(min = 3,max = 50, message = "Name must be 3-50 characters")
    private String username;

    @Column(unique = true)
    @NotBlank(message = "RollNo is required")
    @Size(min = 6, max = 10, message = "RollNo must be 6-10 characters")
    @Pattern(regexp = "^[123]\\d[A-Za-z]{2}\\d{2,3}$")
    private String rollNo;

    @Column(unique = true)
    @NotBlank(message = "Admin id is required")
    @Size(min = 4, max = 20)
    @Pattern(regexp = "^[0-9]{4}admin[0-9]{2}$")
    private String adminId;

    @Column(nullable = false, unique = true)
    @Email(message = "Invalid email format")
    private String email;

    @Column(nullable = false)
    @Size(min = 6, max = 20, message = "Password must be atLeast 6 characters")
    private String password;

    @Enumerated(EnumType.STRING)
    // enum value but given as string
    //ex: in postman we give role as "role": "STUDENT" -> as string
    @NotBlank(message = "Role must be either ADMIN or STUDENT")
    private User.Role role;
}
