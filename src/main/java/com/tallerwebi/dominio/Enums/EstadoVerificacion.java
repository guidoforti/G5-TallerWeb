package com.tallerwebi.dominio.Enums;

public enum EstadoVerificacion {

    PENDIENTE,    // El conductor o el viaje fue creado, pero aún no se validó la documentación
    EN_REVISION,  // El equipo de soporte está revisando la documentación enviada
    RECHAZADO,    // La verificación fue denegada (documentación inválida, vencida o inconsistente)
    VERIFICADO    // El estado final: el usuario o vehículo está validado y habilitado para publicar viajes

}
