package com.tallerwebi.dominio.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

//@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ubicacion {


    private String direccion;
   /* private String ciudad;
    private String provincia;
    private String pais;*/
    private Double latitud;
    private Double longitud;
    private Long id;

    public Ubicacion(Long id, Double latitud, Double longitud, String direccion) {
        this.id = id;
        this.latitud = latitud;
        this.longitud = longitud;
        this.direccion = direccion;
    }

}
