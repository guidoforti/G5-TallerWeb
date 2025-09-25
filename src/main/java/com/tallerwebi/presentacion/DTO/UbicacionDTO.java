package com.tallerwebi.presentacion.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UbicacionDTO {
    private String direccion;
    private Float latitud;
    private Float longitud;

}
