package com.example.travelappbe.controller;

import com.example.travelappbe.dto.ContactRequestDto;
import com.example.travelappbe.security.JwtTokenProvider;
import com.example.travelappbe.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    private final EmailService emailService;
    private final JwtTokenProvider jwtTokenProvider;

    public ContactController(EmailService emailService, JwtTokenProvider jwtTokenProvider) {
        this.emailService = emailService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping
    public ResponseEntity<Void> sendContactMessage(
            HttpServletRequest request,
            @Valid @RequestBody ContactRequestDto contactRequestDto) {

        String email = extractEmailFromToken(request);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        emailService.sendContactMessage(email, contactRequestDto.getSubject(), contactRequestDto.getMessage());
        return ResponseEntity.ok().build();
    }

    private String extractEmailFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                return jwtTokenProvider.validateAndGetEmail(token);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}