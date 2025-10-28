package com.imperialgrand.backend.resetpassword.model;

import com.imperialgrand.backend.authentication.DTO.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reset_password_table")
public class ResetPasswordToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer resetTokenId;

    private String token;
    private String plainToken;
    private String salt;

    private LocalDateTime expiryTime ;
    private LocalDateTime createdAt;
    private boolean used;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id") // <-- use "id"
    private User user;
}
