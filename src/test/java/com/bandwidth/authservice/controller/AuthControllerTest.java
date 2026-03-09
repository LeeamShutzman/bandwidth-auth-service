package com.bandwidth.authservice.controller;

import com.bandwidth.authservice.config.FeignClientConfig;
import com.bandwidth.authservice.config.SecurityConfig;
import com.bandwidth.authservice.dto.AuthResponseDTO;
import com.bandwidth.authservice.dto.LoginRequestDTO;
import com.bandwidth.authservice.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("POST /login - Success")
    void login_Success() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("user123", "password");
        AuthResponseDTO responseBody = new AuthResponseDTO("mock-jwt-token", "Bearer");

        when(authService.login(any(LoginRequestDTO.class)))
                .thenReturn(ResponseEntity.ok(responseBody));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwtToken").value("mock-jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("POST /login - Invalid Credentials (401)")
    void login_Failure() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("wrong", "wrong");

        when(authService.login(any(LoginRequestDTO.class)))
                .thenReturn(ResponseEntity.status(401).build());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /login - Server Error (500)")
    void login_ServerError() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("user123", "password");

        when(authService.login(any(LoginRequestDTO.class)))
                .thenReturn(ResponseEntity.status(500).body(new AuthResponseDTO("error", "Internal Error")));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.jwtToken").value("error"));
    }

    @Test
    @DisplayName("POST /login - Bad Request (Missing fields)")
    void login_BadRequest() throws Exception {
        // Empty JSON or missing password
        String invalidJson = "{\"username\": \"\"}";

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

}