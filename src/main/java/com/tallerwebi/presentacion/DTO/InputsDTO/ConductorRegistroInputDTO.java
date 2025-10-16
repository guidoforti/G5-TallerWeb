package com.tallerwebi.presentacion.DTO.InputsDTO;

import com.tallerwebi.dominio.Entity.Conductor;
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
public class ConductorRegistroInputDTO {

    private Long id;
    private String nombre;
    private String email;
    private String contrasenia;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaDeVencimientoLicencia;
    private List<Viaje> viajes;


    public Conductor toEntity () {
        Conductor conductor = new Conductor();
        conductor.setId(this.getId());
        conductor.setEmail(this.getEmail());
        conductor.setContrasenia(this.getContrasenia());
        conductor.setNombre(this.getNombre());
        conductor.setFechaDeVencimientoLicencia(this.getFechaDeVencimientoLicencia());
        conductor.setViajes(this.getViajes());
        return conductor;
    }
}
