package com.tallerwebi.presentacion.DTO.OutputsDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.tallerwebi.dominio.Entity.HistorialReserva;
import com.tallerwebi.dominio.Enums.EstadoReserva;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HistorialReservaDTO {

    private Long id;
    private Long idViaje;
    private String nombreViajero;
    private String emailViajero;
    private String nombreConductor;
    private LocalDateTime fechaEvento;
    private EstadoReserva estadoAnterior;
    private EstadoReserva estadoNuevo;
    
    public HistorialReservaDTO(HistorialReserva historial) {
    this.id = historial != null ? historial.getId() : null;

    // Información del viaje
    if (historial != null && historial.getReserva() != null && historial.getReserva().getViaje() != null) {
        this.idViaje = historial.getReserva().getViaje().getId();
    } else {
        this.idViaje = null;
    }

    // Información del viajero
    if (historial != null && historial.getReserva() != null && historial.getReserva().getViajero() != null) {
        this.nombreViajero = historial.getReserva().getViajero().getNombre() != null
                ? historial.getReserva().getViajero().getNombre()
                : "Sin nombre";
        this.emailViajero = historial.getReserva().getViajero().getEmail() != null
                ? historial.getReserva().getViajero().getEmail()
                : "Sin email";
    } else {
        this.nombreViajero = "Sin nombre";
        this.emailViajero = "Sin email";
    }

    // Información del conductor
    if (historial != null && historial.getConductor() != null) {
        this.nombreConductor = historial.getConductor().getNombre() != null
                ? historial.getConductor().getNombre()
                : "Sin nombre";
    } else {
        this.nombreConductor = "Sin conductor";
    }

    // Fecha del evento (formateada)
    if (historial != null && historial.getFechaEvento() != null) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        this.fechaEvento = historial.getFechaEvento();
    } else {
        this.fechaEvento = null;
    }

    // Estados (anterior y nuevo)
    this.estadoAnterior = historial != null ? historial.getEstadoAnterior() : null;
    this.estadoNuevo = historial != null ? historial.getEstadoNuevo() : null;
    }
}
