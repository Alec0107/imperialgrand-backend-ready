package com.imperialgrand.backend.authentication.Repository;

import com.imperialgrand.backend.authentication.DTO.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);

}
