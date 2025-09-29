package com.tallerwebi.dominio.IServicio;

import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.excepcion.CredencialesInvalidas;
import com.tallerwebi.dominio.excepcion.EdadInvalida;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;

public interface ServicioViajero {

    Viajero login(String nombreUsuario, String contrasenia) throws CredencialesInvalidas;
    Viajero registrar(Viajero nuevoViajero) throws UsuarioExistente, EdadInvalida;
    Viajero obtenerViajero(Long viajeroId) throws UsuarioInexistente;

}
