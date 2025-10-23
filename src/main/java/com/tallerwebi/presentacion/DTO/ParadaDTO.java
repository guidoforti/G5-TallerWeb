package com.tallerwebi.presentacion.DTO;

import com.tallerwebi.dominio.Entity.Parada;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ParadaDTO {

    private CiudadDTO ciudadDTO;

    public ParadaDTO (Parada parada) {
        this.ciudadDTO =  new CiudadDTO(parada.getCiudad());
    }
}
