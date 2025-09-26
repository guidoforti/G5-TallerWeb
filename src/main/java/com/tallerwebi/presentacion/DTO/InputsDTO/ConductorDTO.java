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
public class ConductorDTO {

    private Long id;
    private String nombre;
    private String email;
    private String contrasenia;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaDeVencimientoLicencia;
    private List<Viaje> viajes;

    public ConductorDTO (Conductor conductor) {
        this.id = conductor.getId();
        this.nombre = conductor.getNombre();
        this.email = conductor.getEmail();
        // para evitar esto tenemos que crear si o si un input output dto
        this.contrasenia = conductor.getContrasenia();
        this.fechaDeVencimientoLicencia = conductor.getFechaDeVencimientoLicencia();
        this.viajes = conductor.getViajes();
    }


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
