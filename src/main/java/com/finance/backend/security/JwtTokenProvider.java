package com.finance.backend.security;

import com.finance.backend.enums.Role;
import com.finance.backend.enums.UserStatus;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @PostConstruct
    public void init() {
        // Validate JWT secret on startup
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new IllegalStateException("JWT secret is not configured. Please set jwt.secret property.");
        }
        
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                "JWT secret must be at least 256 bits (32 characters). Current length: " + keyBytes.length
            );
        }
        
        log.info("JWT Token Provider initialized successfully. Token expiration: {} ms", jwtExpiration);
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Long userId, String username, Role role, UserStatus status) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role.name())
                .claim("status", status.name())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("JWT token is expired: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.warn("Invalid JWT token format: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            log.warn("JWT signature validation failed - possible tampering detected: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            log.warn("JWT validation error: {}", e.getMessage());
            return false;
        }
    }

    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = getClaims(token);
            return Long.parseLong(claims.getSubject());
        } catch (NumberFormatException e) {
            log.error("Invalid user ID format in JWT token", e);
            throw new JwtException("Invalid token: user ID format error");
        }
    }

    public String getUsernameFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("username", String.class);
    }

    public Role getRoleFromToken(String token) {
        try {
            Claims claims = getClaims(token);
            String role = claims.get("role", String.class);
            return Role.valueOf(role);
        } catch (IllegalArgumentException e) {
            log.error("Invalid role in JWT token", e);
            throw new JwtException("Invalid token: role format error");
        }
    }

    public UserStatus getStatusFromToken(String token) {
        try {
            Claims claims = getClaims(token);
            String status = claims.get("status", String.class);
            // Handle tokens without status claim (backward compatibility)
            if (status == null) {
                return UserStatus.ACTIVE;
            }
            return UserStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            log.error("Invalid status in JWT token", e);
            throw new JwtException("Invalid token: status format error");
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
