package com.tallerwebi.presentacion.DTO.OutputsDTO;

import com.tallerwebi.presentacion.DTO.UbicacionDTO;
import com.tallerwebi.presentacion.DTO.VehiculoDTO;
import com.tallerwebi.presentacion.DTO.ViajeroDTO;
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
public class ViajeOutputDTO {
    private UbicacionDTO origen;
    private UbicacionDTO destino;
    private List<UbicacionDTO> paradas;
    private LocalDateTime fechaHoraDeSalida;
    private Double precio;
    private Integer asientosDisponibles;
    private Integer asientosTotales;
    private LocalDateTime fechaDeCreacion;
    private VehiculoDTO vehiculo;
    private String nombreConductor;
    private List<ViajeroDTO> viajeros;
}
