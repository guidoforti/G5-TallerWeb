package com.tallerwebi.dominio.IServicio;

import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.excepcion.UsuarioNoAutorizadoException;
import com.tallerwebi.dominio.excepcion.ViajeNoCancelableException;
import com.tallerwebi.dominio.excepcion.ViajeNoEncontradoException;
import com.tallerwebi.dominio.excepcion.*;
import java.util.List;

public interface ServicioViaje {

    Viaje obtenerViajePorId(Long id);
    void publicarViaje(Viaje viaje, Long conductorId, Long vehiculoId) throws UsuarioInexistente, NotFoundException,
            UsuarioNoAutorizadoException, AsientosDisponiblesMayorQueTotalesDelVehiculoException, DatoObligatorioException;
            
    void cancelarViaje (Long id, Usuario usuarioEnSesion) throws ViajeNoEncontradoException, UsuarioNoAutorizadoException, ViajeNoCancelableException;
    List<Viaje> listarViajesPorConductor(Usuario usuarioEnSesion) throws UsuarioNoAutorizadoException;
}