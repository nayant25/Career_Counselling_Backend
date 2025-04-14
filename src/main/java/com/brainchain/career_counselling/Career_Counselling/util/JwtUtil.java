package com.brainchain.career_counselling.Career_Counselling.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtUtil.class);
    private final String secret;
    private final long expirationMillis;

    public JwtUtil(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration:36000000}") long expirationMillis) {
        this.secret = secret;
        this.expirationMillis = expirationMillis;
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String email, Long userId, Collection<? extends GrantedAuthority> authorities) {
        LOGGER.debug("Generating token for email: {}, userId: {}", email, userId);
        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .claim("roles", authorities.stream().map(GrantedAuthority::getAuthority).toList())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        return extractClaims(token).get("userId", Long.class);
    }

    public List<String> extractRoles(String token) {
        Object roles = extractClaims(token).get("roles");
        if (roles instanceof List<?>) {
            return ((List<?>) roles).stream()
                    .filter(String.class::isInstance)
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public Collection<? extends GrantedAuthority> getAuthorities(String token) {
        return extractRoles(token).stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = extractClaims(token);
            boolean isValid = !claims.getExpiration().before(new Date());
            if (!isValid) {
                LOGGER.warn("Token expired for email: {}", claims.getSubject());
            }
            return isValid;
        } catch (ExpiredJwtException e) {
            LOGGER.warn("Token expired: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            LOGGER.warn("Invalid token: {}", e.getMessage());
            return false;
        }
    }

    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
