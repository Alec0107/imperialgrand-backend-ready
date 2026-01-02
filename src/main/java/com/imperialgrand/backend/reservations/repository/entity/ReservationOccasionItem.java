package com.imperialgrand.backend.reservations.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "reservation_occasion_item")
public class ReservationOccasionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "reservation_id")
    private ReservationsV1 reservationsV1;

    private String occasionItem;

}
