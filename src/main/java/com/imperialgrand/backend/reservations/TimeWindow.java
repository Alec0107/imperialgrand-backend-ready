package com.imperialgrand.backend.reservations;

import java.time.LocalDateTime;

public record TimeWindow(LocalDateTime start,
                         LocalDateTime end)
{}
