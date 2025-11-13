package com.tallerwebi.dominio.Enums;

public enum EstadoPago {

    NO_PAGADO,     // El viajero aún no ha realizado el pago
    PAGADO,    // El viajero ha completado el pago
    // Futuros estados para integración con MercadoPago:
    // PENDIENTE, EN_PROCESO, ERROR, REEMBOLSADO, etc.
    REEMBOLSO_PENDIENTE
}
