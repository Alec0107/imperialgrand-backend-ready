package com.imperialgrand.backend.authentication.Repository;

import com.imperialgrand.backend.auth.AuthController;
import com.imperialgrand.backend.authentication.DTO.User;
import com.imperialgrand.backend.authentication.Exception.OtpVerificationException;
import com.imperialgrand.backend.authentication.Exception.UserAlreadyExistsException;
import com.imperialgrand.backend.authentication.Exception.UserPersistenceException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.logging.Logger;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAccountRepositoryService{

    private final UserAccountRepository userRepo;
    private final Logger logger = Logger.getLogger(UserAccountRepositoryService.class.getName());

    @Transactional
    public User saveUserAccount(User user){
        try {
            return userRepo.save(user);
        } catch (DataIntegrityViolationException ex) {
            // This specifically catches duplicate email violations
            log.error("Duplicate email detected while saving user: {}", user.getEmail(), ex);
            throw new UserAlreadyExistsException("A user with this email already exists.");
        } catch (DataAccessException ex) {
            // Other generic DB errors
            log.error("Database error while saving user: {}", user.getEmail(), ex);
            throw new UserPersistenceException("Unable to save user account right now.");
        }
    }

    @Transactional
    public void markUserAsVerified(String email){
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new OtpVerificationException(
                        HttpStatus.NOT_FOUND,
                        String.format("User not found for email %s", email)));

        user.setEmailVerified(true);
        user.setStatus(Status.VERIFIED);
        user.setUpdatedAt(Instant.now());

        userRepo.save(user);
    }


    public User readUserByEmail(String email){
        return userRepo.findByEmail(email).orElse(null);
    }

}
