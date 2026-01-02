package com.imperialgrand.backend.reservations.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "reservation_dietary_item")
public class ReservationDietaryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "reservation_id")
    private ReservationsV1 reservationsV1;

    private String dietaryItem;

}
