package com.bandwidth.authservice.service;

import com.bandwidth.authservice.client.UserServiceFeignClient;
import com.bandwidth.authservice.dto.UserCredentialDTO;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.core.env.Environment;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserServiceFeignClient userServiceFeignClient;

    public UserDetailsServiceImpl(UserServiceFeignClient userServiceFeignClient) {
        this.userServiceFeignClient = userServiceFeignClient;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserCredentialDTO credentials;
        try {
            credentials = userServiceFeignClient.getCredentialsByUsername(username).getBody();
        } catch (FeignException e) {
            log.error("Feign error body: {}", e);
            throw e;
        }

        if (credentials == null) {
            log.warn("User not found for username: {}", username);
            throw new UsernameNotFoundException("User not found: " + username);
        }
        // Map String roles to GrantedAuthority objects
        List<GrantedAuthority> authorities = credentials.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // Return a Spring Security UserDetails object
        // The HASHED password is used here for verification by the AuthenticationManager
        return new org.springframework.security.core.userdetails.User(
                // Use the user ID as the principal (subject) for the JWT
                String.valueOf(credentials.getId()),
                credentials.getHashedPassword(), // This is the HASHED password!
                authorities
        );
    }
}