package com.saladin.dto;

import com.saladin.entity.Role;
import com.saladin.entity.Status;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private Role role;
    private Status status;
    private LocalDateTime createdAt;
    /** Present only on POST /api/users and POST /api/users/login (local auth). Use as Bearer token on the gateway. */
    private String accessToken;
}
