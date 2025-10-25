package com.tallerwebi.presentacion.DTO.OutputsDTO;

import com.tallerwebi.dominio.Entity.Viaje;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ViajeResultadoDTO {

    private Long id;
    private String nombreConductor;
    private String origen;
    private String destino;
    private String fechaSalida;              // Formatted: dd/MM/yyyy HH:mm
    private Double precio;
    private Integer asientosDisponibles;
    private String estado;

    // Constructor from Viaje entity
    public ViajeResultadoDTO(Viaje viaje) {
        this.id = viaje.getId();

        this.nombreConductor = viaje.getConductor() != null
                ? viaje.getConductor().getNombre()
                : "Sin conductor";

        this.origen = viaje.getOrigen() != null
                ? viaje.getOrigen().getNombre()
                : "Sin origen";

        this.destino = viaje.getDestino() != null
                ? viaje.getDestino().getNombre()
                : "Sin destino";

        this.fechaSalida = viaje.getFechaHoraDeSalida() != null
                ? viaje.getFechaHoraDeSalida().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "Sin fecha";

        this.precio = viaje.getPrecio() != null ? viaje.getPrecio() : 0.0;

        this.asientosDisponibles = viaje.getAsientosDisponibles() != null
                ? viaje.getAsientosDisponibles()
                : 0;

        this.estado = viaje.getEstado() != null
                ? viaje.getEstado().name()
                : "SIN_ESTADO";
    }
}
