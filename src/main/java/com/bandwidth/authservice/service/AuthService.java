package com.bandwidth.authservice.service;

import com.bandwidth.authservice.dto.AuthResponseDTO;
import com.bandwidth.authservice.dto.LoginRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthService {
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthService(JwtService jwtService, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager){
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }
    public ResponseEntity<AuthResponseDTO> login(LoginRequestDTO loginRequest) {
        // 1. Authenticate the user credentials
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            // 2. Set the authentication in the Security Context
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (AuthenticationException e) {
            // This catches exceptions like BadCredentialsException and UsernameNotFoundException
            AuthResponseDTO errorResponse = new AuthResponseDTO(
                    "failure",
                    "Authentication Failed: " + e.getMessage()
            );
            log.error("An unexpected error occurred during authentication process for {}: {}",
                    loginRequest.getUsername(), e.getMessage(), e); // Log the full stack trace
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);

        } catch (Exception e) {
            // This catches any other unexpected exceptions (e.g., Feign connection timeout)
            log.error("An unexpected error occurred during authentication process for {}: {}",
                    loginRequest.getUsername(), e.getMessage(), e); // Log the full stack trace

            AuthResponseDTO internalErrorResponse = new AuthResponseDTO(
                    "error",
                    "An internal server error occurred."
            );
            return new ResponseEntity<>(internalErrorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // 3. Generate the JWT
        String userId = authentication.getName();

        // -------------------------------------------------------------------------

        // Generate the token using the user's identifier
        String token = jwtService.generateToken(userId);
        // 4. Return the token to the client
        return ResponseEntity.ok(new AuthResponseDTO(token, "Bearer"));
    }
}
