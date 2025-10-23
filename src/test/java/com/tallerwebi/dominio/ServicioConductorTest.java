package com.tallerwebi.dominio;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.IRepository.RepositorioConductor;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import com.tallerwebi.dominio.IServicio.ServicioLogin;
import com.tallerwebi.dominio.ServiceImpl.ServicioConductorImpl;
import com.tallerwebi.dominio.excepcion.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

class ServicioConductorTest {

    private RepositorioConductor repositorioMock;
    private ServicioConductor servicio;
    private ServicioLogin servicioLoginMock;

    @BeforeEach
    void setUp() {
        repositorioMock = mock(RepositorioConductor.class);
        servicioLoginMock = mock(ServicioLogin.class);
        servicio = new ServicioConductorImpl(repositorioMock, servicioLoginMock);
    }


    // 1. Cobertura: registrar() - Éxito
    @Test
    void deberiaRegistrarConductorSiNoExiste() throws UsuarioExistente, FechaDeVencimientoDeLicenciaInvalida {
        // Arrange
        Conductor nuevo = new Conductor();
        nuevo.setNombre("Ana");
        nuevo.setEmail("ana@mail.com");
        nuevo.setContrasenia("123");
        // Licencia futura
        nuevo.setFechaDeVencimientoLicencia(LocalDate.now().plusDays(10));

        // Mock: ServicioLogin no lanza excepción (email es nuevo)
        doNothing().when(servicioLoginMock).registrar(any(Conductor.class));

        // Act
        servicio.registrar(nuevo);

        // Assert
        verify(servicioLoginMock, times(1)).registrar(nuevo);
        assertThat(nuevo.getRol(), equalTo("CONDUCTOR"));
        assertThat(nuevo.getActivo(), equalTo(true));
    }

    // 2. Cobertura: registrar() - Usuario Existente (Lanzado por ServicioLogin)
    @Test
    void noDeberiaRegistrarSiUsuarioYaExiste() throws UsuarioExistente {
        // Arrange
        Conductor nuevo = new Conductor();
        nuevo.setNombre("Ana");
        nuevo.setEmail("ana@mail.com");
        nuevo.setContrasenia("123");
        nuevo.setFechaDeVencimientoLicencia(LocalDate.now().plusDays(10));

        // Mock: ServicioLogin lanza la excepción (simulando que el email ya existe)
        doThrow(new UsuarioExistente("Ya existe un usuario con ese email"))
                .when(servicioLoginMock).registrar(any(Conductor.class));

        // Act & Assert
        assertThrows(UsuarioExistente.class,
                () -> servicio.registrar(nuevo));

        // Verificamos que el Repositorio de Rol NUNCA fue consultado
        verify(repositorioMock, never()).buscarPorId(anyLong());
    }

    // 3. Cobertura: registrar() - Licencia Vencida (Ruta condicional IF)
    @Test
    void noDeberiaRegistrarConductorSiLicenciaEstaVencida() throws UsuarioExistente{
        // Arrange
        Conductor vencido = new Conductor();
        vencido.setNombre("Carlos");
        vencido.setEmail("carlos@mail.com");
        vencido.setContrasenia("1234");
        // Licencia pasada
        vencido.setFechaDeVencimientoLicencia(LocalDate.now().minusDays(1));

        // Act & Assert
        FechaDeVencimientoDeLicenciaInvalida exception = assertThrows(
                FechaDeVencimientoDeLicenciaInvalida.class,
                () -> servicio.registrar(vencido)
        );

        assertThat(exception.getMessage(), equalTo("La fecha de vencimiento de la licencia debe ser mayor a la actual"));
        // Verificamos que el ServicioLogin NUNCA fue llamado
        verify(servicioLoginMock, never()).registrar(any());
    }

    // 4. Cobertura: obtenerConductor() - Éxito
    @Test
    void obtenerConductor_existente_deberiaRetornarConductor() throws UsuarioInexistente {
        // Arrange
        Long id = 1L;
        Conductor esperado = new Conductor();
        esperado.setId(id);

        // Mock: Repositorio devuelve Optional con el Conductor
        when(repositorioMock.buscarPorId(id)).thenReturn(Optional.of(esperado));

        // Act
        Conductor resultado = servicio.obtenerConductor(id);

        // Assert
        assertThat(resultado, equalTo(esperado));
        verify(repositorioMock).buscarPorId(id);
    }

    // 5. Cobertura: obtenerConductor() - Conductor Inexistente (Ruta orElseThrow)
    @Test
    void obtenerConductor_noExistente_deberiaLanzarExcepcion() {
        // Arrange
        Long id = 1L;
        // Mock: Repositorio devuelve Optional vacío
        when(repositorioMock.buscarPorId(id)).thenReturn(Optional.empty());

        // Act & Assert
        // Verificamos que se lanza la excepción correcta
        assertThrows(UsuarioInexistente.class, () -> servicio.obtenerConductor(id));
        verify(repositorioMock).buscarPorId(id);
    }
}