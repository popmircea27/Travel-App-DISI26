package com.example.travelappbe.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * CORS (Cross-Origin Resource Sharing) Configuration
 * Allows the frontend (running on localhost:5173) to make requests to this backend API
 */
@Configuration
public class CorsConfig {

    /**
     * Configure CORS for all endpoints
     * Allows requests from the Vite frontend development server
     * @return CorsConfigurationSource bean
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow requests from frontend (development)
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",      // Vite dev server
                "http://localhost:3000",       // Alternative port
                "http://127.0.0.1:5173"        // Loopback address
        ));
        
        // Allow these HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"
        ));
        
        // Allow all headers (but most importantly: Authorization for JWT tokens)
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With",
                "*"
        ));
        
        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Cache preflight requests for 1 hour
        configuration.setMaxAge(3600L);
        
        // Expose Authorization header in response if needed
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        
        // Register the CORS configuration for all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
