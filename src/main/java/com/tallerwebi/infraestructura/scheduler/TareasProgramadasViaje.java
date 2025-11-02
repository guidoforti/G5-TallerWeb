package com.tallerwebi.infraestructura.scheduler;

import com.tallerwebi.dominio.IServicio.ServicioViaje;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Tareas programadas para gestión automática de viajes
 */
@Component
public class TareasProgramadasViaje {

    private final ServicioViaje servicioViaje;

    public TareasProgramadasViaje(ServicioViaje servicioViaje) {
        this.servicioViaje = servicioViaje;
    }

    /**
     * Cierra viajes olvidados cada 30 minutos
     * Ejecuta a los minutos 0 y 30 de cada hora
     */
    @Scheduled(cron = "0 0,30 * * * *")
    public void cerrarViajesOlvidados() {
        servicioViaje.cerrarViajesOlvidados();
    }

    /**
     * Inicia automáticamente viajes atrasados (cada 5 minutos)
     * Inicia viajes que están 15+ minutos pasados de su hora de salida
     */
    @Scheduled(fixedRate = 300000) // 5 minutos en milisegundos
    public void iniciarViajesAtrasados() {
        servicioViaje.iniciarViajesAtrasados();
    }
}
