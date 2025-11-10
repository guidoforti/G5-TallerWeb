package com.tallerwebi.presentacion.DTO.OutputsDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Este DTO viaja por el WebSocket
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class NotificacionOutputDTO {

    private String mensaje;
    private String urlDestino; // CLAVE: para la redirección al hacer click
    private Long idNotificacion; // CLAVE: para marcar como leída via AJAX/AJAX
}