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
    private float latitud;
    private float longitud;

    public Ubicacion(float latitud, float longitud, String direccion) {
        this.direccion = direccion;
        this.latitud = latitud;
        this.longitud = longitud;
        
    }



}
