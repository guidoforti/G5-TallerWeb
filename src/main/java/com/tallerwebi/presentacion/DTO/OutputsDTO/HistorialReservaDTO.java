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
    private EstadoReserva estadoFinal;
    
    /* 
    private Long id;
    private Long reservaId;
    private String nombreViajero;
    private String origenViaje;
    private String destinoViaje;
    private String estadoAnterior;
    private String estadoNuevo;
    private String motivo;
    private String fechaCambio;
    */

    /**
     * Constructor que convierte una entidad HistorialReserva en DTO.
     * Null-safe y con formato legible.
     */
   /*
   public HistorialReservaDTO(HistorialReserva historial) {
        this.id = historial.getId();

        if (historial.getReserva() != null) {
            this.reservaId = historial.getReserva().getId();

            if (historial.getReserva().getViajero() != null) {
                this.nombreViajero = historial.getReserva().getViajero().getNombre();
            } else {
                this.nombreViajero = "Sin viajero";
            }

            if (historial.getReserva().getViaje() != null) {
                this.origenViaje = historial.getReserva().getViaje().getOrigen() != null
                        ? historial.getReserva().getViaje().getOrigen().getNombre()
                        : "Sin origen";
                this.destinoViaje = historial.getReserva().getViaje().getDestino() != null
                        ? historial.getReserva().getViaje().getDestino().getNombre()
                        : "Sin destino";
            } else {
                this.origenViaje = "Sin origen";
                this.destinoViaje = "Sin destino";
            }

        } else {
            this.reservaId = null;
            this.nombreViajero = "Sin viajero";
            this.origenViaje = "Sin origen";
            this.destinoViaje = "Sin destino";
        }

        this.estadoAnterior = historial.getEstadoAnterior() != null
                ? historial.getEstadoAnterior()
                : "SIN_ESTADO";

        this.estadoNuevo = historial.getEstadoNuevo() != null
                ? historial.getEstadoNuevo()
                : "SIN_ESTADO";

        this.motivo = historial.getMotivo() != null ? historial.getMotivo() : "Sin motivo";

        this.fechaCambio = historial.getFechaCambio() != null
                ? historial.getFechaCambio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "Sin fecha";
    }
    */ 
}
