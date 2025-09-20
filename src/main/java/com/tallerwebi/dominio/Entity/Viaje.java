package com.tallerwebi.dominio.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
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
    private List<Viajero> viajeros;

   /* @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "direccion", column = @Column(name = "origen_direccion")),
            @AttributeOverride(name = "ciudad", column = @Column(name = "origen_ciudad")),
            @AttributeOverride(name = "provincia", column = @Column(name = "origen_provincia")),
            @AttributeOverride(name = "pais", column = @Column(name = "origen_pais")),
            @AttributeOverride(name = "latitud", column = @Column(name = "origen_latitud")),
            @AttributeOverride(name = "longitud", column = @Column(name = "origen_longitud"))
    }) */
    private Ubicacion origen;

   /* @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "direccion", column = @Column(name = "destino_direccion")),
            @AttributeOverride(name = "ciudad", column = @Column(name = "destino_ciudad")),
            @AttributeOverride(name = "provincia", column = @Column(name = "destino_provincia")),
            @AttributeOverride(name = "pais", column = @Column(name = "destino_pais")),
            @AttributeOverride(name = "latitud", column = @Column(name = "destino_latitud")),
            @AttributeOverride(name = "longitud", column = @Column(name = "destino_longitud"))
    })*/
    private Ubicacion destino;


    private List<Ubicacion> paradas;
    private LocalDateTime fechaHoraDeSalida;
    private Double precio;
    private Integer asientosTotales;
    private Integer asientosDisponibles;
    private LocalDateTime fechaDeCreacion;
    private Vehiculo vehiculo;


}
