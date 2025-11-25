package com.tallerwebi.presentacion.DTO.OutputsDTO;

import com.tallerwebi.dominio.Entity.Reserva;
import com.tallerwebi.dominio.Enums.EstadoReserva;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DTO para mostrar información de una reserva en la vista
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReservaVistaDTO {

    private Long id;
    private Long viajeId;
    private String origenViaje;
    private String destinoViaje;
    private String fechaSalidaViaje;
    private String viajeroNombre;
    private String viajeroEmail;
    private String viajeroFotoUrl;
    private String fechaSolicitud;
    private String estado;
    private String motivoRechazo;

    /**
     * Constructor que convierte una entidad Reserva a DTO
     * con valores por defecto null-safe
     */
    public ReservaVistaDTO(Reserva reserva) {
        this.id = reserva.getId();

        // Información del viaje
        if (reserva.getViaje() != null) {
            this.viajeId = reserva.getViaje().getId();
            this.origenViaje = reserva.getViaje().getOrigen() != null
                    ? reserva.getViaje().getOrigen().getNombre()
                    : "Sin origen";
            this.destinoViaje = reserva.getViaje().getDestino() != null
                    ? reserva.getViaje().getDestino().getNombre()
                    : "Sin destino";
            this.fechaSalidaViaje = reserva.getViaje().getFechaHoraDeSalida() != null
                    ? reserva.getViaje().getFechaHoraDeSalida().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    : "Sin fecha";
            this.viajeroFotoUrl = reserva.getViajero().getFotoPerfilUrl();
        } else {
            this.viajeId = null;
            this.origenViaje = "Sin origen";
            this.destinoViaje = "Sin destino";
            this.fechaSalidaViaje = "Sin fecha";
            this.viajeroFotoUrl = null;
        }

        // Información del viajero
        if (reserva.getViajero() != null) {
            this.viajeroNombre = reserva.getViajero().getNombre() != null
                    ? reserva.getViajero().getNombre()
                    : "Sin nombre";
            this.viajeroEmail = reserva.getViajero().getEmail() != null
                    ? reserva.getViajero().getEmail()
                    : "Sin email";
        } else {
            this.viajeroNombre = "Sin nombre";
            this.viajeroEmail = "Sin email";
        }

        // Información de la reserva
        this.fechaSolicitud = reserva.getFechaSolicitud() != null
                ? reserva.getFechaSolicitud().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "Sin fecha";

        this.estado = reserva.getEstado() != null
                ? reserva.getEstado().name()
                : "SIN_ESTADO";

        this.motivoRechazo = reserva.getMotivoRechazo();
    }
}
