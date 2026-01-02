package com.imperialgrand.backend.UserDashboard;

import com.imperialgrand.backend.authentication.DTO.User;
import com.imperialgrand.backend.common.response.ApiResponse;
import com.imperialgrand.backend.reservations.repository.entity.ReservationsV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;

@Slf4j
@RestController
@RequestMapping("/api/user/dashboard")
@RequiredArgsConstructor
public class UserDashboardController {

    private final Logger logger = Logger.getLogger(UserDashboardController.log.getName());

    private final UserDashboardService service;


    @GetMapping("/reservation/upcoming")
    public ResponseEntity<ApiResponse<PageResponse<ReservationCardDTO>>> upcomingReservations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size
    ){
        logger.info("Entering User Dashboard Controller");
        User user= null;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())){
            user = (User) auth.getPrincipal();
        }

        PageResponse<ReservationCardDTO> upcoming = service.fetchUpcomingUserReservations(user,page, size);

        return ResponseEntity.ok(new ApiResponse(upcoming, "Upcoming reservations fetched successfully."));
    }

    @GetMapping("/reservation/past")
    public ResponseEntity<ApiResponse<PageResponse<ReservationCardDTO>>> pastReservations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size
    ){
        logger.info("Entering User Dashboard Controller");
        User user= null;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())){
            user = (User) auth.getPrincipal();
        }

        PageResponse<ReservationCardDTO> upcoming = service.fetchPastUserReservations(user,page, size);

        return ResponseEntity.ok(new ApiResponse(upcoming, "Past reservations fetched successfully."));
    }

    @PostMapping("/reservation/cancel")
    public ResponseEntity<ApiResponse> cancelReservation(@RequestParam Long id){
        logger.info("ID: " + id);
        service.cancelReservation(id);
        return ResponseEntity.ok(new ApiResponse(null,"Reservation is successfully deleted."));
    }

    @GetMapping("/reservation/view-details")
    public ResponseEntity<ApiResponse<ReservationDetailsDTO>> getViewDetailsReservation(@RequestParam Integer id){
        logger.info("Entering User Dashboard Controller: View Details Reservation");
        User user= null;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())){
            user = (User) auth.getPrincipal();
        }

        ReservationDetailsDTO detailsDTO = service.fetchViewDetails(user.getId(), id);

        return ResponseEntity.ok(new ApiResponse(detailsDTO, "Fetching view details successfully."));
    }




}
