package com.company.payroll.security;

import java.security.Key;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private final Key key;
    private final long expirationMs;
    private final long rememberMeExpirationMs;

    public JwtUtil(@Value("${jwt.secret:ReplaceMeChangeInProdVeryLongRandomString}") String secret,
            @Value("${jwt.expiration-ms:900000}") long expirationMs,
            @Value("${jwt.remember-me-expiration-ms:604800000}") long rememberMeExpirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = expirationMs;
        this.rememberMeExpirationMs = rememberMeExpirationMs;
    }

    public String generateToken(String username, List<String> roles) {
        return generateToken(username, roles, false);
    }

    public String generateToken(String username, List<String> roles, boolean rememberMe) {
        Date now = new Date();
        long expiration = rememberMe ? rememberMeExpirationMs : expirationMs;
        Date exp = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    // Expose expiration for cookie/max-age purposes
    public long getExpirationMs() {
        return expirationMs;
    }

    public long getExpirationMs(boolean rememberMe) {
        return rememberMe ? rememberMeExpirationMs : expirationMs;
    }
}
