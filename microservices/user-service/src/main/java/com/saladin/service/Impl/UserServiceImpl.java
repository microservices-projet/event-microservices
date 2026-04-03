package com.saladin.service.Impl;

import com.saladin.dto.LoginRequest;
import com.saladin.dto.UserRequest;
import com.saladin.dto.UserResponse;
import com.saladin.entity.Role;
import com.saladin.entity.Status;
import com.saladin.entity.User;
import com.saladin.kafka.UserEventProducer;
import com.saladin.repository.UserRepository;
import com.saladin.security.LocalJwtService;
import com.saladin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserEventProducer userEventProducer;
    private final LocalJwtService localJwtService;


    @Override
    public UserResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        return mapToResponse(user, localJwtService.createToken(user));
    }

    @Override
    public UserResponse createUser(UserRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);
        userEventProducer.sendUserCreated(savedUser);

        return mapToResponse(savedUser, localJwtService.createToken(savedUser));
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        return mapToResponse(user);
    }

    @Override
    public UserResponse getByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Compte introuvable. Creez un compte local avec le meme nom d'utilisateur que Keycloak pour reserver."));
        return mapToResponse(user);
    }

    @Override
    public UserResponse getByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return mapToResponse(user);
    }

    @Override
    public List<UserResponse> getUsersByIds(List<Long> ids) {
        return userRepository.findAllById(ids)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    @Override
    public boolean validateUser(Long id, String token) {
        return userRepository.findById(id)
                .map(user -> user.getStatus() == Status.ACTIVE)
                .orElse(false);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        userRepository.deleteById(id);
        userEventProducer.sendUserDeleted(user.getId());
    }

    @Override
    public void promoteToAdminIfExists(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            if (user.getRole() == Role.ADMIN) {
                return;
            }
            user.setRole(Role.ADMIN);
            User saved = userRepository.save(user);
            userEventProducer.sendUserUpdated(saved);
        });
    }

    @Override
    public UserResponse updateRole(Long id, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setRole(role);
        User saved = userRepository.save(user);
        userEventProducer.sendUserUpdated(saved);
        return mapToResponse(saved);
    }

    @Override
    public UserResponse updateStatus(Long id, Status status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setStatus(status);
        User saved = userRepository.save(user);
        userEventProducer.sendUserUpdated(saved);
        return mapToResponse(saved);
    }

    private UserResponse mapToResponse(User user) {
        return mapToResponse(user, null);
    }

    private UserResponse mapToResponse(User user, String accessToken) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .accessToken(accessToken)
                .build();
    }
}
