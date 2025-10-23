package com.tallerwebi.dominio;

import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.IRepository.RepositorioUsuario;
import com.tallerwebi.dominio.IServicio.ServicioLogin;
import com.tallerwebi.dominio.ServiceImpl.ServicioLoginImpl;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import static org.mockito.Mockito.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServicioLoginTest {

    private RepositorioUsuario repositorioUsuarioMock;
    private ServicioLogin servicioLogin;

    @BeforeEach
    void setUp() {
        repositorioUsuarioMock = mock(RepositorioUsuario.class);
        servicioLogin = new ServicioLoginImpl(repositorioUsuarioMock);
    }

    // --- TESTS DE CONSULTAR USUARIO (LOGIN) ---

    @Test
    void consultarUsuario_credencialesCorrectas_deberiaRetornarUsuario() {
        // Arrange
        String email = "test@mail.com";
        String password = "pass";
        Usuario usuarioEsperado = new Usuario() {};

        when(repositorioUsuarioMock.buscarUsuario(email, password))
                .thenReturn(Optional.of(usuarioEsperado));

        // Act
        Optional<Usuario> resultado = servicioLogin.consultarUsuario(email, password);

        // Assert
        assertThat(resultado.isPresent(), is(true));
        assertThat(resultado.get(), is(usuarioEsperado));
        verify(repositorioUsuarioMock, times(1)).buscarUsuario(email, password);
    }

    @Test
    void consultarUsuario_credencialesIncorrectas_deberiaRetornarOptionalVacio() {
        // Arrange
        String email = "test@mail.com";
        String password = "wrong";

        when(repositorioUsuarioMock.buscarUsuario(email, password))
                .thenReturn(Optional.empty());

        // Act
        Optional<Usuario> resultado = servicioLogin.consultarUsuario(email, password);

        // Assert
        assertThat(resultado.isEmpty(), is(true));
        verify(repositorioUsuarioMock, times(1)).buscarUsuario(email, password);
    }

    // --- TESTS DE REGISTRAR (GUARDAR) ---

    @Test
    void registrar_usuarioNuevo_deberiaGuardarloYNoLanzarExcepcion() throws UsuarioExistente {
        // Arrange
        Usuario nuevoUsuario = new Usuario() {};
        nuevoUsuario.setEmail("nuevo@mail.com");

        // Simula que el usuario NO existe
        when(repositorioUsuarioMock.buscarPorEmail(nuevoUsuario.getEmail()))
                .thenReturn(Optional.empty());

        // Act
        servicioLogin.registrar(nuevoUsuario);

        // Assert
        // Verifica que se llamó a buscarPorEmail y luego a guardar
        verify(repositorioUsuarioMock, times(1)).buscarPorEmail(nuevoUsuario.getEmail());
        verify(repositorioUsuarioMock, times(1)).guardar(nuevoUsuario);
    }

    @Test
    void registrar_usuarioExistente_deberiaLanzarUsuarioExistenteYNoGuardar() {
        // Arrange
        Usuario usuarioExistente = new Usuario() {};
        usuarioExistente.setEmail("existente@mail.com");

        // Simula que el usuario SÍ existe
        when(repositorioUsuarioMock.buscarPorEmail(usuarioExistente.getEmail()))
                .thenReturn(Optional.of(usuarioExistente));

        // Act & Assert
        UsuarioExistente exception = assertThrows(
                UsuarioExistente.class,
                () -> servicioLogin.registrar(usuarioExistente)
        );

        assertThat(exception.getMessage(), equalTo("Ya existe un usuario con ese email"));
        // Verifica que se llamó a buscarPorEmail, pero NO a guardar
        verify(repositorioUsuarioMock, times(1)).buscarPorEmail(usuarioExistente.getEmail());
        verify(repositorioUsuarioMock, never()).guardar(any(Usuario.class));
    }
}