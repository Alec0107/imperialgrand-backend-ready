package com.imperialgrand.backend.authentication.Repository;

import com.imperialgrand.backend.auth.AuthController;
import com.imperialgrand.backend.authentication.DTO.AccountDetailsDTO;
import com.imperialgrand.backend.authentication.DTO.User;
import com.imperialgrand.backend.authentication.DTO.records.CustomerDTO;
import com.imperialgrand.backend.authentication.Exception.OtpVerificationException;
import com.imperialgrand.backend.authentication.Exception.UserAlreadyExistsException;
import com.imperialgrand.backend.authentication.Exception.UserPersistenceException;
import com.imperialgrand.backend.user.exception.EmailNotFoundException;
import com.imperialgrand.backend.user.model.Role;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
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


    public User findUserByEmail(String email){
        return userRepo.findByEmail(email).orElseThrow(() -> new EmailNotFoundException("Email not found"));
    }


    public AccountDetailsDTO getCurrent(User me){
        return new AccountDetailsDTO(
                me.getName(),
                me.getEmail(),
                me.getPhone(),
                me.getBirthday() != null ? me.getBirthday().toString() : null
        );
    }


    @Transactional
    public void setBirthday(User me, String birthdayStr) {
        // Accept null to clear, or "YYYY-MM-DD"
        if (birthdayStr == null || birthdayStr.isBlank()) {
            me.setBirthday(null);
            userRepo.save(me);
            return;
        }

        LocalDate parsed;
        try {
            parsed = LocalDate.parse(birthdayStr); // ISO yyyy-MM-dd
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format. Use YYYY-MM-DD.");
        }

        // simple guard: birthday cannot be in the future
        if (parsed.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Birthday cannot be in the future.");
        }

        me.setBirthday(parsed);
        userRepo.save(me);
    }

    public List<CustomerDTO> getAllCustomers(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return userRepo.findByRole(Role.USER, pageable)
                .getContent()
                .stream()
                .map(CustomerDTO::fromEntity)
                .toList();
    }

}
