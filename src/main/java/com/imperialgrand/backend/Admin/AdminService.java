package com.imperialgrand.backend.Admin;

import com.imperialgrand.backend.authentication.DTO.User;
import com.imperialgrand.backend.authentication.DTO.records.CustomerDTO;
import com.imperialgrand.backend.authentication.Repository.UserAccountRepositoryService;
import com.imperialgrand.backend.email.utils.HashTokenUtils;
import com.imperialgrand.backend.jwt.JwtGeneratorService;
import com.imperialgrand.backend.jwt.JwtRepositoryService;
import com.imperialgrand.backend.jwt.model.JwtToken;
import com.imperialgrand.backend.reservations.Tables.ReservationStatus;
import com.imperialgrand.backend.reservations.repository.ReservationRepositoryServiceV1;
import com.imperialgrand.backend.reservations.repository.entity.ReservationsV1;
import com.imperialgrand.backend.user.exception.EmailNotFoundException;
import com.imperialgrand.backend.user.exception.EmailNotVerifiedException;
import com.imperialgrand.backend.user.model.Role;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import java.util.logging.Logger;

import static com.imperialgrand.backend.authentication.AuthServiceV1.helperResponseMap;

@Service
@AllArgsConstructor
public class AdminService {

    private final AuthenticationManager authenticationManager;
    private final UserAccountRepositoryService userRepoService;
    private final JwtGeneratorService jwtService;
    private final JwtRepositoryService jwtRepoService;
    private final JwtGeneratorService jwtGenService;
    private final ReservationRepositoryServiceV1 reservationRepoService;
    private final Logger logger = Logger.getLogger(AdminService.class.getName());

    /** 1) Admin login */
    public Map<String, Object> login(String email, String password, String incomingDeviceId) {
        logger.info(String.format("LOGIN\nEmail:%s\nPassword:%s\nDeviceId:%s", email, password, incomingDeviceId));
        String did = incomingDeviceId == null ? null : incomingDeviceId.trim();
        boolean missingDeviceId =
                did == null
                        || did.isEmpty()
                        || "null".equalsIgnoreCase(did)
                        || "undefined".equalsIgnoreCase(did);


        Map<String, Object> loginResponse = new HashMap<>();

        String accessToken = null;
        String refreshToken = null;
        String deviceId = null;

        User user  = userRepoService.readUserByEmail(email);
        if(user == null){
            throw new EmailNotFoundException("No account associated with this email. Please contact support or sign up.");
        }

        if(!user.isEmailVerified()){
            throw new EmailNotVerifiedException("Email is not verified");
        }

        if(missingDeviceId){
            deviceId = UUID.randomUUID().toString();
            logger.info(String.format("\nNew user is logging in: \nDevice id:%s", deviceId));
        }else{
            logger.info(String.format("\nOld user is logging in: \nDevice id:%s", incomingDeviceId));
            JwtToken previousRT = jwtRepoService.getTokenByUserIdAndDeviceId(user.getId(), incomingDeviceId);
            previousRT.setRevoked(true);
            jwtRepoService.saveOldToken(previousRT);
            deviceId = incomingDeviceId;
        }


        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

        // generate a new refresh and access jwt token
        refreshToken = jwtGenService.generateRefreshToken(user, Duration.ofDays(7));
        accessToken  = jwtGenService.generateAccessToken(user, Duration.ofMinutes(10));

        String salt = HashTokenUtils.generateSalt();
        String hashedRefreshToken = HashTokenUtils.hashRefreshToken(refreshToken, salt);

        jwtRepoService.saveNewToken(hashedRefreshToken, salt, user, deviceId, LocalDateTime.now().plus(Duration.ofDays(7)));

        return helperResponseMap(user, accessToken, refreshToken, deviceId);
    }

    /** 2) Customers (paged) */
    public List<CustomerDTO> fetchCustomers(int page, int size) {
        return userRepoService.getAllCustomers(page, size); // matches what you used earlier
    }


    public Page<ReservationListItemDTO> findByStatus(String status, Pageable pageable) {
        String s = status.toUpperCase();

        switch (s) {
            case "UPCOMING":
                return reservationRepoService.findUpcoming(pageable);

            case "PAST":
                return reservationRepoService.findPast(pageable);

            // you can add more views later:
            // case "CHECKEDIN": ...
            // case "NOSHOW": ...

            default:
                // fall back to real enum values: CONFIRMED / CANCELLED / FAILED
                com.imperialgrand.backend.reservations.Tables.ReservationStatus rs = ReservationStatus.valueOf(s);
                return reservationRepoService.findByStatus(rs, pageable);
        }
    }

    public ReservationDetailsAdminDTO getDetails(Integer id) {
        return reservationRepoService.getDetailsAdminReservation(id);
    }

    public void cancel(Integer id){

    }

}