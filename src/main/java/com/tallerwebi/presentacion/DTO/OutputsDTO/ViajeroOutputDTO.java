package com.tallerwebi.presentacion.DTO.OutputsDTO;

import com.tallerwebi.dominio.Entity.Viajero;

public class ViajeroOutputDTO {
    
    private String nombre;
    private String email;
    private Integer edad;

    public ViajeroOutputDTO(Viajero viajero){
        this.nombre = viajero.getNombre();
        this.email = viajero.getEmail();
        this.edad = viajero.getEdad();
    }
}
