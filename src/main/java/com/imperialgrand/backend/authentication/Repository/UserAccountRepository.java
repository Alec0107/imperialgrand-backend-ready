package com.imperialgrand.backend.authentication.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepo extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);
}
