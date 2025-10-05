package com.tallerwebi.presentacion.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ViajeroDTO {
    private Long id;
    private String email;
    private String nombre;
    private Integer edad;
    // No incluimos la lista de viajes para evitar referencias circulares
}
