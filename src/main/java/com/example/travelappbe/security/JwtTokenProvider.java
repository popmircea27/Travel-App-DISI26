package com.example.travelappbe.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * Utility component for JWT token generation and validation.
 * Handles creation and parsing of JWT tokens for authentication.
 */
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:SCRUM_MASTER_MIRCHEA_KEY}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationMs;

    /**
     * Get the signing key from the secret
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate a JWT token for the given email and role.
     *
     * @param email the email to include in the token
     * @param role the role to include in the token
     * @return JWT token string
     */
    public String generateToken(String email, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract email from JWT token.
     *
     * @param token the JWT token
     * @return email extracted from token
     */
    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Extract role from JWT token.
     *
     * @param token the JWT token
     * @return role extracted from token
     */
    public String getRoleFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

    /**
     * Validate if a JWT token is valid.
     *
     * @param token the JWT token to validate
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate a JWT token and extract the email if valid.
     *
     * @param token the JWT token to validate
     * @return email if token is valid
     * @throws Exception if token is invalid or expired
     */
    public String validateAndGetEmail(String token) {
        if (validateToken(token)) {
            return getEmailFromToken(token);
        }
        throw new IllegalArgumentException("Invalid or expired token");
    }
}
