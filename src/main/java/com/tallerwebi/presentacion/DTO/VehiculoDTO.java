package com.tallerwebi.presentacion.DTO;

import com.tallerwebi.dominio.Enums.EstadoVerificacion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VehiculoDTO {
    private String modelo;
    private String anio;
    private String patente;
    private Integer asientosTotales;
    private EstadoVerificacion estadoVerificacion;
}
