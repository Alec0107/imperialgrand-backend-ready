package com.imperialgrand.backend.Menu.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name = "menu_categories")
public class CategoryDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private String name;
    @Column(nullable = false, unique = true) private String slug;
    @Column(name="display_order" , nullable = false) private Integer displayOrder;
}
