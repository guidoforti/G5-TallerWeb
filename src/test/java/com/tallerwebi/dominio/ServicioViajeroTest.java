package com.tallerwebi.dominio;

import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.IRepository.RepositorioViajero;
import com.tallerwebi.dominio.IServicio.ServicioLogin; // Usado para el registro centralizado
import com.tallerwebi.dominio.IServicio.ServicioViajero;
import com.tallerwebi.dominio.ServiceImpl.ServicioViajeroImpl;
import com.tallerwebi.dominio.excepcion.DatoObligatorioException;
import com.tallerwebi.dominio.excepcion.EdadInvalidaException;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
    private ServicioLogin servicioLoginMock; // Renombrado a servicioLoginMock para claridad

    @BeforeEach
    void setUp() {
        repositorioMock = mock(RepositorioViajero.class);
        servicioLoginMock = Mockito.mock(ServicioLogin.class); // Mock para el servicio centralizado
        servicio = new ServicioViajeroImpl(repositorioMock, servicioLoginMock);
    }


    @Test
    void deberiaRegistrarViajeroSiNoExiste() throws UsuarioExistente, EdadInvalidaException, DatoObligatorioException {
        // Arrange
        Viajero v = new Viajero();
        v.setId(null);
        v.setNombre("Ana");
        v.setEdad(30);
        v.setEmail("ana@mail.com");
        v.setContrasenia("123");
        v.setReservas(new ArrayList<>());
        // El rol y activo se setean DENTRO del servicio

        // Simulamos que el ServicioLogin NO lanza UsuarioExistente, lo que implica registro exitoso
        doNothing().when(servicioLoginMock).registrar(any(Viajero.class));

        // Act
        servicio.registrar(v);

        // Assert
        // Verificamos que se llama al método centralizado de registro
        verify(servicioLoginMock, times(1)).registrar(v);
        // Verificamos que se asignaron los campos de Usuario dentro del servicio
        assertThat(v.getRol(), equalTo("VIAJERO"));
        assertThat(v.getActivo(), equalTo(true));
    }

    @Test
    void noDeberiaRegistrarSiUsuarioYaExiste() throws UsuarioExistente {
        // Arrange
        Viajero nuevo = new Viajero();
        nuevo.setId(null);
        nuevo.setNombre("Ana");
        nuevo.setEdad(30);
        nuevo.setEmail("ana@mail.com");
        nuevo.setContrasenia("123");
        nuevo.setReservas(new ArrayList<>());

        // El ServicioLogin debe lanzar la excepción para simular que el email ya existe
        doThrow(new UsuarioExistente("Ya existe un usuario con ese email"))
                .when(servicioLoginMock).registrar(any(Viajero.class));

        // Act & Assert
        assertThrows(UsuarioExistente.class,
                () -> servicio.registrar(nuevo));

        // Verificamos que el repositorio de rol NO fue llamado
        verify(repositorioMock, never()).buscarPorId(anyLong());
    }

    // --- Los tests de validación de negocio (Nombre, Edad) siguen siendo válidos ---
    // Ya que la validación ocurre ANTES de llamar a servicioLoginMock.registrar()

    @Test
    void noDeberiaRegistrarSiNombreEsNulo() throws UsuarioExistente {
        // ... (el código del test no cambia)
        Viajero sinNombre = new Viajero();
        sinNombre.setNombre(null);
        sinNombre.setEdad(25);
        // ... (otros campos)

        assertThrows(DatoObligatorioException.class,
                () -> servicio.registrar(sinNombre));

        // Verificamos que NUNCA se llama al servicio de login
        verify(servicioLoginMock, never()).registrar(any());
    }

    @Test
    void noDeberiaRegistrarSiNombreEsVacio() throws UsuarioExistente {
        // ... (el código del test no cambia)
        Viajero sinNombre = new Viajero();
        sinNombre.setNombre(" ");
        sinNombre.setEdad(25);
        // ... (otros campos)

        assertThrows(DatoObligatorioException.class,
                () -> servicio.registrar(sinNombre));

        verify(servicioLoginMock, never()).registrar(any());
    }

    @Test
    void noDeberiaRegistrarSiEdadEsNula() throws UsuarioExistente {
        // ... (el código del test no cambia)
        Viajero sinEdad = new Viajero();
        sinEdad.setNombre("Ana");
        sinEdad.setEdad(null);

        assertThrows(EdadInvalidaException.class,
                () -> servicio.registrar(sinEdad));

        verify(servicioLoginMock, never()).registrar(any());
    }

    @Test
    void noDeberiaRegistrarSiEdadEsMenorA18() throws UsuarioExistente {
        // ... (el código del test no cambia)
        Viajero menor = new Viajero();
        menor.setNombre("Ana");
        menor.setEdad(17);

        assertThrows(EdadInvalidaException.class,
                () -> servicio.registrar(menor));

        verify(servicioLoginMock, never()).registrar(any());
    }

    @Test
    void noDeberiaRegistrarSiEdadEsMayorA120() throws UsuarioExistente {
        // ... (el código del test no cambia)
        Viajero anciano = new Viajero();
        anciano.setNombre("Ana");
        anciano.setEdad(121);

        assertThrows(EdadInvalidaException.class,
                () -> servicio.registrar(anciano));

        verify(servicioLoginMock, never()).registrar(any());
    }

    // --- Los tests de obtenerViajero (Buscar por ID) siguen siendo válidos ---
    // Ya que RepositorioViajero SÍ mantiene el método buscarPorId(Long id)

    @Test
    void obtenerViajero_existente_deberiaRetornarViajero() throws UsuarioInexistente {
        Long id = 1L;
        Viajero esperado = new Viajero();
        // ... (setup de esperado)
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