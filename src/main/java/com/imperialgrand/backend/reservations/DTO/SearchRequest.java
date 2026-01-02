package com.imperialgrand.backend.reservations.DTO;

public record SearchRequest(String date,
                            String time,
                            int partySize)
{}

