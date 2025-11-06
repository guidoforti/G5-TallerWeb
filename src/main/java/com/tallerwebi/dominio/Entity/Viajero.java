package com.tallerwebi.dominio.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "viajero")
@PrimaryKeyJoinColumn(name = "usuario_id")
public class Viajero extends Usuario {

   // @Column(name = "edad_viajero")
    private Integer edad;

  //  @Column(name = "es_fumador")
    private Boolean fumador;

    //@Column(name = "discapacidad")
    private String discapacitado;

    // @Column(name = "foto_perfil_viajero")
    private String fotoPerfilUrl;

    // Relación con reservas
    @OneToMany(mappedBy = "viajero", fetch = FetchType.LAZY)
    private List<Reserva> reservas = new ArrayList<>();

    @OneToMany(mappedBy = "receptor", fetch = FetchType.LAZY)
    private List<Valoracion> valoracionesRecibidas = new ArrayList<>();
}
