package com.healthapp.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT token provider for generating and validating JSON Web Tokens.
 * Uses HMAC SHA-256 algorithm with configurable secret key and expiration time (default 24 hours).
 */
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret; // Secret key for signing tokens

    @Value("${jwt.expiration}")
    private long jwtExpirationMs; // Token expiration time in milliseconds (default: 86400000 = 24 hours)

    /**
     * Generates JWT token from Spring Security authentication object.
     * Includes userId and role as claims for authorization purposes.
     */
    public String generateToken(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes()); // Generate HMAC key from secret

        return Jwts.builder()
                .subject(userDetails.getUsername()) // Subject = email
                .claim("userId", userDetails.getId().toString()) // Custom claim for user ID
                .claim("role", userDetails.getRole().name()) // Custom claim for role
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key) // Sign with HMAC SHA-256
                .compact();
    }

    /**
     * Extracts email (subject) from JWT token.
     * Used to identify the user making authenticated requests.
     */
    public String getEmailFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject(); // Subject contains email
    }

    /**
     * Validates JWT token signature and expiration.
     * Returns true if token is valid, false if expired or tampered.
     */
    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token); // Throws exception if invalid/expired
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("Invalid JWT token: " + e.getMessage());
        }
        return false;
    }
}