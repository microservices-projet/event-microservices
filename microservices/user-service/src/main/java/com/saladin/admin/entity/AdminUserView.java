package com.saladin.admin.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_user_view")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserView {
    @Id
    private Long id;
    private String username;
    private String email;
    private String role;
    private String status;
    private LocalDateTime createdAt;
}
