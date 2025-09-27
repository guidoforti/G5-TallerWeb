package com.tallerwebi.presentacion.DTO.OutputsDTO;

import com.tallerwebi.dominio.Entity.Vehiculo;
import com.tallerwebi.dominio.Enums.EstadoVerificacion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VehiculoOutputDTO {
    private String modelo;
    private String anio;
    private String patente;
    private Integer asientosTotales;
    private EstadoVerificacion estadoVerificacion;


    public VehiculoOutputDTO (Vehiculo vehiculo) {

        this.modelo = vehiculo.getModelo();
        this.anio  = vehiculo.getAnio();
        this.patente = vehiculo.getPatente();
        this.asientosTotales = vehiculo.getAsientosTotales();
        this.estadoVerificacion = vehiculo.getEstadoVerificacion();
    }
}
