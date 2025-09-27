package com.tallerwebi.dominio;

import com.tallerwebi.config.ManualModelMapper;
import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Vehiculo;
import com.tallerwebi.dominio.Enums.EstadoVerificacion;
import com.tallerwebi.dominio.IRepository.RepositorioConductor;
import com.tallerwebi.dominio.IRepository.RepositorioVehiculo;
import com.tallerwebi.dominio.IServicio.ServicioVehiculo;
import com.tallerwebi.dominio.ServiceImpl.ServicioVehiculoImpl;
import com.tallerwebi.dominio.excepcion.NotFoundException;
import com.tallerwebi.dominio.excepcion.PatenteDuplicadaException;
import com.tallerwebi.presentacion.DTO.InputsDTO.VehiculoInputDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.VehiculoOutputDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
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
        servicioVehiculo = new ServicioVehiculoImpl(repositorioVehiculoMock, repositorioConductorMock);
    }

    @Test
    void obtenerVehiculoPorId() throws NotFoundException {
        // Arrange
        Long id = 1L;
        Vehiculo vehiculoEsperado = new Vehiculo(id, null, "Toyota", "2020", "ABC123", 4, EstadoVerificacion.VERIFICADO);
        when(repositorioVehiculoMock.findById(id)).thenReturn(vehiculoEsperado);

        // Act
        Vehiculo resultado = servicioVehiculo.getById(id);

        // Assert
        assertNotNull(resultado);
        assertEquals(vehiculoEsperado, resultado);
        verify(repositorioVehiculoMock).findById(id);
    }

    @Test
    void obtenerVehiculoPorId_NoEncontrado_LanzaExcepcion() {
        // Arrange
        Long id = 1L;
        when(repositorioVehiculoMock.findById(id)).thenReturn(null);

        // Act & Assert
        assertThrows(NotFoundException.class, () -> servicioVehiculo.getById(id));
        verify(repositorioVehiculoMock).findById(id);
    }

    @Test
    void obtenerVehiculoPorPatente() throws NotFoundException {
        // Arrange
        String patente = "ABC123";
        Vehiculo vehiculoEsperado = new Vehiculo(1L, null, "Toyota", "2020", patente, 4, EstadoVerificacion.VERIFICADO);
        when(repositorioVehiculoMock.encontrarVehiculoConPatente(patente)).thenReturn(vehiculoEsperado);

        // Act
        Vehiculo resultado = servicioVehiculo.obtenerVehiculoConPatente(patente);

        // Assert
        assertNotNull(resultado);
        assertEquals(vehiculoEsperado, resultado);
        verify(repositorioVehiculoMock).encontrarVehiculoConPatente(patente);
    }

    @Test
    void obtenerVehiculoPorPatente_NoEncontrado_LanzaExcepcion() {
        // Arrange
        String patente = "NOEXISTE";
        when(repositorioVehiculoMock.encontrarVehiculoConPatente(patente)).thenReturn(null);

        // Act & Assert
        assertThrows(NotFoundException.class,
                () -> servicioVehiculo.obtenerVehiculoConPatente(patente));
        verify(repositorioVehiculoMock).encontrarVehiculoConPatente(patente);
    }

    @Test
    void guardarVehiculoCorrectamente() throws PatenteDuplicadaException, NotFoundException {
        // Arrange
        Long conductorId = 1L;
        Conductor conductor = new Conductor();
        Vehiculo vehiculo = new Vehiculo(null, conductor, "Toyota", "2020", "ABC123", 4, EstadoVerificacion.PENDIENTE);
        Vehiculo vehiculoGuardado = new Vehiculo(1L, conductor, "Toyota", "2020", "ABC123", 4, EstadoVerificacion.PENDIENTE);

        when(repositorioConductorMock.buscarPorId(conductorId)).thenReturn(Optional.of(conductor));
        when(repositorioVehiculoMock.encontrarVehiculoConPatente(anyString())).thenReturn(null);
        when(repositorioVehiculoMock.guardarVehiculo(any(Vehiculo.class))).thenReturn(vehiculoGuardado);

        // Act
        Vehiculo resultado = servicioVehiculo.guardarVehiculo(vehiculo);

        // Assert
        assertNotNull(resultado);
        assertEquals(vehiculoGuardado.getId(), resultado.getId());
        verify(repositorioVehiculoMock).guardarVehiculo(any(Vehiculo.class));
    }

    @Test
    void guardarVehiculo_ConductorNoExiste_LanzaExcepcion() {
        // Arrange
        Long conductorId = 1L;
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setConductor(new Conductor());
        when(repositorioConductorMock.buscarPorId(conductorId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class,
                () -> servicioVehiculo.guardarVehiculo(vehiculo));
        verify(repositorioVehiculoMock, never()).guardarVehiculo(any(Vehiculo.class));
    }

    @Test
    void guardarVehiculo_PatenteDuplicada_LanzaExcepcion() {
        // Arrange
        String patente = "ABC123";
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setConductor(new Conductor());
        vehiculo.setPatente(patente);

        when(repositorioConductorMock.buscarPorId(anyLong())).thenReturn(Optional.of(new Conductor()));
        when(repositorioVehiculoMock.encontrarVehiculoConPatente(patente)).thenReturn(new Vehiculo());

        // Act & Assert
        assertThrows(PatenteDuplicadaException.class,
                () -> servicioVehiculo.guardarVehiculo(vehiculo));
        verify(repositorioVehiculoMock, never()).guardarVehiculo(any(Vehiculo.class));
    }

    @Test
    void obtenerVehiculosPorConductor() {
        // Arrange
        Long conductorId = 1L;
        List<Vehiculo> vehiculosEsperados = List.of(
                new Vehiculo(1L, null, "Toyota", "2020", "ABC123", 4, EstadoVerificacion.VERIFICADO),
                new Vehiculo(2L, null, "Honda", "2019", "XYZ789", 4, EstadoVerificacion.VERIFICADO)
        );
        when(repositorioVehiculoMock.obtenerVehiculosParaConductor(conductorId)).thenReturn(vehiculosEsperados);

        // Act
        List<Vehiculo> resultados = servicioVehiculo.obtenerVehiculosParaConductor(conductorId);

        // Assert
        assertNotNull(resultados);
        assertEquals(2, resultados.size());
        assertEquals(vehiculosEsperados, resultados);
        verify(repositorioVehiculoMock).obtenerVehiculosParaConductor(conductorId);
    }
}
