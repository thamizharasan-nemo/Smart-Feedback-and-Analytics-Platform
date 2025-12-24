package com.example.FeedbackSystem.controller;

import com.example.FeedbackSystem.security.dto.JwtRefreshTokenDTO;
import com.example.FeedbackSystem.security.dto.JwtResponseDTO;
import com.example.FeedbackSystem.DTO.EntityDTO.requestDTOs.UserRequestDTO;
import com.example.FeedbackSystem.Exception.ResourceNotFoundException;
import com.example.FeedbackSystem.model.RefreshToken;
import com.example.FeedbackSystem.model.User;
import com.example.FeedbackSystem.repository.UserRepository;
import com.example.FeedbackSystem.security.CustomUserDetailsService;
import com.example.FeedbackSystem.security.JwtUtils;
import com.example.FeedbackSystem.service.RefreshTokenService;
import com.example.FeedbackSystem.service.UserService;
import com.example.FeedbackSystem.service.other_services.HtmlEmailBody;
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

    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepo;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    CustomUserDetailsService userDetailsService;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    RefreshTokenService refreshTokenService;

    @Autowired
    HtmlEmailBody emailBody;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRequestDTO user){
    User savedUser = userService.addUser(user);
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
