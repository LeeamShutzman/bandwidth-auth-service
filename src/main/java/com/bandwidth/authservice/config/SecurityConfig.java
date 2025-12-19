package com.bandwidth.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
            "http://localhost:8081",
            "http://10.0.0.5:8081",
            "http://10.0.0.8:8081"
    );

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable) // Ensure HTTP Basic is off
                .formLogin(AbstractHttpConfigurer::disable) // Ensure Form Login is off
                .authorizeHttpRequests(auth -> auth
                        // Allow unauthenticated access to the login endpoint
                        .requestMatchers("/api/v1/auth/login").permitAll()
                        // All other requests require authentication (though this service only has login)
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Use stateless session (JWTs)
                );

        return http.build();
    }

    /**
     * Exposes the AuthenticationManager bean, which we will use in the controller
     * to perform the actual authentication (username/password verification).
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


    /**
     * Defines the PasswordEncoder used throughout the application.
     * BCrypt is the industry standard for hashing passwords.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // IMPORTANT: Use the static list defined above for allowed frontend origins
        configuration.setAllowedOrigins(ALLOWED_ORIGINS);

        // Allows all HTTP methods (POST, GET, PUT, DELETE, OPTIONS)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Allows all headers, necessary for Content-Type, Authorization, etc.
        configuration.setAllowedHeaders(List.of("*"));

        // Allows credentials (like cookies or HTTP authentication, although less common with JWTs)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply this configuration to all endpoints
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}