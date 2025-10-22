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
        Viajero v = new Viajero();
        v.setId(1L);
        v.setNombre("Lucas");
        v.setEdad(25);
        v.setEmail("lucas@mail.com");
        v.setContrasenia("pass");
        v.setViajes(new ArrayList<>());
        v.setRol("VIAJERO");
        v.setActivo(true);

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

        Viajero v = new Viajero();
        v.setId(null);
        v.setNombre("Ana");
        v.setEdad(30);
        v.setEmail("ana@mail.com");
        v.setContrasenia("123");
        v.setViajes(new ArrayList<>());
        v.setRol("VIAJERO");
        v.setActivo(true);

        when(repositorioMock.buscarPorEmail(v.getEmail()))
                .thenReturn(Optional.empty());

        servicio.registrar(v);

        verify(repositorioMock, times(1)).guardarViajero(v);
    }

    @Test
    void noDeberiaRegistrarSiUsuarioYaExiste() {
        Viajero existente = new Viajero();
        existente.setId(1L);
        existente.setNombre("Ana");
        existente.setEdad(30);
        existente.setEmail("ana@mail.com");
        existente.setContrasenia("123");
        existente.setViajes(new ArrayList<>());
        existente.setRol("VIAJERO");
        existente.setActivo(true);
        when(repositorioMock.buscarPorEmail(existente.getEmail()))
                .thenReturn(Optional.of(existente));

        Viajero nuevo = new Viajero();
        nuevo.setId(null);
        nuevo.setNombre("Ana");
        nuevo.setEdad(30);
        nuevo.setEmail("ana@mail.com");
        nuevo.setContrasenia("123");
        nuevo.setViajes(new ArrayList<>());
        nuevo.setRol("VIAJERO");
        nuevo.setActivo(true);

        assertThrows(UsuarioExistente.class,
                () -> servicio.registrar(nuevo));

        verify(repositorioMock, never()).guardarViajero(any(Viajero.class));
    }

    @Test
    void noDeberiaRegistrarSiNombreEsNulo() {
        Viajero sinNombre = new Viajero();
        sinNombre.setId(null);
        sinNombre.setNombre(null);
        sinNombre.setEdad(25);
        sinNombre.setContrasenia("123");
        sinNombre.setEmail("ana@mail.com");
        sinNombre.setViajes(new ArrayList<>());
        sinNombre.setRol("VIAJERO");
        sinNombre.setActivo(true);

        assertThrows(DatoObligatorioException.class,
                () -> servicio.registrar(sinNombre));

        verify(repositorioMock, never()).guardarViajero(any());
    }

    @Test
    void noDeberiaRegistrarSiNombreEsVacio() {
        Viajero sinNombre = new Viajero();
        sinNombre.setId(null);
        sinNombre.setNombre(" ");
        sinNombre.setEdad(25);
        sinNombre.setContrasenia("123");
        sinNombre.setEmail("ana@mail.com");
        sinNombre.setViajes(new ArrayList<>());
        sinNombre.setRol("VIAJERO");
        sinNombre.setActivo(true);

        assertThrows(DatoObligatorioException.class,
                () -> servicio.registrar(sinNombre));

        verify(repositorioMock, never()).guardarViajero(any());
    }

    @Test
    void noDeberiaRegistrarSiEdadEsNula() {
        Viajero sinEdad = new Viajero();
        sinEdad.setId(null);
        sinEdad.setNombre("Ana");
        sinEdad.setEdad(null);
        sinEdad.setEmail("ana@mail.com");
        sinEdad.setContrasenia("123");
        sinEdad.setViajes(new ArrayList<>());
        sinEdad.setRol("VIAJERO");
        sinEdad.setActivo(true);

        assertThrows(EdadInvalidaException.class,
                () -> servicio.registrar(sinEdad));

        verify(repositorioMock, never()).guardarViajero(any());
    }

    @Test
    void noDeberiaRegistrarSiEdadEsMenorA18() {
        Viajero menor = new Viajero();
        menor.setId(null);
        menor.setNombre("Ana");
        menor.setEdad(17);
        menor.setEmail("ana@mail.com");
        menor.setContrasenia("123");
        menor.setViajes(new ArrayList<>());
        menor.setRol("VIAJERO");
        menor.setActivo(true);

        assertThrows(EdadInvalidaException.class,
                () -> servicio.registrar(menor));

        verify(repositorioMock, never()).guardarViajero(any());
    }

    @Test
    void noDeberiaRegistrarSiEdadEsMayorA120() {
        Viajero anciano = new Viajero();
        anciano.setId(null);
        anciano.setNombre("Ana");
        anciano.setEdad(121);
        anciano.setEmail("ana@mail.com");
        anciano.setContrasenia("123");
        anciano.setViajes(new ArrayList<>());
        anciano.setRol("VIAJERO");
        anciano.setActivo(true);

        assertThrows(EdadInvalidaException.class,
                () -> servicio.registrar(anciano));

        verify(repositorioMock, never()).guardarViajero(any());
    }

    @Test
    void obtenerViajero_existente_deberiaRetornarViajero() throws UsuarioInexistente {
        Long id = 1L;
        Viajero esperado = new Viajero();
        esperado.setId(id);
        esperado.setNombre("Lucas");
        esperado.setEdad(27);
        esperado.setContrasenia("pass");
        esperado.setEmail("lucas@mail.com");
        esperado.setViajes(new ArrayList<>());
        esperado.setRol("VIAJERO");
        esperado.setActivo(true);
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
