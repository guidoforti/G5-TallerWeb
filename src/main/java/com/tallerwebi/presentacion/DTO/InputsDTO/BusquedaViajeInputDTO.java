package com.tallerwebi.presentacion.DTO.InputsDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusquedaViajeInputDTO {

    private String nombreCiudadOrigen;      // Required
    private String nombreCiudadDestino;     // Required

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaSalida;          // Optional - date only

    private Double precioMin;               // Optional
    private Double precioMax;               // Optional
}
