package com.placement.placement_backend;

import com.placement.placement_backend.security.JwtService;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-unit-test-secret-1234";

    @Test
    void tokenRoundTripReturnsSameEmail() {
        JwtService service = new JwtService(SECRET, 60_000);
        String token = service.generateToken("student@example.com");
        assertEquals("student@example.com", service.extractEmail(token));
    }

    @Test
    void tamperedTokenIsRejected() {
        JwtService service = new JwtService(SECRET, 60_000);
        String token = service.generateToken("student@example.com");
        assertThrows(JwtException.class, () -> service.extractEmail(token + "x"));
    }

    @Test
    void expiredTokenIsRejected() {
        JwtService service = new JwtService(SECRET, -1_000);
        String token = service.generateToken("student@example.com");
        assertThrows(JwtException.class, () -> service.extractEmail(token));
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        String token = new JwtService("another-secret-another-secret-12345678", 60_000)
                .generateToken("student@example.com");
        JwtService service = new JwtService(SECRET, 60_000);
        assertThrows(JwtException.class, () -> service.extractEmail(token));
    }

    @Test
    void shortSecretIsRejectedAtStartup() {
        assertThrows(IllegalStateException.class, () -> new JwtService("too-short", 60_000));
    }
}
