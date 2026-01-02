package com.imperialgrand.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")                   // <- must match new SockJS("/ws")
                .setAllowedOriginPatterns("*")       // tighten later to your domains
                .withSockJS();                       // SockJS fallback
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Client subscribes to destinations starting with /topic
        registry.enableSimpleBroker("/topic");

        // Client sends to destinations starting with /app (for @MessageMapping)
        registry.setApplicationDestinationPrefixes("/app");
    }
}