package com.tallerwebi.dominio.IServicio;

import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.excepcion.UsuarioNoAutorizadoException;
import com.tallerwebi.dominio.excepcion.ViajeNoCancelableException;
import com.tallerwebi.dominio.excepcion.ViajeNoEncontradoException;
import com.tallerwebi.presentacion.DTO.InputsDTO.ViajeInputDTO;

public interface ServicioViaje {

    Viaje obtenerViajePorId (Long id);
    void publicarViaje (ViajeInputDTO viajeInputDTO);
    void cancelarViaje (Long id, Usuario usuarioEnSesion) throws ViajeNoEncontradoException, UsuarioNoAutorizadoException, ViajeNoCancelableException;
}
