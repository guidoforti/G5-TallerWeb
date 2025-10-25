package com.tallerwebi.dominio.Entity;

import lombok.*;
import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "parada")
public class Parada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer orden;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viaje_id", nullable = false)
    private Viaje viaje;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ciudad_id", nullable = false)
    private Ciudad ciudad;

    // Constructor sin el campo viaje para evitar referencias circulares
    public Parada(Ciudad ciudad, Integer orden) {
        this.ciudad = ciudad;
        this.orden = orden;
    }

    // Método seguro para establecer la relación con Viaje
    public void setViaje(Viaje viaje) {
        this.viaje = viaje;
    }
}
