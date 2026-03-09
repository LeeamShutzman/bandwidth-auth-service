package com.bandwidth.authservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    // A valid Base64 encoded 256-bit key for HS256
    private final String testSecret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Manually inject the secret key into the private field since we aren't using @SpringBootTest
        ReflectionTestUtils.setField(jwtService, "SECRET_KEY", testSecret);
    }

    @Test
    @DisplayName("Should generate a valid JWT with subject")
    void generateToken_Simple_Success() {
        String username = "user123";
        String token = jwtService.generateToken(username);

        assertNotNull(token);

        // Use JJWT to parse it back and verify
        Claims claims = parseToken(token);
        assertEquals(username, claims.getSubject());
        assertTrue(claims.getExpiration().after(new Date()));
    }

    @Test
    @DisplayName("Should include extra claims in the JWT")
    void generateToken_WithClaims_Success() {
        String username = "adminUser";
        Map<String, Object> extraClaims = Map.of("role", "ADMIN", "id", 500);

        String token = jwtService.generateToken(username, extraClaims);

        Claims claims = parseToken(token);
        assertEquals(username, claims.getSubject());
        assertEquals("ADMIN", claims.get("role"));
        assertEquals(500, claims.get("id"));
    }

    @Test
    @DisplayName("Should have an expiration date approximately 24 hours in the future")
    void generateToken_ExpirationCheck() {
        String token = jwtService.generateToken("tester");
        Claims claims = parseToken(token);

        long diffInMs = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
        long twentyFourHoursInMs = 24 * 60 * 60 * 1000L;

        // Verify the expiration is exactly 24 hours (with a small margin for execution time)
        assertTrue(Math.abs(diffInMs - twentyFourHoursInMs) < 1000);
    }

    /**
     * Helper method to decode the JWT so we can verify the contents.
     */
    private Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(testSecret));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}