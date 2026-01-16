package com.feedbacks.FeedbackSystem.Exception;

import com.feedbacks.FeedbackSystem.DTO.ApiResponse;
import com.feedbacks.FeedbackSystem.DTO.analytics.RateLimitInfo;
import com.feedbacks.FeedbackSystem.Exception.dto.ExceptionResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
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
    public ResponseEntity<ApiResponse<?>> entityNotFoundException(EntityNotFoundException e,
                                                                HttpServletRequest request){
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.isError(),
                e.getLocalizedMessage(),
                new ExceptionResponseDTO(
                        request.getRequestURI(),
                        HttpStatus.NOT_FOUND.value(),
                        e.getMessage(),
                        "Not found!",
                        Instant.now()
                )
        ));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleResourceNotFoundException(ResourceNotFoundException e,
                                                             HttpServletRequest request){
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.isError(),
                e.getLocalizedMessage(),
                new ExceptionResponseDTO(
                        request.getRequestURI(),
                        HttpStatus.NOT_FOUND.value(),
                        e.getMessage(),
                        "Not found!",
                        Instant.now()
                )
        ));
    }

    @ExceptionHandler(NotAllowedException.class)
    public ResponseEntity<ApiResponse<?>> handleNotAllowedException(NotAllowedException e,
                                                                          HttpServletRequest request){
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.isError(),
                e.getLocalizedMessage(),
                new ExceptionResponseDTO(
                        request.getRequestURI(),
                        429,
                        e.getMessage(),
                        "Not allowed to do this action",
                        Instant.now()
                )
        ));
    }

    private HttpHeaders buildRateLimitHeaders(RateLimitInfo info) {

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-RateLimit-Limit", String.valueOf(info.limit()));
        headers.add("X-RateLimit-Remaining", String.valueOf(info.remaining()));
        headers.add("X-RateLimit-Reset", info.resetAt());

        return headers;
    }

    @ExceptionHandler(TooManyRequestException.class)
    public ResponseEntity<ApiResponse<?>> handleTooManyRequestException(TooManyRequestException e,
                                                                        HttpServletRequest request) {
        RateLimitInfo limitInfo = e.getRateLimitInfo();
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .headers(buildRateLimitHeaders(limitInfo))
                .body(new ApiResponse<>(
                        HttpStatus.OK.isError(),
                        e.getLocalizedMessage(),
                        new ExceptionResponseDTO(
                                request.getRequestURI(),
                                429,
                                e.getMessage(),
                                "Too many requests reached",
                                Instant.now()
                        )
        ));
    }

    @ExceptionHandler(MailSendingFailedException.class)
    public ResponseEntity<ApiResponse<?>> handleMailSendingFailedException(MailSendingFailedException e,
                                                                    HttpServletRequest request){
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.isError(),
                e.getLocalizedMessage(),
                new ExceptionResponseDTO(
                        request.getRequestURI(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        e.getMessage(),
                        "Mail sending failed",
                        Instant.now()
                )
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex, HttpServletRequest request){
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.isError(),
                ex.getLocalizedMessage(),
                new ExceptionResponseDTO(
                        request.getRequestURI(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        ex.getMessage(),
                        "INTERNAL_SERVER_ERROR",
                        Instant.now()
                )
        ));
    }
}
