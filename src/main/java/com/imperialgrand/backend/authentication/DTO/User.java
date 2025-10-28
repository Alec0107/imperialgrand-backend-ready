package com.imperialgrand.backend.authentication.Repository;

import com.imperialgrand.backend.user.model.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Builder
@Entity(name="user_account")
@Table(name="user_account")
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Basic Info
    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String phone;

    // Verification & Status
    private boolean emailVerified;

    @Enumerated(EnumType.STRING)
    private Status status;

    private Instant createdAt;
    private Instant updatedAt;

    @Enumerated(EnumType.STRING)
    private Role role; // Default role for normal users


}
