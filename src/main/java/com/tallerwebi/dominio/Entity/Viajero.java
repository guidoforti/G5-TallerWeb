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

    private Integer edad;

    // Relación con reservas
    @OneToMany(mappedBy = "viajero", fetch = FetchType.LAZY)
    private List<Reserva> reservas = new ArrayList<>();

}
