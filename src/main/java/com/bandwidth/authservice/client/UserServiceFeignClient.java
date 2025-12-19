package com.bandwidth.authservice.client;

import com.bandwidth.authservice.config.FeignClientConfig;
import com.bandwidth.authservice.dto.UserCredentialDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service", url = "${user.service.url:http://localhost:8083}", configuration = FeignClientConfig.class)
public interface UserServiceFeignClient {
    // Maps to: GET http://user-service/api/v1/users/internal/credentials?username={username}

    @GetMapping("/api/v1/users/internal/credentials")
    ResponseEntity<UserCredentialDTO> getCredentialsByUsername(@RequestParam("username") String username);
}
