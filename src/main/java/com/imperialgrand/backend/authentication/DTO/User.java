package com.imperialgrand.backend.authentication.DTO;

import com.imperialgrand.backend.authentication.Repository.Status;
import com.imperialgrand.backend.user.model.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Data
@Builder
@Entity(name="user_account")
@Table(name="user_account")
@AllArgsConstructor
@NoArgsConstructor
public class User implements UserDetails {
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


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }
}
