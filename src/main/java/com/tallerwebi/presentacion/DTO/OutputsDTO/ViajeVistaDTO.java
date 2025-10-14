package com.tallerwebi.presentacion.DTO.OutputsDTO;

import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Enums.EstadoDeViaje;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ViajeVistaDTO {

    private Long id;
    private String origen;
    private String destino;
    private String vehiculo;
    private String fechaSalida;
    private Integer asientosDisponibles;
    private String estado;


    public Viaje toEntity() {
        Viaje viaje = new Viaje();
        viaje.setId(this.id);
        viaje.setFechaHoraDeSalida(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").parse(this.fechaSalida, LocalDateTime::from));
        viaje.setAsientosDisponibles(this.asientosDisponibles);
        viaje.setEstado(Enum.valueOf(EstadoDeViaje.class, this.estado));
        // origen, destino y vehiculo se deben setear aparte si se necesita persistencia completa
        return viaje;
    }

    public ViajeVistaDTO(Viaje viaje) {
    this.id = viaje.getId();

    this.origen = viaje.getOrigen() != null ? viaje.getOrigen().getNombre() : "Sin origen";
    this.destino = viaje.getDestino() != null ? viaje.getDestino().getNombre() : "Sin destino";

    if (viaje.getVehiculo() != null) {
        this.vehiculo = viaje.getVehiculo().getModelo() + " - " + viaje.getVehiculo().getPatente();
    } else {
        this.vehiculo = "Sin vehículo";
    }

    this.fechaSalida = viaje.getFechaHoraDeSalida() != null
        ? viaje.getFechaHoraDeSalida().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        : "Sin fecha";

    this.asientosDisponibles = viaje.getAsientosDisponibles() != null
        ? viaje.getAsientosDisponibles()
        : 0;

    this.estado = viaje.getEstado() != null
        ? viaje.getEstado().name()
        : "SIN_ESTADO";
}
}