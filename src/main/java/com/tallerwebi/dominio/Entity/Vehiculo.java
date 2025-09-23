package com.tallerwebi.dominio.Entity;

import com.tallerwebi.dominio.Enums.EstadoVerificacion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Vehiculo {

    private Long id;
    private Conductor conductor;
    private String Modelo;
    private String anio;
    private String patente;
    private Integer asientosTotales;
    private EstadoVerificacion estadoVerificacion;
}
