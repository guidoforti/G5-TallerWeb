package com.tallerwebi.presentacion.DTO;

import com.tallerwebi.dominio.Entity.Ubicacion;
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


    public UbicacionDTO (Ubicacion ubicacion) {
        this.direccion = ubicacion.getDireccion();
        this.latitud = ubicacion.getLatitud();
        this.longitud = ubicacion.getLongitud();
    }


    public  Ubicacion toEntity () {
        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setDireccion(this.getDireccion());
        ubicacion.setLongitud(this.getLongitud());
        ubicacion.setLatitud(this.getLatitud());
        return  ubicacion;
    }
}
