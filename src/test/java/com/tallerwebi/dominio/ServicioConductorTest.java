package com.tallerwebi.dominio;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.IRepository.RepositorioConductor;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import com.tallerwebi.dominio.ServiceImpl.ServicioConductorImpl;
import com.tallerwebi.dominio.excepcion.CredencialesInvalidas;
import com.tallerwebi.dominio.excepcion.FechaDeVencimientoDeLicenciaInvalida;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
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
        Conductor c = new Conductor(1L,  "Pedro", "pedro@mail.com", "pass", LocalDate.now() ,new ArrayList<>(), new ArrayList<>()  );

        when(repositorioMock.buscarPorEmailYContrasenia(c.getEmail(), c.getContrasenia()))
                .thenReturn(Optional.of(c));

        Conductor resultado = servicio.login(c.getEmail(), c.getContrasenia());

        assertThat(resultado.getNombre(), equalTo(c.getNombre()));
    }

    @Test
    void noDeberiaValidarLoginSiCredencialesInvalidas() throws Exception {
        when(repositorioMock.buscarPorEmailYContrasenia("pedro@mail.com", "pass"))
                .thenReturn(Optional.empty());

        assertThrows(CredencialesInvalidas.class,
                () -> servicio.login("pedro@mail.com", "pass"));
    }

    @Test
    void deberiaRegistrarConductorSiNoExiste() throws UsuarioExistente, FechaDeVencimientoDeLicenciaInvalida {
        Conductor nuevo = new Conductor(null, "Ana", "ana@mail.com", "123", LocalDate.now().plusDays(10) , new ArrayList<>(),new ArrayList<>());

        when(repositorioMock.buscarPorEmail(nuevo.getEmail()))
                .thenReturn(Optional.empty());

        servicio.registrar(nuevo);

        verify(repositorioMock, times(1)).guardar(nuevo);
    }

    @Test
    void noDeberiaRegistrarSiUsuarioYaExiste() {
        Conductor existente = new Conductor(1L,  "Ana", "ana@mail.com", "123", LocalDate.now() ,new ArrayList<>(),new ArrayList<>());

        when(repositorioMock.buscarPorEmail(existente.getEmail()))
                .thenReturn(Optional.of(existente));

        Conductor nuevo = new Conductor(null,  "Ana", "ana@mail.com", "123", LocalDate.now(), new ArrayList<>(),new ArrayList<>());

        assertThrows(UsuarioExistente.class,
                () -> servicio.registrar(nuevo));

        verify(repositorioMock, never()).guardar(any(Conductor.class));
    }

    @Test
    void noDeberiaRegistrarConductorSiLicenciaEstaVencida() {
        Conductor vencido = new Conductor(
                null,
                "Carlos", "carlos@mail.com",
                "1234",
                LocalDate.now().minusDays(1) , new ArrayList<>() ,new ArrayList<>()
        );

        FechaDeVencimientoDeLicenciaInvalida exception = assertThrows(
                FechaDeVencimientoDeLicenciaInvalida.class,
                () -> servicio.registrar(vencido)
        );

        assertThat(exception.getMessage(), equalTo("La fecha de vencimiento de la licencia debe ser mayor a la actual"));
        verify(repositorioMock, times(0)).guardar(any());
    }

    @Test
    void obtenerConductor_existente_deberiaRetornarConductor() throws UsuarioInexistente {
        // given
        Long id = 1L;
        Conductor esperado = new Conductor(id,  "Pedro", "pedro@mail.com", "pass", LocalDate.now(), new ArrayList<>(),new ArrayList<>());
        when(repositorioMock.buscarPorId(id)).thenReturn(Optional.of(esperado));

        // when
        Conductor resultado = servicio.obtenerConductor(id);

        // then
        assertThat(resultado, equalTo(esperado));
        verify(repositorioMock).buscarPorId(id);
    }

    @Test
    void obtenerConductor_noExistente_deberiaLanzarExcepcion() {
        // given
        Long id = 1L;
        when(repositorioMock.buscarPorId(id)).thenReturn(Optional.empty());

        // when & then
        assertThrows(UsuarioInexistente.class, () -> servicio.obtenerConductor(id));
        verify(repositorioMock).buscarPorId(id);
    }
}
