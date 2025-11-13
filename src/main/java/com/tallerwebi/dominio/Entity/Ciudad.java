package com.tallerwebi.dominio.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ciudad", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"latitud", "longitud"})
})
public class Ciudad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
   /* private String ciudad;
    private String provincia;
    private String pais;*/
    private float latitud;
    private float longitud;



    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Ciudad ciudad = (Ciudad) o;
        return Objects.equals(latitud, ciudad.latitud) && Objects.equals(longitud, ciudad.longitud);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitud, longitud);
    }

    @Override
    public String toString(){
        return this.nombre;
    }
}
