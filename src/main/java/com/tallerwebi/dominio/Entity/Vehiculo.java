package com.tallerwebi.dominio.Entity;

import com.tallerwebi.dominio.Enums.EstadoVerificacion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "vehiculo")
public class Vehiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String patente;
    private String modelo;
    private String anio;
    @Column(name = "asientos_totales")
    private Integer asientosTotales;
    @Column(name = "estado_verificacion")
    private EstadoVerificacion estadoVerificacion;

    @ManyToOne
    @JoinColumn(name = "conductor_id")
    private Conductor conductor;




}
