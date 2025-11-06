package com.tallerwebi.presentacion.DTO;

import com.tallerwebi.dominio.Entity.Notificacion;
import java.time.LocalDateTime;

// Nota: Asegúrate de tener Lombok en el classpath o usa getters/setters explícitos
// para esta clase.
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionDTO {

    private Long id;
    private String mensaje;
    private String urlDestino;
    private LocalDateTime fechaCreacion;

    /**
     * Constructor que mapea los campos esenciales desde la entidad.
     * Esto previene LazyInitializationException.
     */
    public NotificacionDTO(Notificacion notificacion) {
        this.id = notificacion.getId();
        this.mensaje = notificacion.getMensaje();
        this.urlDestino = notificacion.getUrlDestino();
        this.fechaCreacion = notificacion.getFechaCreacion();

    }
}