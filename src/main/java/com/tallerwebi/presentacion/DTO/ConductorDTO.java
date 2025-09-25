package com.tallerwebi.presentacion.DTO;

import com.tallerwebi.dominio.Entity.Viaje;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConductorDTO {

    private Long id;
    private String nombre;
    private String email;
    private String contrasenia;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaDeVencimientoLicencia;
    private List<Viaje> viajes;
}
