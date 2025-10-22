package com.tallerwebi.dominio;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.IRepository.RepositorioConductor;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import com.tallerwebi.dominio.ServiceImpl.ServicioConductorImpl;
import com.tallerwebi.dominio.excepcion.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ServicioConductorTest {

    private RepositorioConductor repositorioMock;
    private ServicioConductor servicio;

    @BeforeEach
    void setUp() {
        repositorioMock = Mockito.mock(RepositorioConductor.class);
        servicio = new ServicioConductorImpl(repositorioMock);
    }

    @Test
    void deberiaValidarLoginCorrecto() throws Exception {
        // Arrange
        Conductor c = new Conductor();
        c.setId(1L);
        c.setNombre("Pedro");
        c.setEmail("pedro@mail.com");
        c.setContrasenia("pass");
        c.setRol("CONDUCTOR");
        c.setActivo(true);
        c.setFechaDeVencimientoLicencia(LocalDate.now());

        when(repositorioMock.buscarPorEmailYContrasenia(c.getEmail(), c.getContrasenia()))
                .thenReturn(Optional.of(c));

        // Act
        Conductor resultado = servicio.login(c.getEmail(), c.getContrasenia());

        // Assert
        assertThat(resultado.getNombre(), equalTo(c.getNombre()));
    }

    @Test
    void noDeberiaValidarLoginSiCredencialesInvalidas() {
        // Arrange
        when(repositorioMock.buscarPorEmailYContrasenia("pedro@mail.com", "pass"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CredencialesInvalidas.class,
                () -> servicio.login("pedro@mail.com", "pass"));
    }

    @Test
    void deberiaRegistrarConductorSiNoExiste() throws UsuarioExistente, FechaDeVencimientoDeLicenciaInvalida {
        // Arrange
        Conductor nuevo = new Conductor();
        nuevo.setNombre("Ana");
        nuevo.setEmail("ana@mail.com");
        nuevo.setContrasenia("123");
        nuevo.setRol("CONDUCTOR");
        nuevo.setActivo(true);
        nuevo.setFechaDeVencimientoLicencia(LocalDate.now().plusDays(10));

        when(repositorioMock.buscarPorEmail(nuevo.getEmail()))
                .thenReturn(Optional.empty());

        // Act
        servicio.registrar(nuevo);

        // Assert
        verify(repositorioMock, times(1)).guardarConductor(nuevo);
    }

    @Test
    void noDeberiaRegistrarSiUsuarioYaExiste() {
        // Arrange
        Conductor existente = new Conductor();
        existente.setId(1L);
        existente.setNombre("Ana");
        existente.setEmail("ana@mail.com");
        existente.setContrasenia("123");
        existente.setFechaDeVencimientoLicencia(LocalDate.now());

        when(repositorioMock.buscarPorEmail(existente.getEmail()))
                .thenReturn(Optional.of(existente));

        Conductor nuevo = new Conductor();
        nuevo.setNombre("Ana");
        nuevo.setEmail("ana@mail.com");
        nuevo.setContrasenia("123");
        nuevo.setFechaDeVencimientoLicencia(LocalDate.now());

        // Act & Assert
        assertThrows(UsuarioExistente.class,
                () -> servicio.registrar(nuevo));

        verify(repositorioMock, never()).guardarConductor(any(Conductor.class));
    }

    @Test
    void noDeberiaRegistrarConductorSiLicenciaEstaVencida() {
        // Arrange
        Conductor vencido = new Conductor();
        vencido.setNombre("Carlos");
        vencido.setEmail("carlos@mail.com");
        vencido.setContrasenia("1234");
        vencido.setFechaDeVencimientoLicencia(LocalDate.now().minusDays(1));

        // Act & Assert
        FechaDeVencimientoDeLicenciaInvalida exception = assertThrows(
                FechaDeVencimientoDeLicenciaInvalida.class,
                () -> servicio.registrar(vencido)
        );

        assertThat(exception.getMessage(), equalTo("La fecha de vencimiento de la licencia debe ser mayor a la actual"));
        verify(repositorioMock, times(0)).guardarConductor(any());
    }

    @Test
    void obtenerConductor_existente_deberiaRetornarConductor() throws UsuarioInexistente {
        // Arrange
        Long id = 1L;
        Conductor esperado = new Conductor();
        esperado.setId(id);
        esperado.setNombre("Pedro");
        esperado.setEmail("pedro@mail.com");
        esperado.setContrasenia("pass");
        esperado.setFechaDeVencimientoLicencia(LocalDate.now());

        when(repositorioMock.buscarPorId(id)).thenReturn(Optional.of(esperado));

        // Act
        Conductor resultado = servicio.obtenerConductor(id);

        // Assert
        assertThat(resultado, equalTo(esperado));
        verify(repositorioMock).buscarPorId(id);
    }

    @Test
    void obtenerConductor_noExistente_deberiaLanzarExcepcion() {
        // Arrange
        Long id = 1L;
        when(repositorioMock.buscarPorId(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsuarioInexistente.class, () -> servicio.obtenerConductor(id));
        verify(repositorioMock).buscarPorId(id);
    }

    @Test
    void guardarConductor_ErrorAlGuardar_DeberiaLanzarExcepcion() {
        // Arrange
        Conductor c = new Conductor();
        c.setNombre("Luis");
        c.setEmail("luis@gmail.com");
        c.setContrasenia("1234");
        c.setFechaDeVencimientoLicencia(LocalDate.now().plusDays(10));

        doThrow(new RuntimeException("DB error")).when(repositorioMock).guardarConductor(c);

        // Act & Assert
        ErrorAlGuardarConductorException exception = assertThrows(
                ErrorAlGuardarConductorException.class,
                () -> servicio.guardarConductor(c)
        );

        assertThat(exception.getMessage(), equalTo("Error al guardar el conductor en la base de datos: DB error"));
    }

    @Test
    void guardarConductorCorrectamenteDeberiaRetornarConductor() throws ErrorAlGuardarConductorException {
        // Arrange
        Conductor c = new Conductor();
        c.setNombre("Luis");
        c.setEmail("luis@gmail.com");
        c.setContrasenia("1234");
        c.setFechaDeVencimientoLicencia(LocalDate.now().plusDays(10));

        // Act
        servicio.guardarConductor(c);

        // Assert
        verify(repositorioMock, times(1)).guardarConductor(c);
    }

}
