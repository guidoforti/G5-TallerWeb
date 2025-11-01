package com.tallerwebi.dominio.Enums;

public enum EstadoVerificacion {

    PENDIENTE,    // El conductor o el viaje fue creado, pero aún no se validó la documentación
    VERIFICADO,// El estado final: el usuario o vehículo está validado y habilitado para publicar viajes
    NO_CARGADO,
    DESACTIVADO
}
