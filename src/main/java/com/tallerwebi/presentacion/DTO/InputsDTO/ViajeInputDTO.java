package com.tallerwebi.presentacion.DTO.InputsDTO;

import com.tallerwebi.presentacion.DTO.UbicacionDTO;
import com.tallerwebi.presentacion.DTO.VehiculoDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ViajeInputDTO {
    private Long conductorId;
    private Long idOrigen;
    private Long idDestino;
    private LocalDateTime fechaHoraDeSalida;
    private Double precio;
    private Integer asientosDisponibles;
    private Long idVehiculo;
}
