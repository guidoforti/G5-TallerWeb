package com.tallerwebi.presentacion.DTO.OutputsDTO;

import java.util.List;

import com.tallerwebi.dominio.Entity.Viajero;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ViajeroPerfilOutPutDTO {
    
    private Long id;
    private String nombre;
    private Integer edad;
    private Boolean fumador;
    private Boolean discapacitado;
    private String fotoPerfilUrl;
    private Double promedioValoraciones;
    private List<ValoracionOutputDTO> valoraciones;

     public ViajeroPerfilOutPutDTO(Viajero viajero, List<ValoracionOutputDTO> valoraciones, Double promedio) {
        this.id = viajero.getId();
        this.nombre = viajero.getNombre();
        this.edad = viajero.getEdad();
        this.fumador = viajero.getFumador();
        this.fotoPerfilUrl = viajero.getFotoPerfilUrl();
        this.discapacitado = viajero.getDiscapacitado();
        this.promedioValoraciones = promedio;
        this.valoraciones = valoraciones;
    }
}
