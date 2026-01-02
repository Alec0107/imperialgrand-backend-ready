package com.imperialgrand.backend.reservations.repository.entity;

import com.imperialgrand.backend.authentication.DTO.User;
import com.imperialgrand.backend.reservations.Tables.ReservationStatus;
import com.imperialgrand.backend.reservations.Tables.Tables;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "reservation")
@Table(name = "reservation")
public class ReservationsV1 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reservationId;

    @ManyToOne(optional = false)
    private Tables table;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private int guestCount;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @ManyToOne(optional = true)
    private User user;

    private String customerName;
    private String customerEmail;
    private String phoneNumber;

    private String note;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private LocalDateTime holdExpiresAt;


    // Bi-directional
    @OneToMany(mappedBy = "reservationsV1", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<ReservationDietaryItem> dietaryItems = new ArrayList<>();

    @OneToMany(mappedBy = "reservationsV1", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<ReservationOccasionItem> occasionItems = new ArrayList<>();

}
