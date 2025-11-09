package com.tallerwebi.dominio.IServicio;

import java.util.List;

import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.Entity.Valoracion;
import com.tallerwebi.dominio.excepcion.DatoObligatorioException;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.dominio.excepcion.ViajeNoEncontradoException;
import com.tallerwebi.presentacion.DTO.InputsDTO.ValoracionIndividualInputDTO;

public interface ServicioValoracion {
    void valorarUsuario(Usuario emisor, ValoracionIndividualInputDTO dto, Long viajeId)
        throws UsuarioInexistente, DatoObligatorioException;

    List<Valoracion> obtenerValoracionesDeUsuario(Long usuarioId);

    Double calcularPromedioValoraciones(Long usuarioId);

    Viajero obtenerViajero(Long viajeroId) throws UsuarioInexistente;

    Usuario obtenerUsuario(Long usuarioId) throws UsuarioInexistente;

    List<Viajero> obtenerViajeros(Long viajeId) throws ViajeNoEncontradoException;
}
