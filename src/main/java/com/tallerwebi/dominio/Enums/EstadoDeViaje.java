package com.tallerwebi.dominio.Enums;

public enum EstadoDeViaje {

    DISPONIBLE,//se puede sumar viajeros a este viaje
    COMPLETO, //cupo lleno
    FINALIZADO,//el viaje ya se realizo
    CANCELADO //el viaje fue cancelado por el owner
}
