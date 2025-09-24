package com.tallerwebi.presentacion.DTO.InputsDTO;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Enums.EstadoVerificacion;
import com.tallerwebi.presentacion.DTO.ConductorDTO;
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
}
