package com.example.FeedbackSystem.security.dto;

import lombok.Data;

@Data
public class JwtRefreshTokenDTO {
    private String refreshToken;
}

//Spring will automatically map the incoming JSON body to this class using Jackson