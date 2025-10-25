package com.tallerwebi.presentacion.DTO.OutputsDTO;

import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.presentacion.DTO.CiudadDTO;
import com.tallerwebi.presentacion.DTO.ParadaDTO;
import com.tallerwebi.presentacion.DTO.ViajeroDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DetalleViajeOutputDTO {
    private CiudadDTO origen;
    private CiudadDTO destino;
    private VehiculoOutputDTO vehiculo;
    private List<ViajeroDTO> viajeros;
    private List<ParadaDTO> paradas;
    private LocalDateTime fechaHoraDeSalida;
    private Integer asientosDisponibles;

    public DetalleViajeOutputDTO (Viaje viaje, List<ViajeroDTO> viajeros) {
        this.origen = new CiudadDTO(viaje.getOrigen());
        this.destino = new CiudadDTO(viaje.getDestino());
        this.vehiculo = new VehiculoOutputDTO(viaje.getVehiculo());
        this.viajeros = viajeros;
        this.paradas = viaje.getParadas().stream()
                .map(p -> new ParadaDTO(p))
                .collect(Collectors.toList());
        this.fechaHoraDeSalida = viaje.getFechaHoraDeSalida();
        this.asientosDisponibles = viaje.getAsientosDisponibles();
    }
}
