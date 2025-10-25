package com.tallerwebi.dominio.Enums;

public enum EstadoReserva {

    PENDIENTE,          // La reserva está pendiente de aprobación por el conductor
    CONFIRMADA,         // La reserva fue confirmada por el conductor
    RECHAZADA,          // La reserva fue rechazada por el conductor
    CANCELADA_POR_VIAJERO  // La reserva fue cancelada por el viajero
}
