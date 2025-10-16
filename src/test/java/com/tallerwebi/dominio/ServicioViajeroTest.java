package com.tallerwebi.dominio;

import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.IRepository.RepositorioViajero;
import com.tallerwebi.dominio.IServicio.ServicioViajero;
import com.tallerwebi.dominio.ServiceImpl.ServicioViajeroImpl;
import com.tallerwebi.dominio.excepcion.CredencialesInvalidas;
import com.tallerwebi.dominio.excepcion.DatoObligatorioException;
import com.tallerwebi.dominio.excepcion.EdadInvalidaException;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ServicioViajeroTest {

    private RepositorioViajero repositorioMock;
    private ServicioViajero servicio;

    @BeforeEach
    void setUp() {
        repositorioMock = mock(RepositorioViajero.class);
        servicio = new ServicioViajeroImpl(repositorioMock);
    }

    @Test
    void deberiaValidarLoginCorrecto() throws CredencialesInvalidas {
        Viajero v = new Viajero(1L, "Lucas",25, "lucas@mail.com", "pass", new ArrayList<>());

        when(repositorioMock.buscarPorEmailYContrasenia(v.getEmail(), v.getContrasenia()))
                .thenReturn(Optional.of(v));

        Viajero resultado = servicio.login(v.getEmail(), v.getContrasenia());

        assertThat(resultado.getNombre(), equalTo(v.getNombre()));
    }

    @Test
    void noDeberiaValidarLoginSiCredencialesInvalidas() {
        when(repositorioMock.buscarPorEmailYContrasenia("lucas@mail.com", "pass"))
                .thenReturn(Optional.empty());

        assertThrows(CredencialesInvalidas.class,
                () -> servicio.login("lucas@mail.com", "pass"));
    }

    @Test
    void deberiaRegistrarViajeroSiNoExiste() throws UsuarioExistente, EdadInvalidaException, DatoObligatorioException {
        Viajero nuevo = new Viajero(null, "Ana", 30, "123", "ana@mail.com", new ArrayList<>());

        when(repositorioMock.buscarPorEmail(nuevo.getEmail()))
                .thenReturn(Optional.empty());

        servicio.registrar(nuevo);

        verify(repositorioMock, times(1)).guardarViajero(nuevo);
    }

    @Test
    void noDeberiaRegistrarSiUsuarioYaExiste() {
        Viajero existente = new Viajero(1L, "Ana", 30, "123", "ana@mail.com" , new ArrayList<>());

        when(repositorioMock.buscarPorEmail(existente.getEmail()))
                .thenReturn(Optional.of(existente));

        Viajero nuevo = new Viajero(null, "Ana", 30, "123", "ana@mail.com", new ArrayList<>());

        assertThrows(UsuarioExistente.class,
                () -> servicio.registrar(nuevo));

        verify(repositorioMock, never()).guardarViajero(any(Viajero.class));
    }

    @Test
    void noDeberiaRegistrarSiNombreEsNulo() {
        Viajero sinNombre = new Viajero(null, null, 25, "123", "ana@mail.com", new ArrayList<>());

        assertThrows(DatoObligatorioException.class,
                () -> servicio.registrar(sinNombre));

        verify(repositorioMock, never()).guardarViajero(any());
    }

    @Test
    void noDeberiaRegistrarSiNombreEsVacio() {
        Viajero sinNombre = new Viajero(null, "   ", 24, "123", "ana@mail.com", new ArrayList<>());

        assertThrows(DatoObligatorioException.class,
                () -> servicio.registrar(sinNombre));

        verify(repositorioMock, never()).guardarViajero(any());
    }

    @Test
    void noDeberiaRegistrarSiEdadEsNula() {
        Viajero sinEdad = new Viajero(null, "Ana", null, "123", "ana@mail.com", new ArrayList<>());

        assertThrows(EdadInvalidaException.class,
                () -> servicio.registrar(sinEdad));

        verify(repositorioMock, never()).guardarViajero(any());
    }

    @Test
    void noDeberiaRegistrarSiEdadEsMenorA18() {
        Viajero menor = new Viajero(null, "Ana", 17, "123", "ana@mail.com", new ArrayList<>());

        assertThrows(EdadInvalidaException.class,
                () -> servicio.registrar(menor));

        verify(repositorioMock, never()).guardarViajero(any());
    }

    @Test
    void noDeberiaRegistrarSiEdadEsMayorA120() {
        Viajero anciano = new Viajero(null, "Ana", 121, "123", "ana@mail.com", new ArrayList<>());

        assertThrows(EdadInvalidaException.class,
                () -> servicio.registrar(anciano));

        verify(repositorioMock, never()).guardarViajero(any());
    }

    @Test
    void obtenerViajero_existente_deberiaRetornarViajero() throws UsuarioInexistente {
        Long id = 1L;
        Viajero esperado = new Viajero(id, "Lucas", 27, "pass", "lucas@mail.com", new ArrayList<>());
        when(repositorioMock.buscarPorId(id)).thenReturn(Optional.of(esperado));

        Viajero resultado = servicio.obtenerViajero(id);

        assertThat(resultado, equalTo(esperado));
        verify(repositorioMock).buscarPorId(id);
    }

    @Test
    void obtenerViajero_noExistente_deberiaLanzarExcepcion() {
        Long id = 1L;
        when(repositorioMock.buscarPorId(id)).thenReturn(Optional.empty());

        assertThrows(UsuarioInexistente.class, () -> servicio.obtenerViajero(id));
        verify(repositorioMock).buscarPorId(id);
    }
}
