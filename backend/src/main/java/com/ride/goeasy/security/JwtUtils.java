package com.ride.goeasy.security;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtils {

    @Value("${jwt.secret:MySuperSecretKeyForJwtTokenThatIsAtLeast32CharsLong}")
    private String secretKeyString;

    @Value("${jwt.expiration-ms:3600000}") // 1 hour
    private long expirationMs;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        // Create a proper HMAC secret key
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

 // 1. Token generate 
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey) // Latest version automatically handles algorithm
                .compact();
    }

    // Extract username from token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extract role from token
    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        Object role = claims.get("role");
        return role == null ? null : role.toString();
    }

    // Extract expiration date
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Generic method to extract any claim
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

 // 2. Claims extract
    private Claims extractAllClaims(String token) {
        return Jwts.parser() 
                .verifyWith(secretKey) 
                .build()
                .parseSignedClaims(token) 
                .getPayload(); 
    }

    // Check if token expired
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Validate token
    public boolean isTokenValid(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return extractedUsername != null && extractedUsername.equals(username) && !isTokenExpired(token);
    }
}
