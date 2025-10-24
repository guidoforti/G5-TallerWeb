package com.tallerwebi.presentacion.DTO;


import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class DatosLoginDTO {

    private String email;
    private String password;


}