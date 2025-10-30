package com.tallerwebi.dominio.IServicio;

import com.tallerwebi.dominio.Entity.Ciudad;
import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Parada;
import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.excepcion.*;
import java.time.LocalDateTime;
import java.util.List;

public interface ServicioViaje {

    Viaje obtenerViajePorId(Long id) throws ViajeNoEncontradoException, UsuarioNoAutorizadoException, NotFoundException;
    void publicarViaje(Viaje viaje, Long conductorId, Long vehiculoId) throws UsuarioInexistente, NotFoundException, UsuarioNoAutorizadoException, AsientosDisponiblesMayorQueTotalesDelVehiculoException, DatoObligatorioException, ViajeDuplicadoException;
    void cancelarViaje (Long id, Usuario usuarioEnSesion) throws ViajeNoEncontradoException, UsuarioNoAutorizadoException, ViajeNoCancelableException;
    List<Viaje> listarViajesPorConductor(Conductor conductor) throws UsuarioNoAutorizadoException;
    Viaje obtenerDetalleDeViaje(Long id) throws NotFoundException;

    void modificarViaje(Viaje viaje ,  List<Parada> paradas) throws BadRequestException;

    // En ServicioViaje.java
    Viaje obtenerViajeConParadas(Long id) throws NotFoundException;
    List<Viaje> buscarViajesDisponibles(Ciudad origen, Ciudad destino, LocalDateTime fechaSalida, Double precioMin, Double precioMax) throws DatoObligatorioException;
}
