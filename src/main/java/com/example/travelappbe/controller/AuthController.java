package com.example.travelappbe.controller;

import com.example.travelappbe.dto.RegisterRequestDto;
import com.example.travelappbe.dto.RegisterResponseDto;
import com.example.travelappbe.dto.LoginRequestDto;
import com.example.travelappbe.dto.LoginResponseDto;
import com.example.travelappbe.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication operations (registration, login, etc.)
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Register a new user.
     *
     * @param registerRequestDto the registration request containing email and password
     * @return ResponseEntity with the registered user details
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> register(@Valid @RequestBody RegisterRequestDto registerRequestDto) {
        RegisterResponseDto responseDto = userService.registerUser(registerRequestDto);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    /**
     * Health check endpoint for the auth service.
     *
     * @return simple greeting message
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth service is running");
    }

    /**
     * Authenticate a user with email and password.
     *
     * @param loginRequestDto the login request containing email and password
     * @return ResponseEntity with the authenticated user details and JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        LoginResponseDto responseDto = userService.loginUser(loginRequestDto);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }
}
