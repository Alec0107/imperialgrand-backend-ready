package com.imperialgrand.backend.reservations;

import com.imperialgrand.backend.Admin.ReservationListItemDTO;
import com.imperialgrand.backend.authentication.DTO.User;
import com.imperialgrand.backend.reservation.dto.GuestInfoDTO;
import com.imperialgrand.backend.reservation.dto.ReservationDetailsDTO;
import com.imperialgrand.backend.reservation.dto.SubmissionPayload;
import com.imperialgrand.backend.reservation.exception.NoAvailableTableException;
import com.imperialgrand.backend.reservation.exception.ReservationLockNotFoundException;
import com.imperialgrand.backend.reservations.DTO.HoldResponse;
import com.imperialgrand.backend.reservations.DTO.SearchRequest;
import com.imperialgrand.backend.reservations.Tables.ReservationStatus;
import com.imperialgrand.backend.reservations.Tables.TableRepositoryServiceV1;
import com.imperialgrand.backend.reservations.Tables.Tables;
import com.imperialgrand.backend.reservations.redis.ReservationHold;
import com.imperialgrand.backend.reservations.redis.ReservationHoldService;
import com.imperialgrand.backend.reservations.repository.ReservationRepositoryServiceV1;
import com.imperialgrand.backend.reservations.repository.entity.ReservationDietaryItem;
import com.imperialgrand.backend.reservations.repository.entity.ReservationOccasionItem;
import com.imperialgrand.backend.reservations.repository.entity.ReservationsV1;
import com.imperialgrand.backend.websocket.AdminNotifier;
import com.imperialgrand.backend.websocket.AdminReservationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceV1 {
;
    private final AdminNotifier adminNotifier;
    private final TableRepositoryServiceV1 tableRepoService;
    private final ReservationRepositoryServiceV1 reservationRepoService;
    private final ReservationHoldService reservationHoldService;

    private static final Duration DINING = Duration.ofMinutes(60);
    private static final Duration TURNOVER = Duration.ofMinutes(15);
    private final Logger logger = Logger.getLogger(ReservationServiceV1.class.getName());

    public HoldResponse checkAvailability(SearchRequest sr){
        HoldResponse hd = null;
        LocalDate d = LocalDate.parse(sr.date());
        LocalTime t = LocalTime.parse(sr.time());
        LocalDateTime localStart = LocalDateTime.of(d,t);
        LocalDateTime localEnd = localStart.plus(DINING).plus(TURNOVER);
        TimeWindow window = new TimeWindow(localStart, localEnd);

        logger.info(String.format("Start:%s", String.valueOf(window.start())));
        logger.info(String.format("End:%s", String.valueOf(window.end())));

        // 1. Find candidate tables based on the capacity against the user party size
        List<Tables> candidates = tableRepoService.getCandidateTables(sr.partySize());

        logger.info("\nTables Candidates:");
        candidates.forEach(ts ->
                logger.info(String.format("%nCode: %s%nCapacity: %d%nZone: %s%n",
                        ts.getCode(), ts.getCapacity(), ts.getZone()))
        );
        // 2. Filter based on overlaps start and end time
        List<Tables> dbClean =
                candidates.stream()
                        .filter(tb -> !reservationRepoService.checkReservationOverlaps(
                                tb.getId(),
                                window.start(),
                                window.end(),
                                List.of(ReservationStatus.CONFIRMED, ReservationStatus.HELD)
                        ))
                        .sorted(Comparator.comparing(Tables::getCapacity))
                        .toList();
        logger.info("\nTables Available from db:");
        dbClean.forEach(ts ->
                logger.info(String.format("%nCode: %s%nCapacity: %d%nZone: %s%n",
                        ts.getCode(), ts.getCapacity(), ts.getZone()))
        );

        List<Tables> available = dbClean.stream()
                .filter(tb -> !reservationHoldService.isheld(tb.getId(), window.start()))
                .sorted(Comparator.comparing(Tables::getCapacity))
                .toList();

        logger.info("\nTables Available from redis:");
        available.forEach(ts ->
                logger.info(String.format("%nCode: %s%nCapacity: %d%nZone: %s%n",
                        ts.getCode(), ts.getCapacity(), ts.getZone()))
        );
        // 3. Hold in redis
        if(!available.isEmpty()){
            Tables chosen = available.get(0);

            String key = reservationHoldService.saveHold(new ReservationHold(
                    chosen.getId(),
                    localStart,
                    localEnd,
                    sr.partySize(),
                    null
            ), Duration.ofMinutes(5));

            hd = new HoldResponse(
                    chosen.getId(),
                    Instant.now().plus(Duration.ofMinutes(5)).toEpochMilli(),
                    window.start().toString(),
                    window.end().toString(),
                    key,
                    chosen.getCode(),
                    sr.partySize()
            );
        }else{
          throw new NoAvailableTableException("No available tables");
        }
        return hd;
    }

    public void checkStatus(long tableId, LocalDateTime start){
        boolean status = reservationHoldService.isheld(tableId, start);
        if(!status){
            throw new ReservationLockNotFoundException("Reservation lock invalid. Try to request a new one");
        }
    }

    public void saveReservation(SubmissionPayload submissionPayload, User user){
        ReservationDetailsDTO rd = submissionPayload.getReservationDetails();
        GuestInfoDTO gi = submissionPayload.getGuestInfo();
        long tableId = rd.getTableId();
        String dt = rd.getDate() + "T" + rd.getTime();

        // 1. If not found in redis (expired)
        if(!reservationHoldService.isheld(tableId, dt)){
            throw new ReservationLockNotFoundException("Reservation hold is expired. Try to request a new one");
        }

        Optional<Tables> table = tableRepoService.findTableById(rd.getTableId());
        LocalDateTime startTime = LocalDateTime.of(rd.getDate(), rd.getTime());
        LocalDateTime endTime   = startTime.plus(DINING).plus(TURNOVER);

        ReservationsV1 reservationDb = ReservationsV1.builder()
                .table(table.get())
                .startTime(startTime)
                .endTime(endTime)
                .guestCount(rd.getGuestCount())
                .status(ReservationStatus.CONFIRMED)
                .note(rd.getMessage())
                .createdAt(LocalDateTime.now())
                .build();

        // Reservation is from a guest user
        if(user == null){

            String[] n = (gi.getFirstName() + " " +  gi.getLastName()).split(" ");
            String name = n[0] + " " + n[1];

            reservationDb.setUpdatedAt(null);
            reservationDb.setUpdatedAt(null);
            reservationDb.setHoldExpiresAt(null);
            reservationDb.setUser(null);
            reservationDb.setCustomerName(name);
            reservationDb.setCustomerEmail(gi.getEmail());
            reservationDb.setPhoneNumber(gi.getPhoneNumber());

        }else{ // Reservation is from a logged user
            reservationDb.setUpdatedAt(null);
            reservationDb.setUpdatedAt(null);
            reservationDb.setHoldExpiresAt(null);
            reservationDb.setUser(user);
            reservationDb.setCustomerName(null);
            reservationDb.setCustomerEmail(null);
            reservationDb.setPhoneNumber(null);
        }

        // Initially save the reservation
        ReservationsV1 savedReservation = reservationRepoService.saveReservation(reservationDb);


        // save the dietary and occasions if present
        if(rd.getDietary() != null){
            for(String item : rd.getDietary()){
                ReservationDietaryItem d = new ReservationDietaryItem();
                d.setDietaryItem(item);
                d.setReservationsV1(savedReservation);
                savedReservation.getDietaryItems().add(d);
            }
        }

        if(rd.getOccasion() != null){
            for(String item : rd.getOccasion()){
                ReservationOccasionItem o = new ReservationOccasionItem();
                o.setOccasionItem(item);
                o.setReservationsV1(savedReservation);
                savedReservation.getOccasionItems().add(o);
            }
        }

        // 5) Save again to flush children (cascade will handle it too)
        reservationRepoService.saveReservation(savedReservation);

        // 5.1) Notify admins via WebSocket
        var evt = new AdminReservationEvent(
                savedReservation.getReservationId(),
                (user != null ? user.getName() : gi.getFullName()),   // adjust getters
                rd.getGuestCount(),
                rd.getDate().toString(),
                rd.getTime().toString(),
                table.orElseThrow().getCode(),      // or savedReservation.getTable().getCode()
                table.orElseThrow().getZone()
        );
        adminNotifier.newReservation(evt);

        // 6) Cleanup hold (idempotent)
        reservationHoldService.deleteHold(tableId, dt);
    }




}
