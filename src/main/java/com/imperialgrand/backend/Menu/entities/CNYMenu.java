package com.imperialgrand.backend.Menu.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cny_menus")
public class CNYMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String category;

    private String nameEn;
    private String nameCn;

    @Column(columnDefinition = "TEXT")
    private String coursesJson;

    private String imageUrl;

    private boolean isActive = true;

    @OneToMany(
            mappedBy = "cnyMenu",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<MenuOption> options = new ArrayList<>();

}
