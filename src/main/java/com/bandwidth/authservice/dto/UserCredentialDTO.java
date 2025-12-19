package com.bandwidth.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
@Builder
public class UserCredentialDTO {
    private Long id;
    private String username;
    private String hashedPassword;
    private List<String> roles; // e.g., ["USER", "ADMIN"]
}