package com.tallerwebi.dominio.excepcion;

public class UsuarioNoAutorizadoException extends Exception {
    public UsuarioNoAutorizadoException(String message) {
        super(message);
    }
}
