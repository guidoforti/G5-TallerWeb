package com.tallerwebi.dominio;

import com.tallerwebi.dominio.Entity.*;
import com.tallerwebi.dominio.Entity.Ciudad;
import com.tallerwebi.dominio.Entity.Conductor;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


import org.hibernate.Hibernate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        Ciudad origen = new Ciudad(1L, "Buenos Aires", -34.6037f, -58.3816f);
        Ciudad destino = new Ciudad(2L, "Córdoba", -31.4201f, -64.1888f);

        Viaje viaje = new Viaje();
        viaje.setFechaHoraDeSalida(LocalDateTime.now().plusDays(1));
        viaje.setPrecio(1500.0);
        viaje.setAsientosDisponibles(3);
        viaje.setFechaDeCreacion(LocalDateTime.now());
        viaje.setEstado(EstadoDeViaje.DISPONIBLE);
        viaje.setViajeros(new ArrayList<>());
        viaje.setParadas(new ArrayList<>());
        viaje.setOrigen(origen);
        viaje.setDestino(destino);
        return viaje;
    }

    @Test
    void deberiaPublicarViajeCorrectamente() throws Exception {
        // given
        Long conductorId = 1L;
        Long vehiculoId = 1L;

        Conductor conductor = new Conductor();
        conductor.setId(conductorId);
        conductor.setNombre("Juan");
        conductor.setEmail("juan@test.com");
        conductor.setContrasenia("pass");
        conductor.setFechaDeVencimientoLicencia(LocalDate.now().plusDays(30));
        conductor.setRol("CONDUCTOR");
        conductor.setActivo(true);

        Vehiculo vehiculo = new Vehiculo(vehiculoId, "ABC123", "Toyota Corolla", "2020", 5, EstadoVerificacion.VERIFICADO, conductor);
        Viaje viaje = crearViajeDeTest();

        when(servicioConductorMock.obtenerConductor(conductorId)).thenReturn(conductor);
        when(servicioVehiculoMock.getById(vehiculoId)).thenReturn(vehiculo);
        when(viajeRepositoryMock.findByOrigenYDestinoYConductorYEstadoIn(any(), any(), any(), anyList())).thenReturn(Collections.emptyList());

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
        Conductor conductor = new Conductor();
        conductor.setId(conductorId);
        conductor.setNombre("Juan");
        conductor.setEmail("juan@test.com");
        conductor.setContrasenia("pass");
        conductor.setFechaDeVencimientoLicencia(LocalDate.now().plusDays(30));
        conductor.setRol("CONDUCTOR");
        conductor.setActivo(true);

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

        Conductor conductor = new Conductor();
        conductor.setId(conductorId);
        conductor.setNombre("Juan");
        conductor.setEmail("juan@test.com");
        conductor.setContrasenia("pass");
        conductor.setFechaDeVencimientoLicencia(LocalDate.now().plusDays(30));
        conductor.setRol("CONDUCTOR");
        conductor.setActivo(true);

        Conductor otroConductor = new Conductor();
        otroConductor.setId(otroConductorId);
        otroConductor.setNombre("Pedro");
        otroConductor.setEmail("pedro@test.com");
        otroConductor.setContrasenia("pass");
        otroConductor.setFechaDeVencimientoLicencia(LocalDate.now().plusDays(30));
        otroConductor.setRol("CONDUCTOR");
        otroConductor.setActivo(true);

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
        Conductor conductor = new Conductor();
        conductor.setId(conductorId);
        conductor.setNombre("Juan");
        conductor.setEmail("juan@test.com");
        conductor.setContrasenia("pass");
        conductor.setFechaDeVencimientoLicencia(LocalDate.now().plusDays(30));
        conductor.setRol("CONDUCTOR");
        conductor.setActivo(true);

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
    void noDeberiaPublicarSiAsientosDisponiblesEsCeroONegativo() {
        // given
        Viaje viaje = crearViajeDeTest();
        viaje.setAsientosDisponibles(0);
        Viaje viajeNegativo = crearViajeDeTest();
        viajeNegativo.setAsientosDisponibles(-1);

        // when & then
        DatoObligatorioException exception = assertThrows(
                DatoObligatorioException.class,
                () -> servicioViaje.publicarViaje(viaje, 1L, 1L)
        );

        DatoObligatorioException exceptionNegativo = assertThrows(
                DatoObligatorioException.class,
                () -> servicioViaje.publicarViaje(viajeNegativo, 1L, 1L)
        );
        assertThat(exception.getMessage(), equalTo("Los asientos disponibles deben ser mayor a 0"));
        assertThat(exceptionNegativo.getMessage(), equalTo("Los asientos disponibles deben ser mayor a 0"));
        verify(viajeRepositoryMock, never()).guardarViaje(any());
    }

    @Test
    void noDeberiaPublicarSiPrecioEsCeroONegativo() {
        // given
        Viaje viaje = crearViajeDeTest();
        viaje.setPrecio(0.0);
        Viaje viajeNegativo = crearViajeDeTest();
        viajeNegativo.setPrecio(-10.0);

        // when & then
        DatoObligatorioException exception = assertThrows(
                DatoObligatorioException.class,
                () -> servicioViaje.publicarViaje(viaje, 1L, 1L)
        );
        DatoObligatorioException exceptionNegativo = assertThrows(
                DatoObligatorioException.class,
                () -> servicioViaje.publicarViaje(viajeNegativo, 1L, 1L)
        );
        assertThat(exception.getMessage(), equalTo("El precio debe ser mayor a 0"));
        assertThat(exceptionNegativo.getMessage(), equalTo("El precio debe ser mayor a 0"));
        verify(viajeRepositoryMock, never()).guardarViaje(any());
    }

    @Test
    void noDeberiaPublicarSiPrecioEsNull() {
        Viaje viaje = crearViajeDeTest();
        viaje.setPrecio(null);
        DatoObligatorioException exception = assertThrows(
                DatoObligatorioException.class,
                () -> servicioViaje.publicarViaje(viaje, 1L, 1L)
        );
        assertThat(exception.getMessage(), equalTo("El precio debe ser mayor a 0"));
        verify(viajeRepositoryMock, never()).guardarViaje(any());
    }

    @Test
    void noDeberiaPublicarSiAsientosDisponiblesEsNull() {
        Viaje viaje = crearViajeDeTest();
        viaje.setAsientosDisponibles(null);

        DatoObligatorioException exception = assertThrows(
                DatoObligatorioException.class,
                () -> servicioViaje.publicarViaje(viaje, 1L, 1L)
        );
        assertThat(exception.getMessage(), equalTo("Los asientos disponibles deben ser mayor a 0"));
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

        Conductor conductor = new Conductor();
        conductor.setId(conductorId);
        conductor.setNombre("Juan");
        conductor.setEmail("juan@test.com");
        conductor.setContrasenia("pass");
        conductor.setFechaDeVencimientoLicencia(LocalDate.now().plusDays(30));
        conductor.setRol("CONDUCTOR");
        conductor.setActivo(true);

        Vehiculo vehiculo = new Vehiculo(vehiculoId, "ABC123", "Toyota Corolla", "2020", 5, EstadoVerificacion.VERIFICADO, conductor);
        Viaje viaje = crearViajeDeTest();

        when(servicioConductorMock.obtenerConductor(conductorId)).thenReturn(conductor);
        when(servicioVehiculoMock.getById(vehiculoId)).thenReturn(vehiculo);
        when(viajeRepositoryMock.findByOrigenYDestinoYConductorYEstadoIn(any(), any(), any(), anyList())).thenReturn(Collections.emptyList());

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
conductor.setRol("CONDUCTOR");
        conductor.setActivo(true);

        Conductor usuarioEnSesion = new Conductor();
        usuarioEnSesion.setId(1L);
        usuarioEnSesion.setRol("CONDUCTOR");

        Viaje viaje = new Viaje();
        viaje.setId(100L);
        viaje.setConductor(conductor);
        viaje.setEstado(EstadoDeViaje.DISPONIBLE);

        when(viajeRepositoryMock.findById(100L)).thenReturn(Optional.of(viaje));

        servicioViaje.cancelarViaje(100L, usuarioEnSesion);

        assertEquals(EstadoDeViaje.CANCELADO, viaje.getEstado());
        verify(viajeRepositoryMock).modificarViaje(viaje);
    }

    @Test
    void noDebeCancelarSiUnUsuarioNoTieneRol() {
        Conductor usuarioSinRol = new Conductor();
        usuarioSinRol.setId(2L);
        usuarioSinRol.setRol(null);

        assertThrows(UsuarioNoAutorizadoException.class,
                () -> servicioViaje.cancelarViaje(100L, usuarioSinRol));

        verify(viajeRepositoryMock, never()).modificarViaje(any());
    }

    @Test
    void noDebeCancelarSiUnViajeNoExiste() {
        Conductor usuarioConductor = new Conductor();
        usuarioConductor.setId(1L);
        usuarioConductor.setRol("CONDUCTOR");

        when(viajeRepositoryMock.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ViajeNoEncontradoException.class,
                () -> servicioViaje.cancelarViaje(999L, usuarioConductor));

        verify(viajeRepositoryMock, never()).modificarViaje(any());
    }

    @Test
    void noSeDebeCancelarSiUnViajeNoPerteneceAlConductor() {
        Conductor usuarioConductor = new Conductor();
        usuarioConductor.setId(1L);
        usuarioConductor.setRol("CONDUCTOR");

        // el viaje pertenece a otro conductor
        Conductor otroConductor = new Conductor();
        otroConductor.setId(99L);
        otroConductor.setRol("CONDUCTOR");
        otroConductor.setActivo(true);

        Viaje viaje = new Viaje();
        viaje.setId(100L);
        viaje.setConductor(otroConductor);
        viaje.setEstado(EstadoDeViaje.DISPONIBLE);

        when(viajeRepositoryMock.findById(100L)).thenReturn(Optional.of(viaje));

        assertThrows(UsuarioNoAutorizadoException.class,
                () -> servicioViaje.cancelarViaje(100L, usuarioConductor));

        verify(viajeRepositoryMock, never()).modificarViaje(any());
    }

    @Test
    void noSeDebeCancelarSiElEstadoDelViajeEsFinalizado() {
        Conductor usuarioConductor = new Conductor();
        usuarioConductor.setId(1L);
        usuarioConductor.setRol("CONDUCTOR");

        Conductor conductor = new Conductor();
        conductor.setId(1L);
        conductor.setRol("CONDUCTOR");
        conductor.setActivo(true);

        Viaje viaje = new Viaje();
        viaje.setId(100L);
        viaje.setConductor(conductor);
        //el viaje se finalizo
        viaje.setEstado(EstadoDeViaje.FINALIZADO);

        when(viajeRepositoryMock.findById(100L)).thenReturn(Optional.of(viaje));

        assertThrows(ViajeNoCancelableException.class,
                () -> servicioViaje.cancelarViaje(100L, usuarioConductor));

        verify(viajeRepositoryMock, never()).modificarViaje(any());
    }

    @Test
    void noSeDebeCancelarSiElEstadoDelViajeEsCancelado() {
        Conductor usuarioConductor = new Conductor();
        usuarioConductor.setId(1L);
        usuarioConductor.setRol("CONDUCTOR");

        Conductor conductor = new Conductor();
        conductor.setId(1L);
        conductor.setRol("CONDUCTOR");
        conductor.setActivo(true);

        Viaje viaje = new Viaje();
        viaje.setId(100L);
        viaje.setConductor(conductor);
        //viaje cancelado
        viaje.setEstado(EstadoDeViaje.CANCELADO);

        when(viajeRepositoryMock.findById(100L)).thenReturn(Optional.of(viaje));

        assertThrows(ViajeNoCancelableException.class,
                () -> servicioViaje.cancelarViaje(100L, usuarioConductor));

        verify(viajeRepositoryMock, never()).modificarViaje(any());
    }

    @Test
    void noDebeListarViajesSiConductorEsNull() {


        assertThrows(UsuarioNoAutorizadoException.class,
                () -> servicioViaje.listarViajesPorConductor(null));

        verify(viajeRepositoryMock, never()).findByConductorId(anyLong());
    }

    @Test
    void deberiaListarViajesPorConductor() throws UsuarioNoAutorizadoException {
        Conductor conductor = new Conductor();
        conductor.setId(1L);
        conductor.setRol("CONDUCTOR");
        conductor.setActivo(true);

        Viaje viaje1 = new Viaje();
        viaje1.setId(10L);
        Viaje viaje2 = new Viaje();
        viaje2.setId(20L);

        List<Viaje> viajesEsperados = List.of(viaje1, viaje2);

        when(viajeRepositoryMock.findByConductorId(1L)).thenReturn(viajesEsperados);

        List<Viaje> resultado = servicioViaje.listarViajesPorConductor(conductor);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());

        verify(viajeRepositoryMock).findByConductorId(1L);
    }

    @Test
    void deberiaDevolverListaVaciaSiConductorNoTieneViajes() throws UsuarioNoAutorizadoException {


        Conductor conductorSinViajes = new Conductor();
        conductorSinViajes.setId(99L);
        conductorSinViajes.setRol("CONDUCTOR");
        conductorSinViajes.setActivo(true);

        when(viajeRepositoryMock.findByConductorId(99L)).thenReturn(Collections.emptyList());

        List<Viaje> resultado = servicioViaje.listarViajesPorConductor(conductorSinViajes);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        assertEquals(0, resultado.size());

        verify(viajeRepositoryMock).findByConductorId(99L);
    }

    @Test
    void noDeberiaPublicarSiOrigenEsNull() {
        // given
        Viaje viaje = crearViajeDeTest();
        viaje.setOrigen(null);

        // when & then
        DatoObligatorioException exception = assertThrows(
                DatoObligatorioException.class,
                () -> servicioViaje.publicarViaje(viaje, 1L, 1L)
        );

        assertThat(exception.getMessage(), equalTo("La ciudad de origen es obligatoria"));
        verify(viajeRepositoryMock, never()).guardarViaje(any());
    }

    @Test
    void noDeberiaPublicarSiDestinoEsNull() {
        // given
        Viaje viaje = crearViajeDeTest();
        viaje.setDestino(null);

        // when & then
        DatoObligatorioException exception = assertThrows(
                DatoObligatorioException.class,
                () -> servicioViaje.publicarViaje(viaje, 1L, 1L)
        );

        assertThat(exception.getMessage(), equalTo("La ciudad de destino es obligatoria"));
        verify(viajeRepositoryMock, never()).guardarViaje(any());
    }

    @Test
    void noDeberiaPublicarSiOrigenYDestinoSonIguales() {
        // given
        Ciudad ciudad = new Ciudad(1L, "Buenos Aires", -34.6037f, -58.3816f);
        Viaje viaje = crearViajeDeTest();
        viaje.setOrigen(ciudad);
        viaje.setDestino(ciudad);

        // when & then
        DatoObligatorioException exception = assertThrows(
                DatoObligatorioException.class,
                () -> servicioViaje.publicarViaje(viaje, 1L, 1L)
        );

        assertThat(exception.getMessage(), equalTo("La ciudad de origen y destino deben ser diferentes"));
        verify(viajeRepositoryMock, never()).guardarViaje(any());
    }

    @Test
    void noDeberiaPublicarSiYaExisteViajeDisponibleConMismoOrigenYDestino() throws Exception {
        // given
        Long conductorId = 1L;
        Conductor conductor = new Conductor();
        conductor.setId(conductorId);
        conductor.setNombre("Juan");
        conductor.setEmail("juan@test.com");
        conductor.setContrasenia("pass");
        conductor.setFechaDeVencimientoLicencia(LocalDate.now().plusDays(30));
        conductor.setRol("CONDUCTOR");
        conductor.setActivo(true);

        Vehiculo vehiculo = new Vehiculo(1L, "ABC123", "Toyota Corolla", "2020", 5, EstadoVerificacion.VERIFICADO, conductor);

        Viaje viaje = crearViajeDeTest();
        Ciudad origen = viaje.getOrigen();
        Ciudad destino = viaje.getDestino();

        // Simular que ya existe un viaje en estado DISPONIBLE
        Viaje viajeExistente = new Viaje();
        viajeExistente.setEstado(EstadoDeViaje.DISPONIBLE);
        viajeExistente.setOrigen(origen);
        viajeExistente.setDestino(destino);
        viajeExistente.setConductor(conductor);

        when(servicioConductorMock.obtenerConductor(conductorId)).thenReturn(conductor);
        when(servicioVehiculoMock.getById(1L)).thenReturn(vehiculo);
        when(viajeRepositoryMock.findByOrigenYDestinoYConductorYEstadoIn(
                eq(origen),
                eq(destino),
                eq(conductor),
                anyList()
        )).thenReturn(Arrays.asList(viajeExistente));

        // when & then
        ViajeDuplicadoException exception = assertThrows(
                ViajeDuplicadoException.class,
                () -> servicioViaje.publicarViaje(viaje, conductorId, 1L)
        );

        assertThat(exception.getMessage(), containsString("Ya tenés un viaje publicado con el mismo origen y destino"));
        verify(viajeRepositoryMock, never()).guardarViaje(any());
    }

    @Test
    void noDeberiaPublicarSiYaExisteViajeCompletoConMismoOrigenYDestino() throws Exception {
        // given
        Long conductorId = 1L;
        Conductor conductor = new Conductor();
        conductor.setId(conductorId);
        conductor.setNombre("Juan");
        conductor.setEmail("juan@test.com");
        conductor.setContrasenia("pass");
        conductor.setFechaDeVencimientoLicencia(LocalDate.now().plusDays(30));
        conductor.setRol("CONDUCTOR");
        conductor.setActivo(true);

        Vehiculo vehiculo = new Vehiculo(1L, "ABC123", "Toyota Corolla", "2020", 5, EstadoVerificacion.VERIFICADO, conductor);

        Viaje viaje = crearViajeDeTest();
        Ciudad origen = viaje.getOrigen();
        Ciudad destino = viaje.getDestino();

        // Simular que ya existe un viaje en estado COMPLETO
        Viaje viajeExistente = new Viaje();
        viajeExistente.setEstado(EstadoDeViaje.COMPLETO);
        viajeExistente.setOrigen(origen);
        viajeExistente.setDestino(destino);
        viajeExistente.setConductor(conductor);

        when(servicioConductorMock.obtenerConductor(conductorId)).thenReturn(conductor);
        when(servicioVehiculoMock.getById(1L)).thenReturn(vehiculo);
        when(viajeRepositoryMock.findByOrigenYDestinoYConductorYEstadoIn(
                eq(origen),
                eq(destino),
                eq(conductor),
                anyList()
        )).thenReturn(Arrays.asList(viajeExistente));

        // when & then
        ViajeDuplicadoException exception = assertThrows(
                ViajeDuplicadoException.class,
                () -> servicioViaje.publicarViaje(viaje, conductorId, 1L)
        );

        assertThat(exception.getMessage(), containsString("Ya tenés un viaje publicado con el mismo origen y destino"));
        verify(viajeRepositoryMock, never()).guardarViaje(any());
    }

    @Test
    void deberiaPublicarSiViajeExistenteEstaFinalizado() throws Exception {
        // given
        Long conductorId = 1L;
        Conductor conductor = new Conductor();
        conductor.setId(conductorId);
        conductor.setNombre("Juan");
        conductor.setEmail("juan@test.com");
        conductor.setContrasenia("pass");
        conductor.setFechaDeVencimientoLicencia(LocalDate.now().plusDays(30));
        conductor.setRol("CONDUCTOR");
        conductor.setActivo(true);

        Vehiculo vehiculo = new Vehiculo(1L, "ABC123", "Toyota Corolla", "2020", 5, EstadoVerificacion.VERIFICADO, conductor);

        Viaje viaje = crearViajeDeTest();
        Ciudad origen = viaje.getOrigen();
        Ciudad destino = viaje.getDestino();

        when(servicioConductorMock.obtenerConductor(conductorId)).thenReturn(conductor);
        when(servicioVehiculoMock.getById(1L)).thenReturn(vehiculo);
        // Simular que no hay viajes en estados DISPONIBLE o COMPLETO
        when(viajeRepositoryMock.findByOrigenYDestinoYConductorYEstadoIn(
                eq(origen),
                eq(destino),
                eq(conductor),
                anyList()
        )).thenReturn(Collections.emptyList());

        // when
        servicioViaje.publicarViaje(viaje, conductorId, 1L);

        // then
        ArgumentCaptor<Viaje> viajeCaptor = ArgumentCaptor.forClass(Viaje.class);
        verify(viajeRepositoryMock).guardarViaje(viajeCaptor.capture());
    }

    @Test
    void deberiaPublicarSiViajeExistenteEstaCancelado() throws Exception {
        // given
        Long conductorId = 1L;
        Conductor conductor = new Conductor();
        conductor.setId(conductorId);
        conductor.setNombre("Juan");
        conductor.setEmail("juan@test.com");
        conductor.setContrasenia("pass");
        conductor.setFechaDeVencimientoLicencia(LocalDate.now().plusDays(30));
        conductor.setRol("CONDUCTOR");
        conductor.setActivo(true);

        Vehiculo vehiculo = new Vehiculo(1L, "ABC123", "Toyota Corolla", "2020", 5, EstadoVerificacion.VERIFICADO, conductor);

        Viaje viaje = crearViajeDeTest();
        Ciudad origen = viaje.getOrigen();
        Ciudad destino = viaje.getDestino();

        when(servicioConductorMock.obtenerConductor(conductorId)).thenReturn(conductor);
        when(servicioVehiculoMock.getById(1L)).thenReturn(vehiculo);
        // Simular que no hay viajes en estados DISPONIBLE o COMPLETO
        when(viajeRepositoryMock.findByOrigenYDestinoYConductorYEstadoIn(
                eq(origen),
                eq(destino),
                eq(conductor),
                anyList()
        )).thenReturn(Collections.emptyList());

        // when
        servicioViaje.publicarViaje(viaje, conductorId, 1L);

        // then
        ArgumentCaptor<Viaje> viajeCaptor = ArgumentCaptor.forClass(Viaje.class);
        verify(viajeRepositoryMock).guardarViaje(viajeCaptor.capture());
    }

    @Test
    void obtenerViajePorIdDebeRetornarViaje() throws NotFoundException, ViajeNoEncontradoException, UsuarioNoAutorizadoException {
        // Arrange
        Viaje viajeEsperado = crearViajeDeTest();
        viajeEsperado.setId(1L);
        when(viajeRepositoryMock.findById(1L)).thenReturn(Optional.of(viajeEsperado));
        Viaje resultado = servicioViaje.obtenerViajePorId(1L);
        assertThat(resultado, is(viajeEsperado));
        verify(viajeRepositoryMock).findById(1L);
    }

    @Test
    void obtenerViajePorIdDebeLanzarNotFoundExceptionSiNoExiste() {
        // Arrange
        Long idInexistente = 99L;
        when(viajeRepositoryMock.findById(idInexistente)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> servicioViaje.obtenerViajePorId(idInexistente));
        verify(viajeRepositoryMock).findById(idInexistente);
    }

    @Test
    void deberiaObtenerDetalleDeViajeCorrectamente() throws Exception {
        // given
        Long viajeId = 1L;
        Viaje viajeEsperado = crearViajeCompleto();
        when(viajeRepositoryMock.findById(viajeId)).thenReturn(Optional.of(viajeEsperado));

        // when
        Viaje resultado = servicioViaje.obtenerDetalleDeViaje(viajeId);

        // then
        assertThat(resultado, is(viajeEsperado));
        verify(viajeRepositoryMock).findById(viajeId);
        // Verificar que se inicializaron las relaciones
        assertThat(Hibernate.isInitialized(resultado.getOrigen()), is(true));
        assertThat(Hibernate.isInitialized(resultado.getDestino()), is(true));
        assertThat(Hibernate.isInitialized(resultado.getVehiculo()), is(true));
        assertThat(Hibernate.isInitialized(resultado.getViajeros()), is(true));
        assertThat(Hibernate.isInitialized(resultado.getParadas()), is(true));
        // Verificar inicialización de relaciones anidadas
        for (Parada parada : resultado.getParadas()) {
            assertThat(Hibernate.isInitialized(parada.getCiudad()), is(true));
        }
    }

    @Test
    void deberiaLanzarNotFoundExceptionSiViajeNoExiste() {
        // given
        Long viajeId = 999L;
        when(viajeRepositoryMock.findById(viajeId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () ->
                servicioViaje.obtenerDetalleDeViaje(viajeId)
        );
        verify(viajeRepositoryMock).findById(viajeId);
    }

    // Método auxiliar para crear un viaje con todas sus relaciones
    private Viaje crearViajeCompleto() {
        // Crear ciudades
        Ciudad origen = new Ciudad(1L, "Buenos Aires", -34.6037f, -58.3816f);
        Ciudad destino = new Ciudad(2L, "Córdoba", -31.4201f, -64.1888f);
        Ciudad paradaCiudad = new Ciudad(3L, "Rosario", -32.9468f, -60.6393f);

        // Crear conductor
        Conductor conductor = new Conductor();
        conductor.setId(1L);
        conductor.setNombre("Juan");
        conductor.setEmail("juan@test.com");

        // Crear vehículo
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(1L);
        vehiculo.setPatente("ABC123");
        vehiculo.setModelo("Toyota Corolla");
        vehiculo.setConductor(conductor);

        // Crear viajeros
        Viajero viajero1 = new Viajero();
        viajero1.setId(1L);
        viajero1.setNombre("Ana");
        viajero1.setEmail("ana@test.com");

        Viajero viajero2 = new Viajero();
        viajero2.setId(2L);
        viajero2.setNombre("Pedro");
        viajero2.setEmail("pedro@test.com");

        // Crear parada
        Parada parada = new Parada();
        parada.setId(1L);
        parada.setCiudad(paradaCiudad);
        parada.setOrden(1);

        // Crear viaje
        Viaje viaje = new Viaje();
        viaje.setId(1L);
        viaje.setOrigen(origen);
        viaje.setDestino(destino);
        viaje.setVehiculo(vehiculo);
        viaje.setFechaHoraDeSalida(LocalDateTime.now().plusDays(1));
        viaje.setPrecio(1500.0);
        viaje.setAsientosDisponibles(3);
        viaje.setFechaDeCreacion(LocalDateTime.now());
        viaje.setEstado(EstadoDeViaje.DISPONIBLE);
        viaje.setViajeros(Arrays.asList(viajero1, viajero2));
        viaje.setParadas(Collections.singletonList(parada));

        // Establecer la relación bidireccional
        parada.setViaje(viaje);

        return viaje;
    }
}