package com.imperialgrand.backend.UserDashboard;


import com.imperialgrand.backend.authentication.DTO.User;
import com.imperialgrand.backend.reservations.repository.ReservationRepositoryServiceV1;
import com.imperialgrand.backend.reservations.repository.entity.ReservationsV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDashboardService {
    private Logger logger = Logger.getLogger(UserDashboardService.class.getName());

    private final ReservationRepositoryServiceV1 repositoryServiceV1;

    public PageResponse<ReservationCardDTO> fetchUpcomingUserReservations(User user, int page, int size){
        Pageable pageable =  PageRequest.of(page, size, Sort.by("startTime").ascending());

        logger.info("Entering User Dashboard Service");
        PageResponse<ReservationCardDTO> upcomingLists = repositoryServiceV1.findUpcomingReservations(user, pageable);

        upcomingLists.data().stream().forEach(item -> {
            logger.info("Reservation Date: " + item.date());
        });

       return upcomingLists;
    }

    public PageResponse<ReservationCardDTO> fetchPastUserReservations(User user, int page, int size){
        Pageable pageable =  PageRequest.of(page, size, Sort.by("startTime").descending());

        logger.info("Entering User Dashboard Service");
        PageResponse<ReservationCardDTO> upcomingLists = repositoryServiceV1.findPastReservations(user, pageable);

        upcomingLists.data().stream().forEach(item -> {
            logger.info("Reservation Date: " + item.date());
        });

        return upcomingLists;
    }


    public void cancelReservation(Long id){
        repositoryServiceV1.findAndCancelReservationById(id);
    }


    public ReservationDetailsDTO fetchViewDetails(Long userId, Integer reservationId){
        return repositoryServiceV1.getDetails(userId, reservationId);
    }

}
