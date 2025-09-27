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

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
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
    void obtenerVehiculoPorId() throws NotFoundException {
        Long id = 1L;
        Vehiculo esperado = new Vehiculo(id, null, "Toyota", "2020", "ABC123", 4, EstadoVerificacion.VERIFICADO);
        when(repositorioVehiculoMock.findById(id)).thenReturn(esperado);

        Vehiculo resultado = servicioVehiculo.getById(id);

        assertThat(resultado, notNullValue());
        assertThat(resultado, equalTo(esperado));
        verify(repositorioVehiculoMock).findById(id);
    }

    @Test
    void obtenerVehiculoPorId_NoEncontrado_LanzaExcepcion() {
        when(repositorioVehiculoMock.findById(1L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> servicioVehiculo.getById(1L));
        verify(repositorioVehiculoMock).findById(1L);
    }

    @Test
    void obtenerVehiculoPorPatente() throws NotFoundException {
        String patente = "ABC123";
        Vehiculo esperado = new Vehiculo(1L, null, "Toyota", "2020", patente, 4, EstadoVerificacion.VERIFICADO);
        when(repositorioVehiculoMock.encontrarVehiculoConPatente(patente)).thenReturn(esperado);

        Vehiculo resultado = servicioVehiculo.obtenerVehiculoConPatente(patente);

        assertThat(resultado, notNullValue());
        assertThat(resultado, equalTo(esperado));
        verify(repositorioVehiculoMock).encontrarVehiculoConPatente(patente);
    }

    @Test
    void obtenerVehiculoPorPatente_NoEncontrado_LanzaExcepcion() {
        when(repositorioVehiculoMock.encontrarVehiculoConPatente("NOEXISTE")).thenReturn(null);

        assertThrows(NotFoundException.class,
                () -> servicioVehiculo.obtenerVehiculoConPatente("NOEXISTE"));
        verify(repositorioVehiculoMock).encontrarVehiculoConPatente("NOEXISTE");
    }

    @Test
    void guardarVehiculoCorrectamente() throws PatenteDuplicadaException, NotFoundException {
        Conductor conductor = new Conductor(1L, null, "Ana", "ana@mail.com", "123", null);
        Vehiculo vehiculo = new Vehiculo(null, conductor, "Toyota", "2020", "ABC123", 4, EstadoVerificacion.PENDIENTE);
        Vehiculo guardado = new Vehiculo(1L, conductor, "Toyota", "2020", "ABC123", 4, EstadoVerificacion.PENDIENTE);

        when(repositorioConductorMock.buscarPorId(conductor.getId())).thenReturn(Optional.of(conductor));
        when(repositorioVehiculoMock.encontrarVehiculoConPatente("ABC123")).thenReturn(null);
        when(repositorioVehiculoMock.guardarVehiculo(any(Vehiculo.class))).thenReturn(guardado);

        Vehiculo resultado = servicioVehiculo.guardarVehiculo(vehiculo);

        assertThat(resultado, notNullValue());
        assertThat(resultado.getId(), equalTo(1L));
        verify(repositorioVehiculoMock).guardarVehiculo(any(Vehiculo.class));
    }

    @Test
    void guardarVehiculo_PatenteDuplicada_LanzaExcepcion() {
        Conductor conductor = new Conductor(1L, null, "Ana", "ana@mail.com", "123", null);
        Vehiculo vehiculo = new Vehiculo(null, conductor, "Toyota", "2020", "ABC123", 4, EstadoVerificacion.PENDIENTE);

        when(repositorioConductorMock.buscarPorId(1L)).thenReturn(Optional.of(conductor));
        when(repositorioVehiculoMock.encontrarVehiculoConPatente("ABC123")).thenReturn(new Vehiculo());

        assertThrows(PatenteDuplicadaException.class, () -> servicioVehiculo.guardarVehiculo(vehiculo));
        verify(repositorioVehiculoMock, never()).guardarVehiculo(any(Vehiculo.class));
    }

    @Test
    void obtenerVehiculosPorConductor() {
        Long conductorId = 1L;
        List<Vehiculo> esperados = List.of(
                new Vehiculo(1L, null, "Toyota", "2020", "ABC123", 4, EstadoVerificacion.VERIFICADO),
                new Vehiculo(2L, null, "Honda", "2019", "XYZ789", 4, EstadoVerificacion.VERIFICADO)
        );
        when(repositorioVehiculoMock.obtenerVehiculosParaConductor(conductorId)).thenReturn(esperados);

        List<Vehiculo> resultados = servicioVehiculo.obtenerVehiculosParaConductor(conductorId);

        assertThat(resultados, notNullValue());
        assertThat(resultados, hasSize(2));
        assertThat(resultados, equalTo(esperados));
        verify(repositorioVehiculoMock).obtenerVehiculosParaConductor(conductorId);
    }
}
