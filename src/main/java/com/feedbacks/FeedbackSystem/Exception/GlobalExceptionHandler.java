package com.feedbacks.FeedbackSystem.Exception;

import com.feedbacks.FeedbackSystem.Exception.dto.ExceptionResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolation(DataIntegrityViolationException ex){
        if(ex.getMessage().contains("roll_no")){
            return ResponseEntity.badRequest().body("Roll number already registered");
        }
        else if (ex.getMessage().contains("email")) {
            return ResponseEntity.badRequest().body("Email already registered");
        }
        return ResponseEntity.badRequest().body("Duplicate entry");
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> entityNotFoundException(EntityNotFoundException e,
                                                     HttpServletRequest request){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of(
                        "message: ", e.getMessage(),
                        "timestamp: ", LocalDate.now(),
                        "status: ", 404,
                        "error: ", "Not Found",
                        "path: ", request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFoundException(ResourceNotFoundException e,
                                                             HttpServletRequest request){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ExceptionResponseDTO(
                        request.getRequestURI(),
                        HttpStatus.NOT_FOUND.value(),
                        e.getMessage(),
                        "Resource not found!",
                        Instant.now()
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex, HttpServletRequest request){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ExceptionResponseDTO(
                        request.getRequestURI(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        ex.getMessage(),
                        "INTERNAL_SERVER_ERROR",
                        Instant.now()
                        )
        );
    }
}
