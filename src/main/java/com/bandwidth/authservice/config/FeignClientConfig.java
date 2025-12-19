package com.bandwidth.authservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfig {

    @Value("${user.service.internal-secret}")
    private String internalSecret;

    // Constant for the shared header name
    public static final String INTERNAL_SECRET_HEADER = "X-Internal-Secret";

    @Bean
    public RequestInterceptor internalSecretRequestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // Add the internal API key header for service-to-service authentication
                template.header(INTERNAL_SECRET_HEADER, internalSecret);
            }
        };
    }
}