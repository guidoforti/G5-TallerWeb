package com.tallerwebi.dominio;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Vehiculo;
import com.tallerwebi.dominio.Enums.EstadoVerificacion;
import com.tallerwebi.dominio.IRepository.RepositorioConductor;
import com.tallerwebi.dominio.IRepository.RepositorioVehiculo;
import com.tallerwebi.dominio.IServicio.ServicioVehiculo;
import com.tallerwebi.dominio.ServiceImpl.ServicioVehiculoImpl;
import com.tallerwebi.dominio.excepcion.NotFoundException;
import com.tallerwebi.dominio.excepcion.PatenteDuplicadaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.matchers.Not;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ServicioVehiculoTest {

    @Mock
    private RepositorioVehiculo repositorioVehiculoMock;

    @Mock
    private RepositorioConductor repositorioConductorMock;

    private ServicioVehiculo servicioVehiculo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repositorioVehiculoMock = mock(RepositorioVehiculo.class);
        repositorioConductorMock = mock(RepositorioConductor.class);
        servicioVehiculo = new ServicioVehiculoImpl(repositorioVehiculoMock, repositorioConductorMock);
    }

    @Test
    void obtenerVehiculoPorIdDebeRetornarVehiculoSiExiste() throws NotFoundException {
        Long id = 1L;
        Vehiculo esperado = new Vehiculo(
                id,
                "ABC123",         // patente
                "Toyota",         // modelo
                "2020",           // año
                4,                // asientosTotales
                EstadoVerificacion.VERIFICADO,
                null              // conductor
        );
        when(repositorioVehiculoMock.findById(id)).thenReturn(Optional.of(esperado));

        Vehiculo resultado = servicioVehiculo.getById(id);

        assertThat(resultado, notNullValue());
        assertThat(resultado, equalTo(esperado));
        verify(repositorioVehiculoMock, times(1)).findById(id);
    }

    @Test
    void obtenerVehiculoPorIdNoExisteDeberiaLanzarExcepcion(){
        Long idInexistente = 11L;
        when(repositorioVehiculoMock.findById(idInexistente)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> servicioVehiculo.getById(idInexistente));
        verify(repositorioVehiculoMock, times(1)).findById(idInexistente);        
    }

    @Test
    void obtenerVehiculoPorPatenteDeberiaRetornarVehiculoSiExiste() throws NotFoundException {
        String patente = "ABC123";
        Vehiculo esperado = new Vehiculo(
                1L,
                patente,
                "Toyota",
                "2020",
                4,
                EstadoVerificacion.VERIFICADO,
                null
        );
        when(repositorioVehiculoMock.encontrarVehiculoConPatente(patente)).thenReturn(Optional.of(esperado));

        Vehiculo resultado = servicioVehiculo.obtenerVehiculoConPatente(patente);

        assertThat(resultado, notNullValue());
        assertThat(resultado, equalTo(esperado));
        verify(repositorioVehiculoMock, times(1)).encontrarVehiculoConPatente(patente);
    }

    @Test
    void obtenerVehiculoConPatente_NoExiste_LanzaExcepcion() {
        String patente = "XYZ2378";
        when(repositorioVehiculoMock.encontrarVehiculoConPatente(patente)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> servicioVehiculo.obtenerVehiculoConPatente(patente));
        verify(repositorioVehiculoMock, times(1)).encontrarVehiculoConPatente(patente);
    }

    @Test
    void guardarVehiculoCorrectamente() throws PatenteDuplicadaException, NotFoundException {
        Conductor conductor = new Conductor(
                1L,
                "Ana",
                "ana@mail.com",
                "123",
                null,
                new ArrayList<>(),
                new ArrayList<>()
        );
        Vehiculo vehiculo = new Vehiculo(
                null,
                "ABC123",
                "Toyota",
                "2020",
                4,
                EstadoVerificacion.PENDIENTE,
                conductor
        );
        Vehiculo guardado = new Vehiculo(
                1L,
                "ABC123",
                "Toyota",
                "2020",
                4,
                EstadoVerificacion.PENDIENTE,
                conductor
        );

        when(repositorioVehiculoMock.encontrarVehiculoConPatente("ABC123")).thenReturn(Optional.empty());
        when(repositorioVehiculoMock.guardarVehiculo(any(Vehiculo.class))).thenReturn(guardado);

        Vehiculo resultado = servicioVehiculo.guardarVehiculo(vehiculo);

        assertThat(resultado, notNullValue());
        assertThat(resultado.getId(), equalTo(1L));
        verify(repositorioVehiculoMock, times(1)).encontrarVehiculoConPatente("ABC123");
        verify(repositorioVehiculoMock, times(1)).guardarVehiculo(any(Vehiculo.class));
    }

    @Test
    void guardarVehiculo_PatenteDuplicada_LanzaExcepcion() {
        Conductor conductor = new Conductor(
                1L,
                "Ana",
                "ana@mail.com",
                "123",
                null,
                new ArrayList<>(),
                new ArrayList<>()
        );
        Vehiculo vehiculoDuplicado = new Vehiculo(
                null,
                "ABC123",
                "Toyota",
                "2020",
                4,
                EstadoVerificacion.PENDIENTE,
                conductor
        );

        when(repositorioVehiculoMock.encontrarVehiculoConPatente("ABC123")).thenReturn(Optional.of(new Vehiculo()));

        assertThrows(PatenteDuplicadaException.class, () -> servicioVehiculo.guardarVehiculo(vehiculoDuplicado));
        verify(repositorioVehiculoMock, times(1)).encontrarVehiculoConPatente("ABC123");
        verify(repositorioVehiculoMock, never()).guardarVehiculo(any(Vehiculo.class));
    }

    @Test
    void obtenerVehiculosPorConductorDeberiaRetornarListaDeVehiculos() {
        Long conductorId = 1L;
        List<Vehiculo> esperados = List.of(
                new Vehiculo(1L, "ABC123", "Toyota", "2020", 4, EstadoVerificacion.VERIFICADO, null),
                new Vehiculo(2L, "XYZ789", "Honda", "2019", 4, EstadoVerificacion.VERIFICADO, null)
        );

        when(repositorioVehiculoMock.obtenerVehiculosParaConductor(conductorId)).thenReturn(esperados);

        List<Vehiculo> resultados = servicioVehiculo.obtenerVehiculosParaConductor(conductorId);

        assertThat(resultados, notNullValue());
        assertThat(resultados, hasSize(2));
        assertThat(resultados, equalTo(esperados));
        verify(repositorioVehiculoMock, times(1)).obtenerVehiculosParaConductor(conductorId);
    }

    @Test
    void obtenerVehiculoPorIdNoExisteLanzaExcepcion() {
        Long id = 99L;
        when(repositorioVehiculoMock.findById(id)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> servicioVehiculo.getById(id));

        verify(repositorioVehiculoMock).findById(id);
    }

    @Test
    void obtenerVehiculosParaConductor_IdNulo_LanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> servicioVehiculo.obtenerVehiculosParaConductor(null));
        verifyNoInteractions(repositorioVehiculoMock);
    }

    

}
