package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.IServicio.ServicioLogin;
import com.tallerwebi.presentacion.Controller.ControladorLogin;
import com.tallerwebi.presentacion.DTO.DatosLoginDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Optional;
import static org.hamcrest.CoreMatchers.instanceOf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.Mockito.*;

public class ControladorLoginTest {

    private ControladorLogin controladorLogin;
    private DatosLoginDTO datosLoginMock;
    private HttpServletRequest requestMock;
    private HttpSession sessionMock;
    private ServicioLogin servicioLoginMock;


    @BeforeEach
    public void init(){
        datosLoginMock = new DatosLoginDTO("dami@unlam.com", "123");
        // Configuramos los mocks de Request y Session
        requestMock = mock(HttpServletRequest.class);
        sessionMock = mock(HttpSession.class);
        when(requestMock.getSession()).thenReturn(sessionMock);

        servicioLoginMock = mock(ServicioLogin.class);
        controladorLogin = new ControladorLogin(servicioLoginMock);
    }

    // --- TESTS DE FLUJO BÁSICO ---

    @Test
    public void deberiaRetornarVistaLoginInicial() {
        // Act
        ModelAndView modelAndView = controladorLogin.irALogin();

        // Validacion
        assertThat(modelAndView.getViewName(), equalTo("login"));
        assertThat(modelAndView.getModel().get("datosLogin"), instanceOf(DatosLoginDTO.class));
    }

    @Test
    public void inicioDeberiaRedirigirALogin() {
        // Act
        ModelAndView modelAndView = controladorLogin.inicio();

        // Validacion
        assertThat(modelAndView.getViewName(), equalTo("redirect:/login"));
    }

    @Test
    public void irAHomeDeberiaRetornarVistaHomeGenerica() {
        // Act
        ModelAndView modelAndView = controladorLogin.irAHome();

        // Validacion
        assertThat(modelAndView.getViewName(), equalTo("home"));
    }

    @Test
    public void logoutDeberiaInvalidarSesionYRedirigirALogin() {
        // Act
        ModelAndView modelAndView = controladorLogin.logout(requestMock);

        // Validacion
        assertThat(modelAndView.getViewName(), equalTo("redirect:/login"));
        verify(sessionMock, times(1)).invalidate();
    }


    // --- TESTS DE VALIDAR LOGIN ---

    @Test
    public void loginConUsuarioYPasswordInorrectosDeberiaLlevarALoginNuevamente(){
        // preparacion
        when(servicioLoginMock.consultarUsuario(anyString(), anyString())).thenReturn(Optional.empty());

        // ejecucion
        ModelAndView modelAndView = controladorLogin.validarLogin(datosLoginMock, requestMock);

        // validacion
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("login"));
        assertThat(modelAndView.getModel().get("error").toString(), equalToIgnoringCase("Usuario o clave incorrecta"));
        verify(sessionMock, times(0)).setAttribute(anyString(), any());
    }

    @Test
    public void loginConUsuarioYPasswordCorrectosDeberiaLLevarAHomeDeConductor(){
        // preparacion
        Usuario usuarioEncontradoMock = mock(Usuario.class);
        when(usuarioEncontradoMock.getRol()).thenReturn("CONDUCTOR");
        when(usuarioEncontradoMock.getId()).thenReturn(5L);

        when(servicioLoginMock.consultarUsuario(anyString(), anyString())).thenReturn(Optional.of(usuarioEncontradoMock));

        // ejecucion
        ModelAndView modelAndView = controladorLogin.validarLogin(datosLoginMock, requestMock);

        // validacion
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/conductor/home"));
        verify(sessionMock, times(1)).setAttribute("idUsuario", 5L);
        verify(sessionMock, times(1)).setAttribute("ROL", "CONDUCTOR");
    }

    @Test
    public void loginConUsuarioYPasswordCorrectosDeberiaLLevarAHomeDeViajero(){
        // preparacion
        Usuario usuarioEncontradoMock = mock(Usuario.class);
        when(usuarioEncontradoMock.getRol()).thenReturn("VIAJERO");
        when(usuarioEncontradoMock.getId()).thenReturn(6L);

        when(servicioLoginMock.consultarUsuario(anyString(), anyString())).thenReturn(Optional.of(usuarioEncontradoMock));

        // ejecucion
        ModelAndView modelAndView = controladorLogin.validarLogin(datosLoginMock, requestMock);

        // validacion
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/viajero/home"));
        verify(sessionMock, times(1)).setAttribute("idUsuario", 6L);
        verify(sessionMock, times(1)).setAttribute("ROL", "VIAJERO");
    }

    @Test
    public void loginConRolDesconocidoDeberiaLLevarAHomeGenerico() {
        // preparacion
        Usuario usuarioEncontradoMock = mock(Usuario.class);
        when(usuarioEncontradoMock.getRol()).thenReturn("ADMIN"); // Rol desconocido
        when(usuarioEncontradoMock.getId()).thenReturn(7L);

        when(servicioLoginMock.consultarUsuario(anyString(), anyString())).thenReturn(Optional.of(usuarioEncontradoMock));

        // ejecucion
        ModelAndView modelAndView = controladorLogin.validarLogin(datosLoginMock, requestMock);

        // validacion
        // Verifica el fallback a /home (ruta genérica)
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
        verify(sessionMock, times(1)).setAttribute("idUsuario", 7L);
        verify(sessionMock, times(1)).setAttribute("ROL", "ADMIN");
    }
}