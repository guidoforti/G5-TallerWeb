package com.tallerwebi.dominio;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Enums.EstadoDeViaje;
import com.tallerwebi.dominio.IRepository.RepositorioConductor;
import com.tallerwebi.dominio.IRepository.ViajeRepository;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import com.tallerwebi.dominio.IServicio.ServicioViaje;
import com.tallerwebi.dominio.ServiceImpl.ServicioConductorImpl;
import com.tallerwebi.dominio.ServiceImpl.ServicioViajeImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


import com.tallerwebi.dominio.Entity.Vehiculo;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Enums.EstadoDeViaje;
import com.tallerwebi.dominio.Enums.EstadoVerificacion;
import com.tallerwebi.dominio.IRepository.ViajeRepository;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import com.tallerwebi.dominio.IServicio.ServicioVehiculo;
import com.tallerwebi.dominio.IServicio.ServicioViaje;
import com.tallerwebi.dominio.ServiceImpl.ServicioViajeImpl;
import com.tallerwebi.dominio.excepcion.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ServicioViajeTest {

    private ViajeRepository viajeRepositoryMock;
    private ServicioConductor servicioConductorMock;
    private ServicioVehiculo servicioVehiculoMock;
    private ServicioViaje servicioViaje;

    @BeforeEach
    void setUp() {
        viajeRepositoryMock = mock(ViajeRepository.class);
        servicioConductorMock = mock(ServicioConductor.class);
        servicioVehiculoMock = mock(ServicioVehiculo.class);
        servicioViaje = new ServicioViajeImpl(viajeRepositoryMock, servicioConductorMock, servicioVehiculoMock);
    }

    private Viaje crearViajeDeTest() {
        Viaje viaje = new Viaje();
        viaje.setFechaHoraDeSalida(LocalDateTime.now().plusDays(1));
        viaje.setPrecio(1500.0);
        viaje.setAsientosDisponibles(3);
        viaje.setFechaDeCreacion(LocalDateTime.now());
        viaje.setEstado(EstadoDeViaje.DISPONIBLE);
        viaje.setViajeros(new ArrayList<>());
        viaje.setParadas(new ArrayList<>());
        viaje.setOrigen(null);
        viaje.setDestino(null);
        return viaje;
    }

    @Test
    void deberiaPublicarViajeCorrectamente() throws Exception {
        // given
        Long conductorId = 1L;
        Long vehiculoId = 1L;

        Conductor conductor = new Conductor(conductorId, "Juan", "juan@test.com", "pass", LocalDate.now().plusDays(30), new ArrayList<>(), new ArrayList<>());
        Vehiculo vehiculo = new Vehiculo(vehiculoId, "ABC123", "Toyota Corolla", "2020", 5, EstadoVerificacion.VERIFICADO, conductor);
        Viaje viaje = crearViajeDeTest();

        when(servicioConductorMock.obtenerConductor(conductorId)).thenReturn(conductor);
        when(servicioVehiculoMock.getById(vehiculoId)).thenReturn(vehiculo);

        // when
        servicioViaje.publicarViaje(viaje, conductorId, vehiculoId);

        // then
        ArgumentCaptor<Viaje> viajeCaptor = ArgumentCaptor.forClass(Viaje.class);
        verify(viajeRepositoryMock).guardarViaje(viajeCaptor.capture());

        Viaje viajeGuardado = viajeCaptor.getValue();
        assertThat(viajeGuardado.getConductor(), equalTo(conductor));
        assertThat(viajeGuardado.getVehiculo(), equalTo(vehiculo));
        assertThat(viajeGuardado.getAsientosDisponibles(), equalTo(3));
        assertThat(viajeGuardado.getPrecio(), equalTo(1500.0));
        assertThat(viajeGuardado.getEstado(), equalTo(EstadoDeViaje.DISPONIBLE));
        assertThat(viajeGuardado.getFechaDeCreacion(), notNullValue());
        assertThat(viajeGuardado.getViajeros(), empty());
        assertThat(viajeGuardado.getParadas(), empty());
    }

    @Test
    void noDeberiaPublicarSiConductorNoExiste() throws Exception {
        // given
        Viaje viaje = crearViajeDeTest();
        when(servicioConductorMock.obtenerConductor(999L)).thenThrow(new UsuarioInexistente("Conductor no encontrado"));

        // when & then
        assertThrows(UsuarioInexistente.class, () -> servicioViaje.publicarViaje(viaje, 999L, 1L));
        verify(viajeRepositoryMock, never()).guardarViaje(any());
    }

    @Test
    void noDeberiaPublicarSiVehiculoNoExiste() throws Exception {
        // given
        Long conductorId = 1L;
        Conductor conductor = new Conductor(conductorId, "Juan", "juan@test.com", "pass", LocalDate.now().plusDays(30), new ArrayList<>(), new ArrayList<>());
        Viaje viaje = crearViajeDeTest();

        when(servicioConductorMock.obtenerConductor(conductorId)).thenReturn(conductor);
        when(servicioVehiculoMock.getById(999L)).thenThrow(new NotFoundException("Vehículo no encontrado"));

        // when & then
        assertThrows(NotFoundException.class, () -> servicioViaje.publicarViaje(viaje, conductorId, 999L));
        verify(viajeRepositoryMock, never()).guardarViaje(any());
    }

    @Test
    void noDeberiaPublicarSiVehiculoNoPerteneceConductor() throws Exception {
        // given
        Long conductorId = 1L;
        Long otroConductorId = 2L;

        Conductor conductor = new Conductor(conductorId, "Juan", "juan@test.com", "pass", LocalDate.now().plusDays(30), new ArrayList<>(), new ArrayList<>());
        Conductor otroConductor = new Conductor(otroConductorId, "Pedro", "pedro@test.com", "pass", LocalDate.now().plusDays(30), new ArrayList<>(), new ArrayList<>());
        Vehiculo vehiculo = new Vehiculo(1L, "ABC123", "Toyota Corolla", "2020", 5, EstadoVerificacion.VERIFICADO, otroConductor);
        Viaje viaje = crearViajeDeTest();

        when(servicioConductorMock.obtenerConductor(conductorId)).thenReturn(conductor);
        when(servicioVehiculoMock.getById(1L)).thenReturn(vehiculo);

        // when & then
        UsuarioNoAutorizadoException exception = assertThrows(
            UsuarioNoAutorizadoException.class,
            () -> servicioViaje.publicarViaje(viaje, conductorId, 1L)
        );

        assertThat(exception.getMessage(), equalTo("El vehículo seleccionado no pertenece al conductor"));
        verify(viajeRepositoryMock, never()).guardarViaje(any());
    }

    @Test
    void noDeberiaPublicarSiAsientosDisponiblesMayorQueTotalesMenosUno() throws Exception {
        // given
        Long conductorId = 1L;
        Conductor conductor = new Conductor(conductorId, "Juan", "juan@test.com", "pass", LocalDate.now().plusDays(30), new ArrayList<>(), new ArrayList<>());
        Vehiculo vehiculo = new Vehiculo(1L, "ABC123", "Toyota Corolla", "2020", 5, EstadoVerificacion.VERIFICADO, conductor);

        Viaje viaje = crearViajeDeTest();
        viaje.setAsientosDisponibles(5); // 5 asientos totales - 1 conductor = 4 máximo

        when(servicioConductorMock.obtenerConductor(conductorId)).thenReturn(conductor);
        when(servicioVehiculoMock.getById(1L)).thenReturn(vehiculo);

        // when & then
        AsientosDisponiblesMayorQueTotalesDelVehiculoException exception = assertThrows(
            AsientosDisponiblesMayorQueTotalesDelVehiculoException.class,
            () -> servicioViaje.publicarViaje(viaje, conductorId, 1L)
        );

        assertThat(exception.getMessage(), containsString("Los asientos disponibles no pueden ser mayores a 4"));
        verify(viajeRepositoryMock, never()).guardarViaje(any());
    }

    @Test
    void noDeberiaPublicarSiAsientosDisponiblesEsCero() {
        // given
        Viaje viaje = crearViajeDeTest();
        viaje.setAsientosDisponibles(0);

        // when & then
        DatoObligatorioException exception = assertThrows(
            DatoObligatorioException.class,
            () -> servicioViaje.publicarViaje(viaje, 1L, 1L)
        );

        assertThat(exception.getMessage(), equalTo("Los asientos disponibles deben ser mayor a 0"));
        verify(viajeRepositoryMock, never()).guardarViaje(any());
    }

    @Test
    void noDeberiaPublicarSiPrecioEsCeroONegativo() {
        // given
        Viaje viaje = crearViajeDeTest();
        viaje.setPrecio(0.0);

        // when & then
        DatoObligatorioException exception = assertThrows(
            DatoObligatorioException.class,
            () -> servicioViaje.publicarViaje(viaje, 1L, 1L)
        );

        assertThat(exception.getMessage(), equalTo("El precio debe ser mayor a 0"));
        verify(viajeRepositoryMock, never()).guardarViaje(any());
    }

    @Test
    void noDeberiaPublicarSiFechaEsNull() {
        // given
        Viaje viaje = crearViajeDeTest();
        viaje.setFechaHoraDeSalida(null);

        // when & then
        DatoObligatorioException exception = assertThrows(
            DatoObligatorioException.class,
            () -> servicioViaje.publicarViaje(viaje, 1L, 1L)
        );

        assertThat(exception.getMessage(), equalTo("La fecha y hora de salida es obligatoria"));
        verify(viajeRepositoryMock, never()).guardarViaje(any());
    }

    @Test
    void noDeberiaPublicarSiFechaEsAnteriorAHoy() {
        // given
        Viaje viaje = crearViajeDeTest();
        viaje.setFechaHoraDeSalida(LocalDateTime.now().minusDays(1)); // Fecha pasada

        // when & then
        DatoObligatorioException exception = assertThrows(
            DatoObligatorioException.class,
            () -> servicioViaje.publicarViaje(viaje, 1L, 1L)
        );

        assertThat(exception.getMessage(), equalTo("La fecha y hora de salida debe ser mayor a la fecha actual"));
        verify(viajeRepositoryMock, never()).guardarViaje(any());
    }

    @Test
    void noDeberiaPublicarSiConductorIdEsNull() {
        // given
        Viaje viaje = crearViajeDeTest();

        // when & then
        DatoObligatorioException exception = assertThrows(
            DatoObligatorioException.class,
            () -> servicioViaje.publicarViaje(viaje, null, 1L)
        );

        assertThat(exception.getMessage(), equalTo("El ID del conductor es obligatorio"));
        verify(viajeRepositoryMock, never()).guardarViaje(any());
    }

    @Test
    void noDeberiaPublicarSiVehiculoIdEsNull() {
        // given
        Viaje viaje = crearViajeDeTest();

        // when & then
        DatoObligatorioException exception = assertThrows(
            DatoObligatorioException.class,
            () -> servicioViaje.publicarViaje(viaje, 1L, null)
        );

        assertThat(exception.getMessage(), equalTo("El vehículo es obligatorio"));
        verify(viajeRepositoryMock, never()).guardarViaje(any());
    }

    @Test
    void deberiaSetearEstadoComoDisponible() throws Exception {
        // given
        Long conductorId = 1L;
        Long vehiculoId = 1L;

        Conductor conductor = new Conductor(conductorId, "Juan", "juan@test.com", "pass", LocalDate.now().plusDays(30), new ArrayList<>(), new ArrayList<>());
        Vehiculo vehiculo = new Vehiculo(vehiculoId, "ABC123", "Toyota Corolla", "2020", 5, EstadoVerificacion.VERIFICADO, conductor);
        Viaje viaje = crearViajeDeTest();

        when(servicioConductorMock.obtenerConductor(conductorId)).thenReturn(conductor);
        when(servicioVehiculoMock.getById(vehiculoId)).thenReturn(vehiculo);

        // when
        servicioViaje.publicarViaje(viaje, conductorId, vehiculoId);

        // then
        ArgumentCaptor<Viaje> viajeCaptor = ArgumentCaptor.forClass(Viaje.class);
        verify(viajeRepositoryMock).guardarViaje(viajeCaptor.capture());

        Viaje viajeGuardado = viajeCaptor.getValue();
        assertThat(viajeGuardado.getEstado(), equalTo(EstadoDeViaje.DISPONIBLE));
    }

    @Test
        void seDebeCancelarViajeCorrectamente() throws Exception {
        
        Conductor conductor = new Conductor();
        conductor.setId(1L);
        conductor.setEmail("pepito@gmail.com");
        
        Usuario usuarioEnSesion = new Usuario();
        usuarioEnSesion.setId(1L);
        usuarioEnSesion.setRol("CONDUCTOR");

        Viaje viaje = new Viaje();
        viaje.setId(100L);
        viaje.setConductor(conductor);
        viaje.setEstado(EstadoDeViaje.DISPONIBLE);

        when(viajeRepositoryMock.findById(100L)).thenReturn(viaje);

        servicioViaje.cancelarViaje(100L, usuarioEnSesion);

        assertEquals(EstadoDeViaje.CANCELADO, viaje.getEstado());
        verify(viajeRepositoryMock).modificarViaje(viaje);
    }


    @Test
        void noDebeCancelarSiUnUsuarioNoTieneRol() {
        
        Usuario usuarioSinRol = new Usuario();
        usuarioSinRol.setId(2L);
        usuarioSinRol.setRol(null);

        assertThrows(UsuarioNoAutorizadoException.class,
                () -> servicioViaje.cancelarViaje(100L, usuarioSinRol));

        verify(viajeRepositoryMock, never()).modificarViaje(any());
    }

    @Test
        void noDebeCancelarSiUnViajeNoExiste() {

        Usuario usuarioConductor = new Usuario();
        usuarioConductor.setId(1L);
        usuarioConductor.setRol("CONDUCTOR");

        when(viajeRepositoryMock.findById(999L)).thenReturn(null);

        assertThrows(ViajeNoEncontradoException.class,
                () -> servicioViaje.cancelarViaje(999L, usuarioConductor));

        verify(viajeRepositoryMock, never()).modificarViaje(any());
    }

    @Test
        void noSeDebeCancelarSiUnViajeNoPerteneceAlConductor() {
        
        Usuario usuarioConductor = new Usuario();
        usuarioConductor.setId(1L);
        usuarioConductor.setRol("CONDUCTOR");

        // el viaje pertenece a otro conductor
        Conductor otroConductor = new Conductor();
        otroConductor.setId(99L);

        Viaje viaje = new Viaje();
        viaje.setId(100L);
        viaje.setConductor(otroConductor);
        viaje.setEstado(EstadoDeViaje.DISPONIBLE);

        when(viajeRepositoryMock.findById(100L)).thenReturn(viaje);

        assertThrows(UsuarioNoAutorizadoException.class,
                () -> servicioViaje.cancelarViaje(100L, usuarioConductor));

        verify(viajeRepositoryMock, never()).modificarViaje(any());
    }

    @Test
        void noSeDebeCancelarSiElEstadoDelViajeEsFinalizado() {
    
        Usuario usuarioConductor = new Usuario();
        usuarioConductor.setId(1L);
        usuarioConductor.setRol("CONDUCTOR");

        Conductor conductor = new Conductor();
        conductor.setId(1L);

        Viaje viaje = new Viaje();
        viaje.setId(100L);
        viaje.setConductor(conductor);
        //el viaje se finalizo
        viaje.setEstado(EstadoDeViaje.FINALIZADO);

        when(viajeRepositoryMock.findById(100L)).thenReturn(viaje);

        assertThrows(ViajeNoCancelableException.class,
                () -> servicioViaje.cancelarViaje(100L, usuarioConductor));

        verify(viajeRepositoryMock, never()).modificarViaje(any());
    }

    @Test
        void noSeDebeCancelarSiElEstadoDelViajeEsCancelado() {

        Usuario usuarioConductor = new Usuario();
        usuarioConductor.setId(1L);
        usuarioConductor.setRol("CONDUCTOR");

        Conductor conductor = new Conductor();
        conductor.setId(1L);

        Viaje viaje = new Viaje();
        viaje.setId(100L);
        viaje.setConductor(conductor);
        //viaje cancelado
        viaje.setEstado(EstadoDeViaje.CANCELADO);

        when(viajeRepositoryMock.findById(100L)).thenReturn(viaje);

        assertThrows(ViajeNoCancelableException.class,
                () -> servicioViaje.cancelarViaje(100L, usuarioConductor));

        verify(viajeRepositoryMock, never()).modificarViaje(any());
    }

    @Test
        void noDebeListarViajesSiUsuarioNoEsConductor() {
    
    Usuario usuarioSinRol = new Usuario();
    usuarioSinRol.setId(1L);
    usuarioSinRol.setRol("PASAJERO"); // rol incorrecto

    
    assertThrows(UsuarioNoAutorizadoException.class,
        () -> servicioViaje.listarViajesPorConductor(usuarioSinRol));

    }


    @Test
        void noDebeListarViajesSiUsuarioEsNull() {
    
        assertThrows(UsuarioNoAutorizadoException.class,
        () -> servicioViaje.listarViajesPorConductor(null));
    }


    @Test
        void deberiaListarViajesSiUsuarioEsConductor() throws UsuarioNoAutorizadoException {
    
        Usuario usuarioConductor = new Usuario();
        usuarioConductor.setId(1L);
        usuarioConductor.setRol("CONDUCTOR");

        Viaje viaje1 = new Viaje();
        viaje1.setId(10L);
        Viaje viaje2 = new Viaje();
        viaje2.setId(20L);

        List<Viaje> viajesMock = List.of(viaje1, viaje2);

        when(viajeRepositoryMock.findByConductorId(1L)).thenReturn(viajesMock);

        List<Viaje> resultado = servicioViaje.listarViajesPorConductor(usuarioConductor);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(viajeRepositoryMock).findByConductorId(1L);
    }
}