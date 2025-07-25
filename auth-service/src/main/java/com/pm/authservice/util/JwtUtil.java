package com.pm.authservice.util;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.SignatureException;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    private final Key secretKey;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(secret.getBytes(
                    StandardCharsets.UTF_8
            ));
            this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        } catch (WeakKeyException e) {
            throw new IllegalStateException("Failed to initialize JwtUtil. Check if 'jwt.secret' is base64 and valid length.", e);
        }
    }

    public String generateToken(String email, String role) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10hours
                .signWith(secretKey)
                .compact();
    }

    public void validateToken(String token){
        try {
            Jwts.parser().verifyWith((SecretKey) secretKey)
                    .build()
                    .parseSignedClaims(token);
        } catch (MalformedJwtException e) {
            throw new JwtException("Malformed token", e);
        } catch (SecurityException e) {
            throw new JwtException("Invalid signature", e);
        } catch (JwtException e) {
            throw new JwtException("Invalid JWT", e);
        }
    }
}
