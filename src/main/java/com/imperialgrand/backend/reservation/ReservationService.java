package com.imperialgrand.backend.reservation;

import com.imperialgrand.backend.common.utils.InputValidator;
import com.imperialgrand.backend.redis.reservation_locks.ReservationLockingService;
import com.imperialgrand.backend.reservation.dto.*;
import com.imperialgrand.backend.reservation.enums.ReservationStatus;
import com.imperialgrand.backend.reservation.exception.NoAvailableTableException;
import com.imperialgrand.backend.reservation.exception.ReservationNoLongerHeldException;
import com.imperialgrand.backend.reservation.table_entity.Reservation;
import com.imperialgrand.backend.reservation.table_entity.ReservationDietary;
import com.imperialgrand.backend.reservation.table_entity.ReservationOccasion;
import com.imperialgrand.backend.reservation.table_entity.TableEntity;
import com.imperialgrand.backend.reservation.reservation_repository.ReservationRepositoryService;
import com.imperialgrand.backend.reservation.tables_repository.TableRepositoryService;
import com.imperialgrand.backend.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepositoryService reservationRepositoryService;
    private final TableRepositoryService tableRepositoryService;
    private final ReservationLockingService reservationLockingService;

    private final Logger logger = Logger.getLogger(ReservationService.class.getName());

    public ReservationLockStatusDTO checkReservationAvailability(ReservationDTO reservation){
        //System.out.println(reservation.getTime().toString());
        List<TableEntity> availableTableEntity;
        // fetch all ids that are already booked based on the date and the time range before 30 mins and after 30mins of user's preferred time
        List<Integer> bookedTableIds = reservationRepositoryService.findBookedTablesId(reservation.getDate(), reservation.getTime());

        // if no tables are booked then fetch all tables entity so we can check the capacity f each table entity best is if exact or greater than the guest count (smallest possible)
        if(bookedTableIds.isEmpty()){
            availableTableEntity = sortTableCapacity(tableRepositoryService.finAllTableEntities(), reservation.getGuestCount());
        }else{
            availableTableEntity = sortTableCapacity(tableRepositoryService.fetchTablesExcludedTheIds(bookedTableIds), reservation.getGuestCount());
        }

        if (availableTableEntity.isEmpty()) {
            logger.warning("No suitable tables found. Guest count too high: " + reservation.getGuestCount());
            throw new NoAvailableTableException("No tables can handle " + reservation.getGuestCount() + " guests.");
        }

        TableEntity selectedTableEntity = null;

        // iterate the list and check redis whether the first index or other index tables are in redis or not
        for(TableEntity tableEntity : availableTableEntity){
            if(!reservationLockingService.isTableLocked(tableEntity.getTableId(), reservation)){
                logger.info("Table " + tableEntity.getTableId() + " is obtained");
                selectedTableEntity = tableEntity;
                break;
            }else{
                logger.info(tableEntity.getName() + " is assigned to " + reservation.getGuestCount() + " guests");
            }
        }

        if(selectedTableEntity == null){
            logger.warning("No available tables. Please change date, time or guest count.");
            throw new NoAvailableTableException("No available tables. Please change date, time or guest count.");
        }

        // lock the table and return the object;
        return reservationLockingService.lockTable(selectedTableEntity.getTableId(), selectedTableEntity.getName(), reservation);
    }

    //  ascending sorting and fetch the first index (0)
    private List<TableEntity> sortTableCapacity(List<TableEntity> tableEntities, int guestCount){
        List<TableEntity> suitable = tableEntities.stream()
                .filter(t -> t.getGuestCapacity() >= guestCount)
                .collect(Collectors.toList());

        suitable.sort(Comparator.comparingInt(TableEntity::getGuestCapacity));
        return suitable;
    }


    // a function use to check reservation lock status
    public ReservationLockStatusDTO checkLockStatus(int tableId, LocalDate date, LocalTime time){
        return reservationLockingService.isLockValid(tableId, date, time);
    }

    @Transactional
    // function for saving reservation in the database
    public Map<String, Object> saveReservation(User user, SubmissionPayload dto) {
        ReservationDetailsDTO reservationDetailsDTO = dto.getReservationDetails();
        GuestInfoDTO guestInfoDTO = dto.getGuestInfo();

        /** Step 1: Fetch the target table entity **/
        TableEntity table = tableRepositoryService.getTableEntityById(reservationDetailsDTO.getTableId());

        // Step 2: Begin building the reservation
        Reservation.ReservationBuilder builder = Reservation.builder()
                .date(reservationDetailsDTO.getDate())
                .time(reservationDetailsDTO.getTime())
                .guestCount(reservationDetailsDTO.getGuestCount())
                .specialRequest(reservationDetailsDTO.getMessage())
                .status(ReservationStatus.PENDING)
                .savedAt(LocalDateTime.now())
                .table(table);

        /** Step 3: Handle reservation depending on user type **/
        if (user != null) {
            /** Logged-in user: attach user entity to reservation **/
            logger.info("Reserving a table via authenticated User");
            builder.user(user);
        } else {
            /**  Guest user: validate and set guest fields
                 Validate guest inputs (first name, last name, email, phone) **/
            InputValidator.runValidation(InputValidator::validateName, guestInfoDTO.getFirstName(), "First name");
            InputValidator.runValidation(InputValidator::validateName, guestInfoDTO.getLastName(), "Last name");
            InputValidator.runValidation(InputValidator::validateEmail, guestInfoDTO.getEmail());

            /** Add guest details to the reservation **/
            builder.firstName(guestInfoDTO.getFirstName())
                    .lastName(guestInfoDTO.getLastName())
                    .email(guestInfoDTO.getEmail())
                    .phoneNumber(guestInfoDTO.getPhoneNumber());
        }

        /** Step 4: Check Redis to see if the reservation lock is still active **/
        boolean lockActive = reservationLockingService.isReservationLockActive(
                reservationDetailsDTO.getTableId(),
                reservationDetailsDTO.getDate(),
                reservationDetailsDTO.getTime());

        if (!lockActive) {
            /** Lock is gone: throw an exception to prevent invalid reservation **/
            throw new ReservationNoLongerHeldException("Reservation slot has expired or is no longer held. Please make a new one.");
        }

        /** Step 5: Finalize and save the reservation **/
        Reservation reservation = builder.build();

        /** Step 6: Save the occasions / dietary restriction (optional) **/
        if(reservationDetailsDTO.getOccasion() != null && reservationDetailsDTO.getOccasion().length > 0){
            for(String occasion : reservationDetailsDTO.getOccasion()){
                ReservationOccasion occ = new ReservationOccasion();
                occ.setOccasion(occasion); // set the string like "Proposal"
                occ.setReservation(reservation); // set the parent object (required for FK)
                reservation.getOccasions().add(occ); // add it to the list in Reservation
            }
        }

        if(reservationDetailsDTO.getDietary() != null && reservationDetailsDTO.getDietary().length > 0){
            for(String restriction : reservationDetailsDTO.getDietary()){
                ReservationDietary restic = new ReservationDietary();
                restic.setReservation(reservation);
                restic.setRestriction(restriction);
                reservation.getDietaryRestrictions().add(restic);
            }
        }




        /** Step 6: Save the reservation**/
        reservationRepositoryService.saveReservationNative(reservation);




        System.out.print(String.format("Reservation saved successfully for table %s on %s at %s",
                reservationDetailsDTO.getTableId(),
                reservationDetailsDTO.getDate(),
                reservationDetailsDTO.getTime()));

        Map<String, Object> map = new HashMap<>();
        map.put("tableId", reservationDetailsDTO.getTableId());
        map.put("date", reservationDetailsDTO.getDate());
        map.put("time", reservationDetailsDTO.getTime());
        return map;
        }





}
