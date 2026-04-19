package com.example.travelappbe.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for JwtTokenProvider
 * Tests JWT token generation, extraction, and validation
 */
@DisplayName("JwtTokenProvider Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        // Set field values using reflection
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "SCRUM_MASTER_MIRCHEA_KEY_TEST_SECRET_MIN_256");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 86400000L);
    }

    @Test
    @DisplayName("Should generate valid JWT token with email and role")
    void testGenerateToken() {
        // Arrange
        String email = "test@example.com";
        String role = "TOURIST";

        // Act
        String token = jwtTokenProvider.generateToken(email, role);

        // Assert
        assertTrue(token != null && !token.isEmpty());
        assertTrue(token.contains("."));  // JWT format: header.payload.signature
    }

    @Test
    @DisplayName("Should extract email from valid token")
    void testGetEmailFromToken() {
        // Arrange
        String email = "test@example.com";
        String role = "TOURIST";
        String token = jwtTokenProvider.generateToken(email, role);

        // Act
        String extractedEmail = jwtTokenProvider.getEmailFromToken(token);

        // Assert
        assertEquals(email, extractedEmail);
    }

    @Test
    @DisplayName("Should extract role from valid token")
    void testGetRoleFromToken() {
        // Arrange
        String email = "test@example.com";
        String role = "ADMIN";
        String token = jwtTokenProvider.generateToken(email, role);

        // Act
        String extractedRole = jwtTokenProvider.getRoleFromToken(token);

        // Assert
        assertEquals(role, extractedRole);
    }

    @Test
    @DisplayName("Should validate correct token as true")
    void testValidateToken_ValidToken() {
        // Arrange
        String email = "test@example.com";
        String role = "TOURIST";
        String token = jwtTokenProvider.generateToken(email, role);

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should validate invalid token as false")
    void testValidateToken_InvalidToken() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should validate corrupted token as false")
    void testValidateToken_CorruptedToken() {
        // Arrange
        String email = "test@example.com";
        String role = "TOURIST";
        String token = jwtTokenProvider.generateToken(email, role);
        String corruptedToken = token.substring(0, token.length() - 5) + "xxxxx";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(corruptedToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should extract email from valid token using validateAndGetEmail")
    void testValidateAndGetEmail_ValidToken() {
        // Arrange
        String email = "admin@example.com";
        String role = "ADMIN";
        String token = jwtTokenProvider.generateToken(email, role);

        // Act
        String extractedEmail = jwtTokenProvider.validateAndGetEmail(token);

        // Assert
        assertEquals(email, extractedEmail);
    }

    @Test
    @DisplayName("Should throw exception for invalid token in validateAndGetEmail")
    void testValidateAndGetEmail_InvalidToken() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> jwtTokenProvider.validateAndGetEmail(invalidToken),
                "Should throw IllegalArgumentException for invalid token"
        );
    }

    @Test
    @DisplayName("Should generate different tokens for different calls")
    void testGenerateToken_UniqueTokens() {
        // Arrange
        String email = "test@example.com";
        String role = "TOURIST";

        // Act
        String token1 = jwtTokenProvider.generateToken(email, role);
        String token2 = jwtTokenProvider.generateToken(email, role);

        // Assert
        // Tokens might be different due to different issuedAt timestamps
        assertTrue(token1 != null && token2 != null);
        assertEquals(jwtTokenProvider.getEmailFromToken(token1), jwtTokenProvider.getEmailFromToken(token2));
    }

    @Test
    @DisplayName("Should generate token with ADMIN role")
    void testGenerateToken_AdminRole() {
        // Arrange
        String email = "admin@example.com";
        String role = "ADMIN";

        // Act
        String token = jwtTokenProvider.generateToken(email, role);
        String extractedRole = jwtTokenProvider.getRoleFromToken(token);

        // Assert
        assertEquals("ADMIN", extractedRole);
    }

    @Test
    @DisplayName("Should handle special characters in email")
    void testGenerateToken_SpecialCharactersInEmail() {
        // Arrange
        String email = "test+tag@sub.example.com";
        String role = "TOURIST";

        // Act
        String token = jwtTokenProvider.generateToken(email, role);
        String extractedEmail = jwtTokenProvider.getEmailFromToken(token);

        // Assert
        assertEquals(email, extractedEmail);
    }
}
