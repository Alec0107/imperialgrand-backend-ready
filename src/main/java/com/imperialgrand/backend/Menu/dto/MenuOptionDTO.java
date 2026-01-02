package com.imperialgrand.backend.Menu.dto;

import com.imperialgrand.backend.Menu.entities.PricingModel;

public record MenuOptionDTO(
        long id,
        PricingModel pricingModel,
        Integer pax,
        Integer minPax,
        Integer priceCents,
        String priceSuffix
) {
}
