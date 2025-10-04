package com.tallerwebi.dominio.excepcion;

public class ViajeNoCancelableException extends Exception{
    public ViajeNoCancelableException(String mensaje){
        super(mensaje);
    }
}
