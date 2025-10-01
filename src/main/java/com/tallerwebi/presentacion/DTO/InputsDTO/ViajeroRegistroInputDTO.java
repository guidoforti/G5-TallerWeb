package com.tallerwebi.presentacion.DTO.InputsDTO;

import com.tallerwebi.dominio.Entity.Viajero;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ViajeroRegistroInputDTO {

    private String nombre;
    private Integer edad;
    private String email;
    private String contrasenia;

    public Viajero toEntity(){

        Viajero viajero = new Viajero();
        viajero.setNombre(nombre);
        viajero.setEdad(edad);
        viajero.setEmail(email);
        viajero.setContrasenia(contrasenia);

        return viajero;
    }
}
