package com.tallerwebi.dominio.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

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



    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Ubicacion ubicacion = (Ubicacion) o;
        return Objects.equals(latitud, ubicacion.latitud) && Objects.equals(longitud, ubicacion.longitud);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitud, longitud);
    }

}
