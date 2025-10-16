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
public class Conductor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String email;
    private String contrasenia;

    @Column(name = "fecha_de_vencimiento_licencia")
    private LocalDate fechaDeVencimientoLicencia;

    @OneToMany(mappedBy = "conductor")
    private List<Viaje> viajes;

    //ESTO PUEDE CONVERTIRSE EN UNA ENTITY LICENCIA A FUTURO

    @OneToMany(mappedBy = "conductor", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Vehiculo> vehiculos;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Conductor)) return false;
        Conductor that = (Conductor) o;
        return Objects.equals(email, that.email); // or your identity fields
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}
