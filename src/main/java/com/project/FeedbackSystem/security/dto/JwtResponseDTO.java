package com.example.FeedbackSystem.security.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class JwtResponseDTO {
    String accessToken;
    String RefreshToken;
}
