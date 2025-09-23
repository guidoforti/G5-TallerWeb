package com.tallerwebi.dominio.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

//@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Viajero {

   /* @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) */
    private Long id;


    private List<Viaje> viajes;

}
