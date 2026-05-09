package com.example.travelappbe.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.travelappbe.dto.NewVisitRequestDto;
import com.example.travelappbe.entity.AnalyticsVisit;
import com.example.travelappbe.repository.AnalyticsVisitRepository;
import com.example.travelappbe.repository.UserRepository;
import com.example.travelappbe.security.JwtTokenProvider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class NewVisitController {

    private final AnalyticsVisitRepository analyticsVisitRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public NewVisitController(AnalyticsVisitRepository analyticsVisitRepository,
                              JwtTokenProvider jwtTokenProvider,
                              UserRepository userRepository) {
        this.analyticsVisitRepository = analyticsVisitRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @PostMapping("/newVisit")
    public ResponseEntity<?> recordNewVisit(
            HttpServletRequest request,
            @Valid @RequestBody NewVisitRequestDto dto) {

        // 1. Extrage userId din Bearer Token
        UUID userId = extractUserIdFromToken(request);
        
        // 2. Daca nu exista un token valid, fallback pe userId venit din frontend
        if (userId == null && dto.getUserId() != null) {
            userId = dto.getUserId();
        }

        // 3. Salveaza in baza de date direct (analytics_visits)
        AnalyticsVisit visit = new AnalyticsVisit(dto.getObjectiveId(), userId);
        AnalyticsVisit savedVisit = analyticsVisitRepository.save(visit);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Visit recorded successfully",
                "visitId", savedVisit.getId()
        ));
    }

    private UUID extractUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                String email = jwtTokenProvider.validateAndGetEmail(token);
                return userRepository.findByEmail(email).map(user -> user.getId()).orElse(null);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}