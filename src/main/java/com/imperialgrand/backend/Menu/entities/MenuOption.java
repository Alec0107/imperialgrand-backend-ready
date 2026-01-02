package com.imperialgrand.backend.Menu.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "menu_options")
public class MenuOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cny_menu_id", nullable = false)
    private CNYMenu cnyMenu;

    @Enumerated(EnumType.STRING)
    private PricingModel pricingModel;

    private Integer pax;
    private Integer minPax;

    private Integer priceCents;
    private String priceSuffix;

    private boolean isActive = true;

}
