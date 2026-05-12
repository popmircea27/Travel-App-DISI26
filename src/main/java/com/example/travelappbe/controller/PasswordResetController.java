package com.example.travelappbe.controller;

import com.example.travelappbe.dto.ForgotPasswordRequestDto;
import com.example.travelappbe.dto.ResetPasswordRequestDto;
import com.example.travelappbe.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto request) {
        try {
            String token = passwordResetService.generateResetToken(request.getEmail());
            return ResponseEntity.ok(Map.of(
                    "message", "Dacă adresa de email există în sistem, a fost generat un link de resetare.",
                    "mock_token", token // Notă: elimină 'mock_token' din răspunsul final pe producție!
            ));
        } catch (IllegalArgumentException e) {
            // Returnăm 200 OK oricum, pentru a preveni ghicirea email-urilor valide de către hackeri
            return ResponseEntity.ok(Map.of("message", "Dacă adresa de email există în sistem, a fost generat un link de resetare."));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequestDto request) {
        try {
            passwordResetService.updatePassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Parola a fost resetată cu succes."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}