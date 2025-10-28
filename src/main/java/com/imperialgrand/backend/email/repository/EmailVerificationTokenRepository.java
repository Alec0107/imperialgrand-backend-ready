package com.imperialgrand.backend.email.repository;

import com.imperialgrand.backend.email.model.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Integer> {
    Optional<EmailVerificationToken> findByToken(String token);

    // ✅ FIX HERE
    Optional<EmailVerificationToken> findByUser_Id(Long userId);

    // ✅ FIX HERE TOO
    void deleteByUser_Id(Long userId);
}
