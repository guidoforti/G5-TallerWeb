package com.tallerwebi.presentacion.DTO.InputsDTO;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Viajero;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class RegistroInputDTO {

    // Campos Comunes
    private String nombre;
    private String email;
    private String contrasenia;

    // Campo Clave para la Bifurcación de Lógica
    private String rolSeleccionado; // Valores esperados: "CONDUCTOR" o "VIAJERO"

    // Campos Específicos de Conductor
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaDeVencimientoLicencia;

    // Campos Específicos de Viajero
    private Integer edad;
    private Boolean fumador;
    private String discapacitado;
    private Double promedioValoraciones;

    // --- Métodos de Conversión a Entidad ---

    public Conductor toConductorEntity() {
        Conductor conductor = new Conductor();
        conductor.setNombre(this.nombre);
        conductor.setEmail(this.email);
        conductor.setContrasenia(this.contrasenia);
        conductor.setFechaDeVencimientoLicencia(this.fechaDeVencimientoLicencia);
        // NOTA: El ROL y ACTIVO se asignan en el Servicio.
        return conductor;
    }

    public Viajero toViajeroEntity() {
        Viajero viajero = new Viajero();
        viajero.setNombre(this.nombre);
        viajero.setEmail(this.email);
        viajero.setContrasenia(this.contrasenia);
        viajero.setEdad(this.edad);
        viajero.setDiscapacitado(discapacitado);
        viajero.setFumador(fumador);
        // NOTA: El ROL y ACTIVO se asignan en el Servicio.
        return viajero;
    }
}