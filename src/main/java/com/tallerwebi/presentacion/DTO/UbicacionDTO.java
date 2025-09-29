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
public class UbicacionDTO {

    private String direccion;
    private Float latitud;
    private Float longitud;


    public UbicacionDTO (Ciudad ciudad) {
        this.direccion = ciudad.getDireccion();
        this.latitud = ciudad.getLatitud();
        this.longitud = ciudad.getLongitud();
    }


    public Ciudad toEntity () {
        Ciudad ciudad = new Ciudad();
        ciudad.setDireccion(this.getDireccion());
        ciudad.setLongitud(this.getLongitud());
        ciudad.setLatitud(this.getLatitud());
        return ciudad;
    }
}
