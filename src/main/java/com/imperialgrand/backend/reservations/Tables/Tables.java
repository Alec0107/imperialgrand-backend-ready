package com.imperialgrand.backend.reservations.Tables;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZoneId;


@Data
@Builder
@Entity(name="restaurant_table")
@Table(
        name="restaurant_table",
        indexes = {
                @Index(name = "idx_active_capacity", columnList = "is_active, capacity")
        }
)

@AllArgsConstructor
@NoArgsConstructor
public class Tables {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String code;

    @Column(nullable = false)
    private Integer capacity;

    @Column
    private final Boolean isActive = true;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Zone zone;

    private String notes;
}



