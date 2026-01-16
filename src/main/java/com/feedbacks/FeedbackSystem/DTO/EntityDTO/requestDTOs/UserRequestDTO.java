package com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs;

import com.feedbacks.FeedbackSystem.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserRequestDTO {

    @NotBlank(message = "Username is required")
    @Size(min = 3,max = 50, message = "Name must be 3-50 characters")
    private String username;

    @Column(unique = true)
    @NotBlank(message = "Identity no is required (RollNo or AdminId)")
    @Size(min = 6, max = 15, message = "Identity no must be 6-10 characters")
    @Pattern(regexp = "^([123]\\d[A-Za-z]{2}\\d{2,3})|([0-9]{4}admin[0-9]{2,3})$")
    private String identityNo;

    @Column(nullable = false, unique = true)
    @Email(message = "Invalid email format")
    private String email;

    @Column(nullable = false)
    @Size(min = 6, max = 20, message = "Password must be atLeast 6 characters")
    private String password;

    @Enumerated(EnumType.STRING)
    // enum value but given as string
    //ex: in postman we give role as "role": "STUDENT" -> as string
    @NotNull(message = "Role must be either ADMIN or STUDENT")
    private User.Role role;
}
