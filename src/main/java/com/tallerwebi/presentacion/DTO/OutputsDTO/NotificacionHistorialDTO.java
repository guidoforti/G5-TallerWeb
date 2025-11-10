package com.tallerwebi.presentacion.DTO.OutputsDTO;

import com.tallerwebi.dominio.Entity.Notificacion;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.format.DateTimeFormatter;

@Getter @Setter @NoArgsConstructor
public class NotificacionHistorialDTO {

    private Long id;
    private String mensaje;
    private String urlDestino;
    private String tipo;
    private String fechaCreacion; // Formato legible para la vista
    private boolean leida;

    // Regla: Constructor desde la entidad con lógica de formateo
    public NotificacionHistorialDTO(Notificacion notificacion) {
        this.id = notificacion.getId();
        this.mensaje = notificacion.getMensaje();
        this.urlDestino = notificacion.getUrlDestino();
        this.tipo = notificacion.getTipo().name(); // Obtener el nombre del Enum
        this.leida = notificacion.getLeida();

        // Formato de fecha para el frontend
        if (notificacion.getFechaCreacion() != null) {
            this.fechaCreacion = notificacion.getFechaCreacion()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        } else {
            this.fechaCreacion = "Fecha Desconocida";
        }
    }
}