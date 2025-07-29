package com.learn.splitwise.security;

import com.learn.splitwise.exception.JwtExceptionHandler;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {
    private Key secretKey;
    private final long jwtExpiration = 1000 * 60 * 1; // 10 hours

    @PostConstruct
    public void init() {
        secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256); // auto generate key
    }

    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(secretKey)
                .compact();
    }

    public String extractEmail(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (ExpiredJwtException ex) {
            throw new JwtExceptionHandler("Token Expired", HttpStatus.UNAUTHORIZED);
        } catch (UnsupportedJwtException | MalformedJwtException  ex) {
            throw new JwtExceptionHandler("Invalid Jwt Token", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            throw new JwtExceptionHandler("Invalid token", HttpStatus.UNAUTHORIZED);
        }
    }

    public boolean isTokenValid(String token, String email) {
        String extractedEmail = extractEmail(token);
        return (email.equals(extractedEmail)) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration()
                    .before(new Date());
        } catch (JwtExceptionHandler e) {
            throw new JwtExceptionHandler("Unable to validate token expiration", HttpStatus.UNAUTHORIZED);
        }
    }


}
