package com.bandwidth.authservice.service;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
@Service
public class JwtService {
    @Value("${jwt.secret.key}")
    private String SECRET_KEY;

    private static final long EXPIRATION_HOURS = 24;

    public String generateToken(String subject, Map<String, Object> claims) {
        Instant now = Instant.now();
        Instant expiryDate = now.plus(EXPIRATION_HOURS, ChronoUnit.HOURS);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryDate))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateToken(String subject) {
        return generateToken(subject, new HashMap<>());
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        // This generates a Key suitable for HMAC-SHA algorithms (e.g., HS256)
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
