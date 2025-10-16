package com.tallerwebi.dominio.IServicio;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.excepcion.*;
import java.util.List;

import java.util.Optional;

public interface ServicioViaje {

    Viaje obtenerViajePorId(Long id) throws ViajeNoEncontradoException, UsuarioNoAutorizadoException, NotFoundException;
    void publicarViaje(Viaje viaje, Long conductorId, Long vehiculoId) throws UsuarioInexistente, NotFoundException, UsuarioNoAutorizadoException, AsientosDisponiblesMayorQueTotalesDelVehiculoException, DatoObligatorioException, ViajeDuplicadoException;
    void cancelarViaje (Long id, Usuario usuarioEnSesion) throws ViajeNoEncontradoException, UsuarioNoAutorizadoException, ViajeNoCancelableException;
    List<Viaje> listarViajesPorConductor(Conductor conductor) throws UsuarioNoAutorizadoException;
    Viaje obtenerDetalleDeViaje(Long id) throws NotFoundException;
}
