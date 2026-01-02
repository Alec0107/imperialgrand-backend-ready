package com.imperialgrand.backend.UserDashboard;

import com.imperialgrand.backend.authentication.DTO.User;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.List;

public record PageResponse<T>(
        List<T> data,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean isLast
        ) {
}
