package com.example.travelappbe.service;

import com.example.travelappbe.entity.PasswordResetToken;
import com.example.travelappbe.entity.User;
import com.example.travelappbe.repository.PasswordResetTokenRepository;
import com.example.travelappbe.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(UserRepository userRepository, PasswordResetTokenRepository tokenRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public String generateResetToken(String email) {
        // Dacă metoda ta din UserRepository se numește altfel (ex: getUser(), te rog să o adaptezi)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User nu a fost găsit"));

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user, LocalDateTime.now().plusHours(1));
        tokenRepository.save(resetToken);

        System.out.println("MOCK EMAIL -> Link/Token resetare parolă pentru " + email + ": " + token);
        return token;
    }

    @Transactional
    public void updatePassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token invalid"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken);
            throw new IllegalArgumentException("Token-ul a expirat");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword)); // Folosește numele corect al setter-ului aici dacă diferă
        userRepository.save(user);
        tokenRepository.delete(resetToken); // Ștergem token-ul după utilizare pentru securitate
    }
}