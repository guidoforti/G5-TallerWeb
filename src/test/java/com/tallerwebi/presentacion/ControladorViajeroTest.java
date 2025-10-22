package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.IServicio.ServicioViajero;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.presentacion.Controller.ControladorViajero;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

public class ControladorViajeroTest {

    private ControladorViajero controladorViajero;
    private ServicioViajero servicioViajeroMock;
    private HttpSession sessionMock;
    private Viajero viajeroMock;

    @BeforeEach
    public void init() {
        servicioViajeroMock = mock(ServicioViajero.class);
        controladorViajero = new ControladorViajero(servicioViajeroMock);
        sessionMock = mock(HttpSession.class);
        viajeroMock = mock(Viajero.class);

        when(viajeroMock.getId()).thenReturn(1L);
        when(viajeroMock.getNombre()).thenReturn("Juan");
    }

    // NOTA: Los tests de REGISTRO fueron movidos a ControladorRegistroTest
    // Se eliminan irARegistro(), registroCorrectoDeberiaRedirigirAHomeYSetearSesion(), etc.

    @Test
    void siUsuarioNoEstaEnSesionEnHomeDeberiaRedirigirALoginCentral() {
        // Arrange: No hay usuario ID, no hay ROL
        when(sessionMock.getAttribute("usuarioId")).thenReturn(null);
        when(sessionMock.getAttribute("rol")).thenReturn(null);

        // Act
        ModelAndView mav = controladorViajero.irAHome(sessionMock);

        // Assert
        // El ROL ya no es /viajero/login, es /login centralizado
        assertThat(mav.getViewName(), equalTo("redirect:/login")); 
        verify(sessionMock, times(1)).getAttribute("usuarioId");
    }
    
    @Test
    void siUsuarioNoEsViajeroDeberiaRedirigirALoginCentral() {
        // Arrange: ID existe, pero ROL no es VIAJERO
        when(sessionMock.getAttribute("usuarioId")).thenReturn(1L);
        when(sessionMock.getAttribute("rol")).thenReturn("CONDUCTOR"); // Rol incorrecto

        // Act
        ModelAndView mav = controladorViajero.irAHome(sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("redirect:/login"));
        verify(servicioViajeroMock, never()).obtenerViajero(anyLong());
    }


    @Test
    void siUsuarioEstaEnSesionYEsViajeroDeberiaMostrarHomeConNombre() throws UsuarioInexistente {
        // Arrange
        when(sessionMock.getAttribute("usuarioId")).thenReturn(1L);
        when(sessionMock.getAttribute("rol")).thenReturn("VIAJERO");
        when(servicioViajeroMock.obtenerViajero(1L)).thenReturn(viajeroMock);

        // Act
        ModelAndView mav = controladorViajero.irAHome(sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("homeViajero"));
        assertThat(mav.getModel().get("nombreConductor").toString(), equalTo(viajeroMock.getNombre())); // OJO: nombreConductor es un error del controlador, pero lo mantenemos por consistencia
        assertThat(mav.getModel().get("rol").toString(), equalTo("VIAJERO"));
        verify(servicioViajeroMock, times(1)).obtenerViajero(1L);
    }
    
    @Test
    void siUsuarioInexistenteEnSesionDeberiaInvalidarSesionYRedirigirALogin() throws UsuarioInexistente {
        // Arrange
        when(sessionMock.getAttribute("usuarioId")).thenReturn(99L);
        when(sessionMock.getAttribute("rol")).thenReturn("VIAJERO");
        
        doThrow(new UsuarioInexistente("Error de sesión"))
            .when(servicioViajeroMock).obtenerViajero(99L);

        // Act
        ModelAndView mav = controladorViajero.irAHome(sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("redirect:/login"));
        assertThat(mav.getModel().get("error").toString(), equalTo("Su sesión no es válida. Por favor, inicie sesión nuevamente."));
        verify(sessionMock, times(1)).invalidate();
    }
}