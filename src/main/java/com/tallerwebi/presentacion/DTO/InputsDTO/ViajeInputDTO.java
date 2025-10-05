package com.tallerwebi.presentacion.DTO.InputsDTO;

import com.tallerwebi.presentacion.DTO.CiudadDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ViajeInputDTO {

    private Long conductorId;
    private CiudadDTO ciudadOrigen;
    private CiudadDTO ciudadDestino;
    private LocalDateTime fechaHoraDeSalida;
    private Double precio;
    private Integer asientosDisponibles;
    private Long idVehiculo;


}
