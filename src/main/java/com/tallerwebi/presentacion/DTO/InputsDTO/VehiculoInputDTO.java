package com.tallerwebi.presentacion.DTO.InputsDTO;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Vehiculo;
import com.tallerwebi.dominio.Enums.EstadoVerificacion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VehiculoInputDTO {


    private String Modelo;
    private String anio;
    private String patente;
    private Integer asientosTotales;
    private EstadoVerificacion estadoVerificacion;





    public Vehiculo toEntity (Conductor conductor) {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setConductor(conductor);
        vehiculo.setModelo(this.getModelo());
        vehiculo.setAnio(this.getAnio());
        vehiculo.setPatente(this.getPatente());
        vehiculo.setAsientosTotales(this.getAsientosTotales());
        vehiculo.setEstadoVerificacion(this.getEstadoVerificacion());
        return  vehiculo;
    }


}
