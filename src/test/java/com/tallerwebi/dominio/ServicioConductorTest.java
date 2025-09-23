package com.tallerwebi.dominio;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.IRepository.RepositorioConductor;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import com.tallerwebi.dominio.ServiceImpl.ServicioConductorImpl;
import com.tallerwebi.dominio.excepcion.CredencialesInvalidas;
import com.tallerwebi.dominio.excepcion.FechaDeVencimientoDeLicenciaInvalida;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServicioConductorTest {

    private RepositorioConductor repositorioMock;
    private ServicioConductor servicio;

    @BeforeEach
    void setUp() {
        repositorioMock = Mockito.mock(RepositorioConductor.class);
        servicio = new ServicioConductorImpl(repositorioMock);
    }

    @Test
    void deberiaValidarLoginCorrecto() throws CredencialesInvalidas {
        Conductor c = new Conductor(1L, null, "Pedro", "pedro@mail.com", "pass", LocalDate.now());

        when(repositorioMock.buscarPorEmailYContrasenia("pedro@mail.com", "pass"))
                .thenReturn(Optional.of(c));

        Conductor resultado = servicio.login("pedro@mail.com", "pass");

        assertThat(resultado.getNombre(), equalTo("Pedro"));
    }

    @Test
    void noDeberiaValidarLoginSiCredencialesInvalidas() {
        when(repositorioMock.buscarPorEmailYContrasenia("pedro@mail.com", "mal"))
                .thenReturn(Optional.empty());

        assertThrows(CredencialesInvalidas.class,
                () -> servicio.login("pedro@mail.com", "mal"));
    }

    @Test
    void deberiaRegistrarConductorSiNoExiste() throws UsuarioExistente, FechaDeVencimientoDeLicenciaInvalida {
        Conductor nuevo = new Conductor(null, null, "Ana", "ana@mail.com", "123", LocalDate.now());

        when(repositorioMock.buscarPorEmail("ana@mail.com"))
                .thenReturn(Optional.empty()); // no existe todavía

        servicio.registrar(nuevo);

        // verificamos que el repositorio guardó al conductor
        verify(repositorioMock, times(1)).guardar(nuevo);
    }

    @Test
    void noDeberiaRegistrarSiUsuarioYaExiste() {
        Conductor existente = new Conductor(1L, null, "Ana", "ana@mail.com", "123", LocalDate.now());

        when(repositorioMock.buscarPorEmail("ana@mail.com"))
                .thenReturn(Optional.of(existente)); // ya existe

        Conductor nuevo = new Conductor(null, null, "Ana", "ana@mail.com", "123", LocalDate.now());

        assertThrows(UsuarioExistente.class,
                () -> servicio.registrar(nuevo));

        // nunca debería intentar guardar porque ya existe
        verify(repositorioMock, never()).guardar(any(Conductor.class));
    }

    @Test
    void noDeberiaRegistrarConductorSiLicenciaEstaVencida() {
        // dado
        Conductor vencido = new Conductor(
                1L, null,
                "Carlos", "carlos@mail.com",
                "1234",
                LocalDate.now().minusDays(1) // licencia vencida ayer
        );

        // cuando - entonces
        FechaDeVencimientoDeLicenciaInvalida exception = assertThrows(
                FechaDeVencimientoDeLicenciaInvalida.class,
                () -> servicio.registrar(vencido)
        );

        assertThat(exception.getMessage(), equalTo("La fecha de vencimiento de la licencia debe ser mayor a la actual"));
        verify(repositorioMock, times(0)).guardar(any()); // nunca debería guardar
    }
}