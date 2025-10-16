package com.tallerwebi.presentacion.DTO.OutputsDTO;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Viaje;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

public class ConductorOutputDTO {



    private String nombre;
    private String email;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaDeVencimientoLicencia;


    public ConductorOutputDTO(Conductor conductor) {

        this.nombre = conductor.getNombre();
        this.email = conductor.getEmail();
        this.fechaDeVencimientoLicencia = conductor.getFechaDeVencimientoLicencia();

    }
}
