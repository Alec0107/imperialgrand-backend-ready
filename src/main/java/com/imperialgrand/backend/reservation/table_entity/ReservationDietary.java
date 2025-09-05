package com.imperialgrand.backend.reservation.table_entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "reservation_dietary")
public class ReservationDietary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer dietaryId;

    private String restriction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;
}
