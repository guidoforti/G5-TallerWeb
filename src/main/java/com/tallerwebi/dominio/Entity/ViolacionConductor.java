package com.tallerwebi.dominio.Entity;

import com.tallerwebi.dominio.Enums.TipoViolacion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "violacion_conductor")
public class ViolacionConductor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conductor_id", nullable = false)
    private Conductor conductor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viaje_id")
    private Viaje viaje;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoViolacion tipo;

    @Column(name = "fecha_violacion", nullable = false)
    private LocalDateTime fechaViolacion;

    @Column(name = "minutos_retraso")
    private Integer minutosRetraso;

    @Column(nullable = false)
    private Boolean activa;

    @Column(name = "fecha_expiracion")
    private LocalDateTime fechaExpiracion;

    private String descripcion;
}
