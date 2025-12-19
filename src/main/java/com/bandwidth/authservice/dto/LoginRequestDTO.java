package com.bandwidth.authservice.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoginRequestDTO implements Serializable {
    private String username;
    private String password;
}
