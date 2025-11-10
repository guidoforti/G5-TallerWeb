package com.tallerwebi.presentacion.DTO.InputsDTO;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.Entity.Viajero;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class RegistroInputDTO {

    private String nombre;
    private String email;
    private String contrasenia;
    private String rolSeleccionado;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaNacimiento;

    private Boolean fumador;
    private String discapacitado;


    private String fotoPerfilUrl;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaDeVencimientoLicencia;


    private <T extends Usuario> T setCommonUserFields(T usuario) {
        usuario.setNombre(this.nombre);
        usuario.setEmail(this.email);
        usuario.setContrasenia(this.contrasenia);
        usuario.setFechaNacimiento(this.fechaNacimiento);
        usuario.setDiscapacitado(this.discapacitado);
        usuario.setFumador(this.fumador != null ? this.fumador : false);
        return usuario;
    }

    public Conductor toConductorEntity() {
        Conductor conductor = setCommonUserFields(new Conductor());
        conductor.setFechaDeVencimientoLicencia(this.fechaDeVencimientoLicencia);
        conductor.setFotoPerfilUrl(this.fotoPerfilUrl);
        return conductor;
    }

    public Viajero toViajeroEntity() {
        Viajero viajero = setCommonUserFields(new Viajero());
        viajero.setFotoPerfilUrl(this.fotoPerfilUrl);
        return viajero;
    }
}