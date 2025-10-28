package com.imperialgrand.backend.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<com.imperialgrand.backend.authentication.DTO.User, Integer> {

}
