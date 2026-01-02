package com.imperialgrand.backend.reservations.repository;

import com.imperialgrand.backend.Admin.ReservationDetailsAdminDTO;
import com.imperialgrand.backend.Admin.ReservationListItemDTO;
import com.imperialgrand.backend.UserDashboard.PageResponse;
import com.imperialgrand.backend.UserDashboard.ReservationCardDTO;
import com.imperialgrand.backend.UserDashboard.ReservationDetailsDTO;
import com.imperialgrand.backend.authentication.DTO.User;
import com.imperialgrand.backend.reservations.ReservationOccasionItemRepository;
import com.imperialgrand.backend.reservations.Tables.ReservationStatus;
import com.imperialgrand.backend.reservations.repository.entity.ReservationDietaryItem;
import com.imperialgrand.backend.reservations.repository.entity.ReservationOccasionItem;
import com.imperialgrand.backend.reservations.repository.entity.ReservationsV1;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
public class ReservationRepositoryServiceV1 {

    private final ReservationRepositoryV1 reservationRepo;
    private final ReservationOccasionItemRepository occasionRepo;
    private final ReservationDietaryItemRepository dietaryRepo;

    public boolean checkReservationOverlaps(long tableId,
                                            LocalDateTime startTime,
                                            LocalDateTime endTime,
                                            List<ReservationStatus> statuses
                                            ){
        return  reservationRepo.existsOverlap(tableId, startTime, endTime, statuses);
    }

    public ReservationsV1 saveReservation(ReservationsV1 rv){
       return reservationRepo.save(rv);
    }

    public PageResponse<ReservationCardDTO> findUpcomingReservations(User user, Pageable pageable){
        Page<ReservationsV1> list = reservationRepo.findByUserAndStatusNotAndStartTimeAfter(
                user,
                ReservationStatus.CANCELLED,
                LocalDateTime.now(),
                pageable);

        List<ReservationCardDTO> content =  list.getContent().stream()
                .sorted(Comparator.comparing(ReservationsV1::getStartTime))
                .map(r -> new ReservationCardDTO(
                        r.getReservationId(),
                        r.getStartTime().toLocalDate().toString(),
                        r.getStartTime().toLocalTime().toString(),
                        r.getGuestCount(),
                        r.getTable().getCode(),
                        r.getTable().getZone(),            // adjust to your Tables fields
                        r.getStatus()
                ))
                .toList();

        return new PageResponse<>(
                content,
                list.getNumber(),
                list.getSize(),
                list.getTotalElements(),
                list.getTotalPages(),
                list.isLast()
        );
    }

    public PageResponse<ReservationCardDTO> findPastReservations(User user, Pageable pageable){
        Page<ReservationsV1> list = reservationRepo.findByUserAndStatusNotAndStartTimeBefore(
                user,
                ReservationStatus.CANCELLED,
                LocalDateTime.now(),
                pageable);

        List<ReservationCardDTO> content =  list.getContent().stream()
                .sorted(Comparator.comparing(ReservationsV1::getStartTime))
                .map(r -> new ReservationCardDTO(
                        r.getReservationId(),
                        r.getStartTime().toLocalDate().toString(),
                        r.getStartTime().toLocalTime().toString(),
                        r.getGuestCount(),
                        r.getTable().getCode(),
                        r.getTable().getZone(),            // adjust to your Tables fields
                        r.getStatus()
                ))
                .toList();

        return new PageResponse<>(
                content,
                list.getNumber(),
                list.getSize(),
                list.getTotalElements(),
                list.getTotalPages(),
                list.isLast()
        );
    }

    public void findAndCancelReservationById(Long id){
        ReservationsV1 reservation = reservationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setUpdatedAt(LocalDateTime.now());

        reservationRepo.save(reservation);
    }

    public ReservationDetailsDTO getDetails(Long userId, Integer reservationId) {
        ReservationsV1 r = reservationRepo.findBaseWithTable(reservationId, userId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        // fetch lists as plain strings
        List<String> diets = dietaryRepo.findDietaryStrings(reservationId);
        List<String> occs  = occasionRepo.findOccasionStrings(reservationId);

        var start = r.getStartTime();
        String date = start.toLocalDate().toString();       // format if you like
        String time = start.toLocalTime().toString();

        return new ReservationDetailsDTO(
                r.getReservationId(),
                date,
                time,
                r.getGuestCount(),
                r.getTable().getCode(),
                r.getTable().getZone(),
                occs,
                diets,
                r.getNote()
        );
    }


    public Page<ReservationListItemDTO> findUpcoming(Pageable pageable) {
        return map(reservationRepo.findUpcoming(LocalDateTime.now(), pageable));
    }

    public Page<ReservationListItemDTO> findPast(Pageable pageable) {
        return map(reservationRepo.findPast(LocalDateTime.now(), pageable));
    }

    public Page<ReservationListItemDTO> findByStatus(ReservationStatus status, Pageable pageable) {
        return map(reservationRepo.findByStatus(status, pageable));
    }

    // ---- mapper lives here ----
    private Page<ReservationListItemDTO> map(Page<ReservationsV1> page) {
        return page.map(r -> new ReservationListItemDTO(
                r.getReservationId(),
                // name fallback: user → guestInfo → "Guest"
                (r.getUser() != null ? r.getUser().getName()
                        : (r.getCustomerName() != null ? r.getCustomerName() : "Guest")),
                r.getGuestCount(),
                r.getStartTime().toLocalDate().toString(),
                r.getStartTime().toLocalTime().toString(),
                r.getTable().getCode(),
                r.getTable().getZone(),
                r.getStatus().name()
        ));
    }


    public ReservationDetailsAdminDTO getDetailsAdminReservation(Integer id) {
        // 1️⃣ Fetch the main reservation (without fetching the lists)
        ReservationsV1 r = reservationRepo.findById(Long.valueOf(id))
                .orElseThrow(() -> new NoSuchElementException("Reservation not found"));

        // 2️⃣ Fetch the lists separately — no multiple bag fetch anymore
        List<String> diets = dietaryRepo.findDietaryStrings(id);
        List<String> occs  = occasionRepo.findOccasionStrings(id);

        // 3️⃣ Assemble the DTO (same as before, no change)
        String date = r.getStartTime().toLocalDate().toString();
        String time = r.getStartTime().toLocalTime().toString();

        return new ReservationDetailsAdminDTO(
                r.getReservationId(),
                r.getStatus().name(),
                date,
                time,
                r.getGuestCount(),
                r.getTable() != null ? r.getTable().getCode() : null,
                r.getTable() != null ? r.getTable().getZone().name() : null,
                r.getCustomerName(),
                r.getCustomerEmail(),
                r.getPhoneNumber(),
                r.getNote(),
                diets,
                occs
        );
    }


    public ReservationDetailsAdminDTO getDetails(Long id) {
        // 1️⃣ Fetch the main reservation (without fetching the lists)
        ReservationsV1 r = reservationRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Reservation not found"));

        // 2️⃣ Fetch the lists separately — no multiple bag fetch anymore
        List<String> diets = dietaryRepo.findDietaryStrings(Math.toIntExact(id));
        List<String> occs  = occasionRepo.findOccasionStrings(Math.toIntExact(id));

        // 3️⃣ Assemble the DTO (same as before, no change)
        String date = r.getStartTime().toLocalDate().toString();
        String time = r.getStartTime().toLocalTime().toString();

        return new ReservationDetailsAdminDTO(
                r.getReservationId(),
                r.getStatus().name(),
                date,
                time,
                r.getGuestCount(),
                r.getTable() != null ? r.getTable().getCode() : null,
                r.getTable() != null ? r.getTable().getZone().name() : null,
                r.getCustomerName(),
                r.getCustomerEmail(),
                r.getPhoneNumber(),
                r.getNote(),
                diets,
                occs
        );
    }



}



