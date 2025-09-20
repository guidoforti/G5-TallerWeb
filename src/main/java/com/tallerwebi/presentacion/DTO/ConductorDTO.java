package com.tallerwebi.presentacion.DTO;

import com.tallerwebi.dominio.Entity.Viaje;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConductorDTO {

    private List<Viaje> viajes;
    private String nombre;
    private String email;
    private LocalDate fechaDeVencimientoLicencia;
}
