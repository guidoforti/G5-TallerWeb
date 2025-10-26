package com.tallerwebi.presentacion.DTO.OutputsDTO;

import com.tallerwebi.dominio.Entity.Viaje;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ViajeReservaSolicitudDTO {

    private Long id;
    private String origenNombre;
    private String destinoNombre;
    private String fechaHoraDeSalida;
    private Double precio;
    private Integer asientosDisponibles;
    private String conductorNombre;

    public ViajeReservaSolicitudDTO(Viaje viaje) {
        this.id = viaje.getId();
        this.origenNombre = viaje.getOrigen() != null ? viaje.getOrigen().getNombre() : "Sin origen";
        this.destinoNombre = viaje.getDestino() != null ? viaje.getDestino().getNombre() : "Sin destino";
        this.fechaHoraDeSalida = viaje.getFechaHoraDeSalida() != null
                ? viaje.getFechaHoraDeSalida().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "Sin fecha";
        this.precio = viaje.getPrecio() != null ? viaje.getPrecio() : 0.0;
        this.asientosDisponibles = viaje.getAsientosDisponibles() != null ? viaje.getAsientosDisponibles() : 0;
        this.conductorNombre = viaje.getConductor() != null ? viaje.getConductor().getNombre() : "Sin conductor";
    }
}
