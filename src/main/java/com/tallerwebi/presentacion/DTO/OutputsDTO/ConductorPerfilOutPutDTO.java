package com.tallerwebi.presentacion.DTO.OutputsDTO;

import java.time.LocalDate;
import java.util.List;

import com.tallerwebi.dominio.Entity.Conductor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConductorPerfilOutPutDTO {

    private String nombre;
    private Boolean fumador;
    private String discapacitado;
    private Integer edad;
    private String fotoPerfilUrl;
    private double promedioValoraciones;
    private LocalDate fechaVencimientoLicencia;
    private List<ValoracionOutputDTO> valoraciones;

    public ConductorPerfilOutPutDTO(Conductor conductor, List<ValoracionOutputDTO> valoraciones, double promedio) {
        this.nombre = conductor.getNombre();
        this.fumador = conductor.getFumador();
        this.discapacitado = conductor.getDiscapacitado();
        this.edad = conductor.getEdad();
        this.fotoPerfilUrl = conductor.getFotoPerfilUrl();
        this.valoraciones = valoraciones;
        this.promedioValoraciones = promedio;
        this.fechaVencimientoLicencia = conductor.getFechaDeVencimientoLicencia();
    }
}
