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

    @Column(name = "fecha_de_vencimiento_licencia")
    private LocalDate fechaDeVencimientoLicencia;
    @OneToMany(mappedBy = "conductor")
    private List<Viaje> viajes;
    @OneToMany(mappedBy = "conductor", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Vehiculo> vehiculos;

    // Campos para sistema de suspensiones
    @Column(name = "suspendido_hasta")
    private LocalDateTime suspendidoHasta;

    @Column(name = "activo")
    private Boolean activo = true;

    /**
     * Verifica si el conductor está actualmente suspendido
     */
    public boolean estaSuspendido() {
        if (suspendidoHasta == null || !activo) {
            return false;
        }
        return LocalDateTime.now().isBefore(suspendidoHasta);
    }

}
