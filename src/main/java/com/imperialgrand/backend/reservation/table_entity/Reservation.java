package com.imperialgrand.backend.reservation.table_entity;

import com.imperialgrand.backend.reservation.enums.ReservationStatus;
import com.imperialgrand.backend.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reservationId;

    private LocalDate date;
    private LocalTime time;
    private int guestCount;

    // Guest reservation info (nullable because logged-in users won’t fill this)
    @Column(nullable = true)
    private String firstName;

    @Column(nullable = true)
    private String lastName;

    @Column(nullable = true)
    private String email;

    @Column(nullable = true)
    private String phoneNumber;

    private String specialRequest;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private LocalDateTime savedAt;

    // Link to the table being reserved (many reservations can share the same table)
    @ManyToOne
    @JoinColumn(name = "table_id")
    private TableEntity table;

    // Link to logged-in user who made the reservation (nullable for guest users)
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    // ✅ Used to store multiple occasion tags (e.g. Proposal, Birthday)
    // `mappedBy = "reservation"` → tells JPA that ReservationOccasion owns the FK
    // `@Builder.Default` → ensures the list is NOT null when using Lombok's builder
    @Builder.Default
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservationOccasion> occasions = new ArrayList<>();

    // ✅ Used to store multiple dietary restrictions (e.g. Vegan, Nut Allergy)
    @Builder.Default
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservationDietary> dietaryRestrictions = new ArrayList<>();
}
