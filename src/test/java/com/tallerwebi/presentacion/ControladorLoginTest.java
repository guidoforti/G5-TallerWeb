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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.Mockito.*;

public class ControladorLoginTest {

    private ControladorLogin controladorLogin;
    private Usuario usuarioMock;
    private DatosLoginDTO datosLoginMock;
    private HttpServletRequest requestMock;
    private HttpSession sessionMock;
    private ServicioLogin servicioLoginMock;


    @BeforeEach
    public void init(){
        datosLoginMock = new DatosLoginDTO("dami@unlam.com", "123");
        usuarioMock = mock(Usuario.class);
        when(usuarioMock.getEmail()).thenReturn("dami@unlam.com");
        requestMock = mock(HttpServletRequest.class);
        sessionMock = mock(HttpSession.class);
        when(requestMock.getSession()).thenReturn(sessionMock); // Configuramos la sesión en el request
        servicioLoginMock = mock(ServicioLogin.class);
        controladorLogin = new ControladorLogin(servicioLoginMock);
    }

    @Test
    public void loginConUsuarioYPasswordInorrectosDeberiaLlevarALoginNuevamente(){
        // preparacion
        // El servicio debe devolver un Optional vacío
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

        // El servicio debe devolver un Optional presente
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

    // NOTA: Los tests de REGISTRO fueron movidos a ControladorRegistroTest.
    // Eliminamos los tests: registrameSiUsuarioNoExisteDeberiaCrearUsuarioYVolverAlLogin,
    // registrarmeSiUsuarioExisteDeberiaVolverAFormularioYMostrarError, errorEnRegistrarmeDeberiaVolverAFormularioYMostrarError
}