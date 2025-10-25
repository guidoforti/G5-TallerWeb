package com.tallerwebi.dominio.Entity;

import com.tallerwebi.dominio.Enums.EstadoDeViaje;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "viaje")
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

    // Optimistic locking para concurrencia
    @Version
    private Long version;

    // Relación con reservas
    @OneToMany(mappedBy = "viaje", fetch = FetchType.LAZY)
    private List<Reserva> reservas = new ArrayList<>();

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

    @Column(name = "fecha_hora_de_salida")
    private LocalDateTime fechaHoraDeSalida;
    private Double precio;
    @Column(name = "asientos_disponibles")
    private Integer asientosDisponibles;
    @Column(name = "fecha_de_creacion")
    private LocalDateTime fechaDeCreacion;
    private EstadoDeViaje estado;


    public void agregarParada(Parada parada) {
        paradas.add(parada);
        parada.setViaje(this);
    }
}
