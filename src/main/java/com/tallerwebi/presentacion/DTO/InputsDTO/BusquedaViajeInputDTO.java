package com.tallerwebi.presentacion.DTO.InputsDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusquedaViajeInputDTO {

    private String nombreCiudadOrigen;      // Required
    private String nombreCiudadDestino;     // Required

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime fechaSalida;      // Optional

    private Double precioMin;               // Optional
    private Double precioMax;               // Optional
}
