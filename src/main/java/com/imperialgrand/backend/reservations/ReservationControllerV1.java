package com.imperialgrand.backend.reservations;

import com.imperialgrand.backend.authentication.DTO.User;
import com.imperialgrand.backend.common.response.ApiResponse;
import com.imperialgrand.backend.reservation.dto.SubmissionPayload;
import com.imperialgrand.backend.reservations.DTO.HoldResponse;
import com.imperialgrand.backend.reservations.DTO.SearchRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.logging.Logger;

@Slf4j
@RestController
@RequestMapping("/api/reservation")
@RequiredArgsConstructor
public class ReservationControllerV1 {

    private final ReservationServiceV1 reservationServiceV1;
    private final Logger logger = Logger.getLogger(ReservationControllerV1.class.getName());

    @PostMapping("/availability")
    public ResponseEntity<ApiResponse<HoldResponse>> checkAvailability(@RequestBody SearchRequest sr){
            HoldResponse hd = reservationServiceV1.checkAvailability(sr);
        return ResponseEntity.ok(new ApiResponse(hd, "Table successfully held for 15 minutes"));
    }

    @GetMapping("/status")
    public ResponseEntity<String> checkLockStatus(@RequestParam String tableId,
                                                  @RequestParam LocalDateTime start){
        logger.info(tableId);
        logger.info(String.valueOf(start));
        reservationServiceV1.checkStatus(Long.parseLong(tableId), start);
        return ResponseEntity.ok("Reservation is still valid.");
    }


    @PostMapping("/submit")
    public ResponseEntity<String> submitReservation(@RequestBody SubmissionPayload submissionPayload){
        System.out.println("Accessing submit reservation endpoint...");
        logger.info(submissionPayload.getReservationDetails().getMessage());
        logger.info(submissionPayload.getReservationDetails().toString());
        User user = null;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())){
            user = (User) auth.getPrincipal();
        }
        reservationServiceV1.saveReservation(submissionPayload, user);
        return ResponseEntity.ok("Saved");
    }


    @PostMapping("/guest/submit")
    public ResponseEntity<String> submitReservationAsGuest(@RequestBody SubmissionPayload submissionPayload){
        if(submissionPayload.getGuestInfo() != null){
            System.out.println("Guest Checkout");
            System.out.println("Name: " + submissionPayload.getGuestInfo().getFirstName());
            System.out.println("Email: " + submissionPayload.getGuestInfo().getEmail());
        }
        reservationServiceV1.saveReservation(submissionPayload,null);
        return ResponseEntity.ok("Saved");
    }

}
