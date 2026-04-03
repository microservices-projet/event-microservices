package com.saladin.service;

import com.saladin.dto.LoginRequest;
import com.saladin.entity.Role;
import com.saladin.entity.Status;
import com.saladin.dto.UserRequest;
import com.saladin.dto.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse createUser(UserRequest request);

    UserResponse login(LoginRequest request);

    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long id);

    UserResponse getByUsername(String username);

    UserResponse getByEmail(String email);

    List<UserResponse> getUsersByIds(List<Long> ids);

    boolean existsById(Long id);

    boolean validateUser(Long id, String token);

    UserResponse updateRole(Long id, Role role);

    UserResponse updateStatus(Long id, Status status);

    void deleteUser(Long id);

    /** If a user with this username exists, sets role to ADMIN. No-op if missing or already ADMIN. */
    void promoteToAdminIfExists(String username);
}
