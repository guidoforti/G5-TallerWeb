package com.tallerwebi.dominio.excepcion;

public class UsuarioInexistente extends Exception {
    public UsuarioInexistente(String message) {
        super(message);
    }
}
