package com.tallerwebi.dominio.IServicio;

import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.excepcion.CredencialesInvalidas;
import com.tallerwebi.dominio.excepcion.DatoObligatorioException;
import com.tallerwebi.dominio.excepcion.EdadInvalidaException;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;

public interface ServicioViajero {

    Viajero registrar(Viajero nuevoViajero) throws UsuarioExistente, EdadInvalidaException ,DatoObligatorioException;
    Viajero obtenerViajero(Long viajeroId) throws UsuarioInexistente;

}
