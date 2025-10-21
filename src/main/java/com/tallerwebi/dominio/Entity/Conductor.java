package com.tallerwebi.dominio.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDate;
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

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaDeVencimientoLicencia;
    @OneToMany(mappedBy = "conductor")
    private List<Viaje> viajes;
    @OneToMany(mappedBy = "conductor", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Vehiculo> vehiculos;

}
