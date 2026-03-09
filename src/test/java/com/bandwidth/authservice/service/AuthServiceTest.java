package com.bandwidth.authservice.service;

import com.bandwidth.authservice.dto.AuthResponseDTO;
import com.bandwidth.authservice.dto.LoginRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private LoginRequestDTO loginRequest;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        loginRequest = new LoginRequestDTO("user123", "password");
    }

    @Test
    @DisplayName("Should return 200 and JWT when credentials are valid")
    void login_Success() {
        // 1. Mock Authentication object
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user123");

        // 2. Mock AuthenticationManager to return that auth object
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);

        // 3. Mock JwtService to return a token
        when(jwtService.generateToken("user123")).thenReturn("mock-jwt-token");

        // Act
        ResponseEntity<AuthResponseDTO> response = authService.login(loginRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("mock-jwt-token", response.getBody().getJwtToken());
        assertEquals("Bearer", response.getBody().getTokenType());
        Authentication contextAuth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(contextAuth);
        assertEquals("user123", contextAuth.getName());

        verify(authenticationManager).authenticate(any());
    }

    @Test
    @DisplayName("Should return 401 when BadCredentialsException is thrown")
    void login_BadCredentials() {
        // Mock AuthenticationManager to throw an exception
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid username or password"));

        // Act
        ResponseEntity<AuthResponseDTO> response = authService.login(loginRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().getJwtToken().contains("failure"));
        assertNull(SecurityContextHolder.getContext().getAuthentication(),
                "Security Context should be null after a failed login attempt");
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    @DisplayName("Should return 500 when an unexpected error occurs")
    void login_InternalError() {

        when(authenticationManager.authenticate(any()))
                .thenThrow(new RuntimeException("DB Connection dropped"));

        ResponseEntity<AuthResponseDTO> response = authService.login(loginRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("error", response.getBody().getJwtToken()); // Based on your code logic
    }
}