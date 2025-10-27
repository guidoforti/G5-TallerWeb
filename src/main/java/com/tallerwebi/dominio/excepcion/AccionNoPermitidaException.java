package com.tallerwebi.dominio.excepcion;

public class AccionNoPermitidaException extends Exception {
    public AccionNoPermitidaException(String mensaje) {
        super(mensaje);
    }
}
