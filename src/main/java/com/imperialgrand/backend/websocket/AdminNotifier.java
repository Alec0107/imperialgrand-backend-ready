package com.imperialgrand.backend.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminNotifier {
    private final SimpMessagingTemplate messaging;

    public void newReservation(AdminReservationEvent event) {
        messaging.convertAndSend("/topic/admin/reservations", event);
    }

    // you can add more later (updates, cancellations, etc.)
}