package com.tallerwebi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuración para habilitar tareas programadas (CRON jobs)
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // Esta clase habilita el soporte de @Scheduled en toda la aplicación
}
