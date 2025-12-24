package com.example.FeedbackSystem.Exception.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExceptionResponseDTO {

    private String path;
    private int statusCode;
    private String message;
    private String error;
    private Instant timeStamp;
}
