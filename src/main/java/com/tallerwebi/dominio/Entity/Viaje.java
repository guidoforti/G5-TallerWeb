package com.tallerwebi.dominio.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

//@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Viaje {

    private Long id;
    /*@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @ManyToOne */
    private Conductor conductor;
    /* @ManyToMany
     @JoinTable(
             name = "viaje_viajero", // Nombre de la tabla intermedia
             joinColumns = @JoinColumn(name = "viaje_id"), // Columna que referencia a Viaje
             inverseJoinColumns = @JoinColumn(name = "viajero_id") // Columna que referencia a Viajero
     ) */
    private List<Viajero> viajeros; /* @ManyToMany
    @JoinTable(
            name = "viaje_viajero", // Nombre de la tabla intermedia
            joinColumns = @JoinColumn(name = "viaje_id"), // Columna que referencia a Viaje
            inverseJoinColumns = @JoinColumn(name = "viajero_id") // Columna que referencia a Viajero
    ) */

    // agregar anotaciones para origen y destino
    private Ciudad origen;


    private Ciudad destino;


    private List<Ciudad> paradas;
    private LocalDateTime fechaHoraDeSalida;
    private Double precio;
    private Integer asientosDisponibles;
    private LocalDateTime fechaDeCreacion;
    private Vehiculo vehiculo;


}
