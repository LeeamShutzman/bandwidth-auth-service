package com.bandwidth.authservice.integration;

import com.bandwidth.authservice.client.UserServiceFeignClient;
import com.bandwidth.authservice.dto.AuthResponseDTO;
import com.bandwidth.authservice.dto.LoginRequestDTO;
import com.bandwidth.authservice.dto.UserCredentialDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder; // Inject the real encoder used by your app

    // This stops Spring from trying to find a real User Service on the network
    @MockBean
    private UserServiceFeignClient userServiceFeignClient;

    @Test
    void healthCheck() {
        // Just to see if the context even starts
        assertTrue(true);
    }

    @Test
    void loginEndpoint_ReturnsUnauthorized_WithRandomCreds() {
        LoginRequestDTO request = new LoginRequestDTO("fakeUser", "fakePass");

        ResponseEntity<AuthResponseDTO> response = restTemplate.postForEntity(
                "/api/v1/auth/login",
                request,
                AuthResponseDTO.class
        );

        // If the context is FIXED, this will return 401, not a crash!
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }



    @Test
    void loginEndpoint_ReturnsSuccess_WithValidMockedUser() {
        // 1. Arrange
        String rawPassword = "password";
        // Generate a REAL BCrypt hash so the security provider is happy
        String validBcryptHash = passwordEncoder.encode(rawPassword);

        UserCredentialDTO mockUser = new UserCredentialDTO(1L, "user", validBcryptHash, List.of("ROLE_USER"));
        ResponseEntity<UserCredentialDTO> userResponse = ResponseEntity.ok(mockUser);

        when(userServiceFeignClient.getCredentialsByUsername("user")).thenReturn(userResponse);

        LoginRequestDTO request = new LoginRequestDTO("user", rawPassword);

        // 2. Act
        ResponseEntity<AuthResponseDTO> response = restTemplate.postForEntity(
                "/api/v1/auth/login",
                request,
                AuthResponseDTO.class
        );

        // 3. Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().getJwtToken());
    }

    @Test
    void loginEndpoint_ReturnsUnauthorized_WhenUserNotFound() {
        // Simulate User Service returning empty/404
        when(userServiceFeignClient.getCredentialsByUsername("nonexistent"))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).build());

        LoginRequestDTO request = new LoginRequestDTO("nonexistent", "password");

        ResponseEntity<AuthResponseDTO> response = restTemplate.postForEntity(
                "/api/v1/auth/login",
                request,
                AuthResponseDTO.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void loginEndpoint_ReturnsBadRequest_WhenInputIsInvalid() {
        // Sending empty username to trigger @NotBlank validation
        LoginRequestDTO request = new LoginRequestDTO("", "");

        ResponseEntity<AuthResponseDTO> response = restTemplate.postForEntity(
                "/api/v1/auth/login",
                request,
                AuthResponseDTO.class
        );

        // This checks if your Controller-level validation is working
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void loginEndpoint_ReturnsInternalServerError_WhenUserServiceIsDown() {
        // Simulate the Feign client exploding (e.g., Connection Refused or 500)
        when(userServiceFeignClient.getCredentialsByUsername("user"))
                .thenThrow(new InternalError());

        LoginRequestDTO request = new LoginRequestDTO("user", "password");

        ResponseEntity<AuthResponseDTO> response = restTemplate.postForEntity(
                "/api/v1/auth/login",
                request,
                AuthResponseDTO.class
        );

        // This confirms your service handles unexpected upstream crashes gracefully
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}