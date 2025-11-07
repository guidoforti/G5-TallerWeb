package com.tallerwebi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Prefijo para suscripciones. El cliente se suscribe a /topic/notificaciones/{idUsuario}
        config.enableSimpleBroker("/topic");
        // Prefijo para los endpoints mapeados por @MessageMapping (ej: Controller)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint que usa el cliente para conectarse
        registry.addEndpoint("/ws-notificaciones")
                .withSockJS(); // Soporte para navegadores antiguos
    }
}