package com.tallerwebi.dominio.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "conductor")
@PrimaryKeyJoinColumn(name = "usuario_id")
public class Conductor extends Usuario {

    // @Column(name = "foto_perfil_conductor")
    private String fotoDePerfil;

    @Column(name = "fecha_de_vencimiento_licencia")
    private LocalDate fechaDeVencimientoLicencia;
    @OneToMany(mappedBy = "conductor")
    private List<Viaje> viajes;
    @OneToMany(mappedBy = "conductor", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Vehiculo> vehiculos;

    /**
     * Campos reservados para futuro sistema de reputación basado en reviews.
     * Estos campos actualmente no se utilizan, pero se mantienen en la estructura
     * para implementar un sistema de calificaciones y suspensiones basado en
     * reviews de viajeros en lugar del antiguo sistema de violaciones.
     */

    /**
     * Fecha hasta la cual el conductor está suspendido.
     * Null si no está suspendido.
     * Reservado para sistema de reputación futuro.
     */
    @Column(name = "suspendido_hasta")
    private LocalDateTime suspendidoHasta;

    /**
     * Indica si el conductor está activo en la plataforma.
     * Reservado para sistema de reputación futuro.
     */
    @Column(name = "activo")
    private Boolean activo = true;

    /**
     * Verifica si el conductor está actualmente suspendido.
     * Reservado para sistema de reputación futuro.
     * @return true si está suspendido, false en caso contrario
     */
    public boolean estaSuspendido() {
        if (suspendidoHasta == null || !activo) {
            return false;
        }
        return LocalDateTime.now().isBefore(suspendidoHasta);
    }

}
