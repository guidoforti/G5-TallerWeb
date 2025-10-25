package com.tallerwebi.dominio.excepcion;

public class BadRequestException extends Exception {
    public BadRequestException(String message) {
        super(message);
    }
}
