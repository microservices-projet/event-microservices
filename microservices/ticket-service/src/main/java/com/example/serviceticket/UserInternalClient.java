package com.example.serviceticket;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "user-service",
        contextId = "userServiceInternal",
        configuration = UserInternalFeignConfig.class
)
public interface UserInternalClient {

    /** Aligned with user-service {@code GET /api/users/{id}} (JWT relayed from incoming request). */
    @GetMapping("/api/users/{id}")
    UserDTO getUserById(@PathVariable("id") Long id);
}
