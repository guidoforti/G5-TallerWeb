package com.tallerwebi.dominio.Entity;

import com.tallerwebi.dominio.Enums.EstadoDeViaje;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Viaje {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "conductor_id")
    private Conductor conductor;

    @ManyToOne
    @JoinColumn(name = "vehiculo_id")
    private Vehiculo vehiculo;

    @ManyToMany
    @JoinTable(
            name = "viaje_viajero", // Nombre de la tabla intermedia
            joinColumns = @JoinColumn(name = "viaje_id"), // Columna que referencia a Viaje
            inverseJoinColumns = @JoinColumn(name = "viajero_id") // Columna que referencia a Viajero
    )
    private List<Viajero> viajeros;


    // Lista de paradas
    @OneToMany(mappedBy = "viaje", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<Parada> paradas;

    @ManyToOne
    @JoinColumn(name = "origen_id")
    private Ciudad origen;

    @ManyToOne
    @JoinColumn(name = "destino_id")
    private Ciudad destino;


    private LocalDateTime fechaHoraDeSalida;
    private Double precio;
    private Integer asientosDisponibles;
    private LocalDateTime fechaDeCreacion;
    private EstadoDeViaje estado;


}
