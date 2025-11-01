package com.tallerwebi.dominio.Enums;

/**
 * Tipos de violaciones que puede cometer un conductor
 */
public enum TipoViolacion {

    RETRASO_LEVE(1),        // 10-15 min tarde al iniciar
    RETRASO_GRAVE(2),       // 15+ min tarde (auto-cancelado)
    OLVIDO_CIERRE(1),       // Olvidó marcar fin de viaje (registrado solo después de 3 olvidos)
    NO_SHOW(2);             // No presentarse a iniciar el viaje

    private final int pesoStrike;

    TipoViolacion(int peso) {
        this.pesoStrike = peso;
    }

    public int getPesoStrike() {
        return pesoStrike;
    }
}
