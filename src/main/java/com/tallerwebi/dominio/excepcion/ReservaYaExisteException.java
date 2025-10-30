package com.tallerwebi.dominio.excepcion;

public class ReservaYaExisteException extends Exception {
    public ReservaYaExisteException(String mensaje) {
        super(mensaje);
    }
}
