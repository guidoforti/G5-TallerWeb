package com.tallerwebi.dominio;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Vehiculo;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Enums.EstadoDeViaje;
import com.tallerwebi.dominio.Enums.EstadoVerificacion;
import com.tallerwebi.dominio.IRepository.RepositorioConductor;
import com.tallerwebi.dominio.IRepository.RepositorioVehiculo;
import com.tallerwebi.dominio.IRepository.ViajeRepository;
import com.tallerwebi.dominio.IServicio.ServicioVehiculo;
import com.tallerwebi.dominio.ServiceImpl.ServicioVehiculoImpl;
import com.tallerwebi.dominio.excepcion.NotFoundException;
import com.tallerwebi.dominio.excepcion.PatenteDuplicadaException;
import com.tallerwebi.dominio.excepcion.VehiculoConViajesActivosException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ServicioVehiculoTest {

    private RepositorioVehiculo repositorioVehiculoMock;
    private RepositorioConductor repositorioConductorMock;
    private ViajeRepository viajeRepositoryMock;
    private ServicioVehiculo servicioVehiculo;

    private final Long ID_CONDUCTOR = 1L;
    private final Long ID_VEHICULO = 10L;
    private final String PATENTE = "ABC123";
    private final Conductor CONDUCTOR = new Conductor();
    private Vehiculo VEHICULO_ACTIVO;

    @BeforeEach
    void setUp() {
        repositorioVehiculoMock = mock(RepositorioVehiculo.class);
        repositorioConductorMock = mock(RepositorioConductor.class);
        viajeRepositoryMock = mock(ViajeRepository.class);
        servicioVehiculo = new ServicioVehiculoImpl(repositorioVehiculoMock, repositorioConductorMock, viajeRepositoryMock);

        CONDUCTOR.setId(ID_CONDUCTOR);
        VEHICULO_ACTIVO = new Vehiculo(ID_VEHICULO, PATENTE, "Modelo", "2020", 4, EstadoVerificacion.VERIFICADO, CONDUCTOR);
    }

    // --- Métodos Básicos (GET / GUARDAR) ---

    @Test
    void obtenerVehiculoPorIdDebeRetornarVehiculoSiExiste() throws NotFoundException {
        when(repositorioVehiculoMock.findById(ID_VEHICULO)).thenReturn(Optional.of(VEHICULO_ACTIVO));
        Vehiculo resultado = servicioVehiculo.getById(ID_VEHICULO);

        assertThat(resultado, notNullValue());
        assertThat(resultado, equalTo(VEHICULO_ACTIVO));
        verify(repositorioVehiculoMock, times(1)).findById(ID_VEHICULO);
    }

    @Test
    void obtenerVehiculoPorIdNoExisteDeberiaLanzarExcepcion() {
        when(repositorioVehiculoMock.findById(ID_VEHICULO)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> servicioVehiculo.getById(ID_VEHICULO));
    }

    @Test
    void obtenerVehiculoPorPatenteDeberiaRetornarVehiculoSiExiste() throws NotFoundException {
        when(repositorioVehiculoMock.encontrarVehiculoConPatente(PATENTE)).thenReturn(Optional.of(VEHICULO_ACTIVO));
        Vehiculo resultado = servicioVehiculo.obtenerVehiculoConPatente(PATENTE);

        assertThat(resultado, notNullValue());
        verify(repositorioVehiculoMock, times(1)).encontrarVehiculoConPatente(PATENTE);
    }

    @Test
    void obtenerVehiculoConPatente_NoExiste_LanzaExcepcion() {
        when(repositorioVehiculoMock.encontrarVehiculoConPatente(PATENTE)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> servicioVehiculo.obtenerVehiculoConPatente(PATENTE));
    }

    @Test
    void guardarVehiculoCorrectamente() throws PatenteDuplicadaException, NotFoundException {
        Conductor conductor = CONDUCTOR;
        Vehiculo vehiculo = new Vehiculo(null, PATENTE, "Toyota", "2020", 4, EstadoVerificacion.PENDIENTE, conductor);
        Vehiculo guardado = new Vehiculo(1L, PATENTE, "Toyota", "2020", 4, EstadoVerificacion.PENDIENTE, conductor);

        when(repositorioVehiculoMock.encontrarVehiculoConPatente(PATENTE)).thenReturn(Optional.empty());
        when(repositorioVehiculoMock.guardarVehiculo(any(Vehiculo.class))).thenReturn(guardado);

        Vehiculo resultado = servicioVehiculo.guardarVehiculo(vehiculo);

        assertThat(resultado, notNullValue());
        assertThat(resultado.getId(), equalTo(1L));
        verify(repositorioVehiculoMock, times(1)).encontrarVehiculoConPatente(PATENTE);
        verify(repositorioVehiculoMock, times(1)).guardarVehiculo(any(Vehiculo.class));
    }

    @Test
    void guardarVehiculo_PatenteDuplicada_LanzaExcepcion() {
        Conductor conductor = CONDUCTOR;
        Vehiculo vehiculoDuplicado = new Vehiculo(null, PATENTE, "Toyota", "2020", 4, EstadoVerificacion.PENDIENTE, conductor);

        when(repositorioVehiculoMock.encontrarVehiculoConPatente(PATENTE)).thenReturn(Optional.of(new Vehiculo()));

        assertThrows(PatenteDuplicadaException.class, () -> servicioVehiculo.guardarVehiculo(vehiculoDuplicado));
        verify(repositorioVehiculoMock, times(1)).encontrarVehiculoConPatente(PATENTE);
        verify(repositorioVehiculoMock, never()).guardarVehiculo(any(Vehiculo.class));
    }
    @Test
    void obtenerVehiculosParaConductor_DebeExcluirDesactivados() {
        List<Vehiculo> esperados = List.of(VEHICULO_ACTIVO);

        when(repositorioVehiculoMock.findByConductorIdAndEstadoVerificacionNot(eq(ID_CONDUCTOR), eq(EstadoVerificacion.DESACTIVADO))).thenReturn(esperados);

        List<Vehiculo> resultados = servicioVehiculo.obtenerVehiculosParaConductor(ID_CONDUCTOR);

        assertThat(resultados, hasSize(1));
        verify(repositorioVehiculoMock, times(1)).findByConductorIdAndEstadoVerificacionNot(ID_CONDUCTOR, EstadoVerificacion.DESACTIVADO);
    }

    @Test
    void obtenerTodosLosVehiculosDeConductor_DebeIncluirTodos() {
        List<Vehiculo> esperados = List.of(VEHICULO_ACTIVO, new Vehiculo(11L, "DES", "Ford", "2021", 4, EstadoVerificacion.DESACTIVADO, CONDUCTOR));

        when(repositorioVehiculoMock.obtenerVehiculosParaConductor(ID_CONDUCTOR)).thenReturn(esperados);

        List<Vehiculo> resultados = servicioVehiculo.obtenerTodosLosVehiculosDeConductor(ID_CONDUCTOR);

        assertThat(resultados, hasSize(2));
        verify(repositorioVehiculoMock, times(1)).obtenerVehiculosParaConductor(ID_CONDUCTOR);
    }

    @Test
    void obtenerVehiculosPorConductorDebeLanzarExcepcionSiIdEsNull() {
        assertThrows(IllegalArgumentException.class, () -> servicioVehiculo.obtenerVehiculosParaConductor(null));
    }

    @Test
    void desactivarVehiculo_DebeSerExitoso_SiNoHayViajesActivos() throws NotFoundException, VehiculoConViajesActivosException {
        when(repositorioVehiculoMock.findById(ID_VEHICULO)).thenReturn(Optional.of(VEHICULO_ACTIVO));

        List<Viaje> viajesActivos = new ArrayList<>();
        when(viajeRepositoryMock.findByVehiculoAndEstadoIn(eq(VEHICULO_ACTIVO), anyList())).thenReturn(viajesActivos);

        servicioVehiculo.desactivarVehiculo(ID_VEHICULO);

        assertThat(VEHICULO_ACTIVO.getEstadoVerificacion(), equalTo(EstadoVerificacion.DESACTIVADO));
        verify(repositorioVehiculoMock, times(1)).guardarVehiculo(VEHICULO_ACTIVO);
    }

    @Test
    void desactivarVehiculo_DebeLanzarExcepcion_SiTieneViajesActivos() {
        when(repositorioVehiculoMock.findById(ID_VEHICULO)).thenReturn(Optional.of(VEHICULO_ACTIVO));

        List<Viaje> viajesActivos = List.of(new Viaje(), new Viaje());
        when(viajeRepositoryMock.findByVehiculoAndEstadoIn(eq(VEHICULO_ACTIVO), anyList())).thenReturn(viajesActivos);

        assertThrows(VehiculoConViajesActivosException.class, () -> servicioVehiculo.desactivarVehiculo(ID_VEHICULO));
        verify(repositorioVehiculoMock, never()).guardarVehiculo(any());
    }

    @Test
    void verificarViajesActivos_DebeLanzarExcepcion_SiElVehiculoYaEstaDesactivado() throws NotFoundException {
        VEHICULO_ACTIVO.setEstadoVerificacion(EstadoVerificacion.DESACTIVADO);
        when(repositorioVehiculoMock.findById(ID_VEHICULO)).thenReturn(Optional.of(VEHICULO_ACTIVO));

        assertThrows(VehiculoConViajesActivosException.class, () -> servicioVehiculo.verificarViajesActivos(ID_VEHICULO));
    }
}