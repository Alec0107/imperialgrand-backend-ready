package com.imperialgrand.backend.Menu.dto;

import com.imperialgrand.backend.Menu.entities.MenuOption;

import java.util.List;

public record CNYMenuResponseDTO(
        Long id,
        String category,
        String nameEn,
        String nameCn,
        List<MenuOptionDTO> options
) { }
