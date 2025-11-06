package com.tallerwebi.presentacion.DTO.OutputsDTO;

import com.tallerwebi.dominio.Entity.Reserva;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.format.DateTimeFormatter;

/**
 * DTO para mostrar reservas pendientes y rechazadas de un viajero
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservaActivaDTO {

    private Long reservaId;
    private Long viajeId;
    private String origenNombre;
    private String destinoNombre;
    private String fechaSalida;
    private String conductorNombre;
    private Double precio;
    private String estado;
    private String motivoRechazo;

    /**
     * Constructor que convierte una entidad Reserva a DTO
     * con valores por defecto null-safe
     */
    public ReservaActivaDTO(Reserva reserva) {
        this.reservaId = reserva.getId();

        // Información del viaje
        if (reserva.getViaje() != null) {
            this.viajeId = reserva.getViaje().getId();

            this.origenNombre = reserva.getViaje().getOrigen() != null
                    ? reserva.getViaje().getOrigen().getNombre()
                    : "Origen no disponible";

            this.destinoNombre = reserva.getViaje().getDestino() != null
                    ? reserva.getViaje().getDestino().getNombre()
                    : "Destino no disponible";

            this.fechaSalida = reserva.getViaje().getFechaHoraDeSalida() != null
                    ? reserva.getViaje().getFechaHoraDeSalida().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    : "Fecha no disponible";

            this.precio = reserva.getViaje().getPrecio() != null
                    ? reserva.getViaje().getPrecio()
                    : 0.0;

            // Información del conductor
            if (reserva.getViaje().getConductor() != null) {
                this.conductorNombre = reserva.getViaje().getConductor().getNombre() != null
                        ? reserva.getViaje().getConductor().getNombre()
                        : "Conductor no disponible";
            } else {
                this.conductorNombre = "Conductor no disponible";
            }
        } else {
            this.viajeId = null;
            this.origenNombre = "Origen no disponible";
            this.destinoNombre = "Destino no disponible";
            this.fechaSalida = "Fecha no disponible";
            this.precio = 0.0;
            this.conductorNombre = "Conductor no disponible";
        }

        // Información de la reserva
        this.estado = reserva.getEstado() != null
                ? reserva.getEstado().name()
                : "SIN_ESTADO";

        this.motivoRechazo = reserva.getMotivoRechazo();
    }
}
