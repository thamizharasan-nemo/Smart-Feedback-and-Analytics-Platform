package com.feedbacks.FeedbackSystem.controller;

import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.UserResponseDTO;
import com.feedbacks.FeedbackSystem.security.dto.JwtRefreshTokenDTO;
import com.feedbacks.FeedbackSystem.security.dto.JwtResponseDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.UserRequestDTO;
import com.feedbacks.FeedbackSystem.Exception.ResourceNotFoundException;
import com.feedbacks.FeedbackSystem.model.RefreshToken;
import com.feedbacks.FeedbackSystem.model.User;
import com.feedbacks.FeedbackSystem.repository.UserRepository;
import com.feedbacks.FeedbackSystem.security.CustomUserDetailsService;
import com.feedbacks.FeedbackSystem.security.JwtUtils;
import com.feedbacks.FeedbackSystem.service.RefreshTokenService;
import com.feedbacks.FeedbackSystem.service.UserService;
import com.feedbacks.FeedbackSystem.service.other_services.HtmlEmailBody;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepo;
    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final HtmlEmailBody emailBody;

    public AuthController(UserService userService, UserRepository userRepo, JwtUtils jwtUtils, CustomUserDetailsService userDetailsService, AuthenticationManager authenticationManager, RefreshTokenService refreshTokenService, HtmlEmailBody emailBody) {
        this.userService = userService;
        this.userRepo = userRepo;
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;
        this.emailBody = emailBody;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRequestDTO user){
    UserResponseDTO savedUser = userService.addUser(user);
    emailBody.registrationEmail(savedUser);
    // because of Async in that method savedUser returned before the mail sent
         return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserRequestDTO user){
        String email = user.getEmail();
        String password = user.getPassword();
        try{
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    email,
                    password
            ));
        }
        catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        // generate access token
        String jwtToken = jwtUtils.generateToken(userDetails);

        // generate refresh token
        User dbUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid email"));

        RefreshToken refreshToken = refreshTokenService.issue(dbUser);

        return ResponseEntity.ok(new JwtResponseDTO(jwtToken, refreshToken.getToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody JwtRefreshTokenDTO refreshToken){
        String presented = refreshToken.getRefreshToken();
        RefreshToken current = refreshTokenService.verifyUsable(presented);
        User user = current.getUser();

        // Rotate old refresh token with a new one
        RefreshToken nextRefreshToken = refreshTokenService.rotate(current);

        // Generate new access token
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String newAccess = jwtUtils.generateToken(userDetails);

        return ResponseEntity.ok(new JwtResponseDTO(newAccess, nextRefreshToken.getToken()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> deleteRefreshToken(@PathVariable int userId){
        refreshTokenService.revokeAllForUser(userId);
        return ResponseEntity.ok().body("All refresh tokens has been deleted.");
    }

}
