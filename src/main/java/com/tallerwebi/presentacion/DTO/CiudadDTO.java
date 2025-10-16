package com.tallerwebi.presentacion.DTO;

import com.tallerwebi.dominio.Entity.Ciudad;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CiudadDTO {

    private String nombre;
    private Float latitud;
    private Float longitud;


    public CiudadDTO(Ciudad ciudad) {
        this.nombre = ciudad.getNombre();
        this.latitud = ciudad.getLatitud();
        this.longitud = ciudad.getLongitud();
    }


    public Ciudad toEntity () {
        Ciudad ciudad = new Ciudad();
        ciudad.setNombre(this.getNombre());
        ciudad.setLongitud(this.getLongitud());
        ciudad.setLatitud(this.getLatitud());
        return ciudad;
    }
}
