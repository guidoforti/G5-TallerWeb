package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.presentacion.Controller.ControladorConductor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

public class ControladorConductorTest {

    private ControladorConductor controladorConductor;
    private ServicioConductor servicioConductorMock;
    private HttpSession sessionMock;
    private Conductor conductorMock;

    @BeforeEach
    public void init() {
        servicioConductorMock = mock(ServicioConductor.class);
        controladorConductor = new ControladorConductor(servicioConductorMock);
        sessionMock = mock(HttpSession.class);
        conductorMock = mock(Conductor.class);

        when(conductorMock.getId()).thenReturn(1L);
        when(conductorMock.getNombre()).thenReturn("Pepe");
    }
    
    // NOTA: Los tests de REGISTRO fueron movidos a ControladorRegistroTest
    // Se eliminan irARegistro(), registroCorrectoDeberiaRedirigirAHomeYSetearSesion(), etc.

    @Test
    void siUsuarioNoEstaEnSesionEnHomeDeberiaRedirigirALoginCentral() {
        // Arrange: No hay usuario ID, no hay ROL
        when(sessionMock.getAttribute("usuarioId")).thenReturn(null);
        when(sessionMock.getAttribute("rol")).thenReturn(null);

        // Act
        ModelAndView mav = controladorConductor.irAHome(sessionMock);

        // Assert
        // El ROL ya no es /conductor/login, es /login centralizado
        assertThat(mav.getViewName(), equalTo("redirect:/login")); 
        verify(sessionMock, times(1)).getAttribute("usuarioId");
    }
    
    @Test
    void siUsuarioNoEsConductorDeberiaRedirigirALoginCentral() {
        // Arrange: ID existe, pero ROL no es CONDUCTOR
        when(sessionMock.getAttribute("usuarioId")).thenReturn(1L);
        when(sessionMock.getAttribute("rol")).thenReturn("VIAJERO"); // Rol incorrecto

        // Act
        ModelAndView mav = controladorConductor.irAHome(sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("redirect:/login"));
        verify(servicioConductorMock, never()).obtenerConductor(anyLong());
    }

    @Test
    void siUsuarioEstaEnSesionYEsConductorDeberiaMostrarHomeConNombre() throws UsuarioInexistente {
        // Arrange
        when(sessionMock.getAttribute("usuarioId")).thenReturn(1L);
        when(sessionMock.getAttribute("rol")).thenReturn("CONDUCTOR");
        when(servicioConductorMock.obtenerConductor(1L)).thenReturn(conductorMock);

        // Act
        ModelAndView mav = controladorConductor.irAHome(sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("homeConductor"));
        assertThat(mav.getModel().get("nombreConductor").toString(), equalTo(conductorMock.getNombre()));
        assertThat(mav.getModel().get("rol").toString(), equalTo("CONDUCTOR"));
        verify(servicioConductorMock, times(1)).obtenerConductor(1L);
    }
    
    @Test
    void siUsuarioInexistenteEnSesionDeberiaInvalidarSesionYRedirigirALogin() throws UsuarioInexistente {
        // Arrange
        when(sessionMock.getAttribute("usuarioId")).thenReturn(99L);
        when(sessionMock.getAttribute("rol")).thenReturn("CONDUCTOR");
        
        doThrow(new UsuarioInexistente("Error de sesión"))
            .when(servicioConductorMock).obtenerConductor(99L);

        // Act
        ModelAndView mav = controladorConductor.irAHome(sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("redirect:/login"));
        assertThat(mav.getModel().get("error").toString(), equalTo("Su sesión no es válida. Por favor, inicie sesión nuevamente."));
        verify(sessionMock, times(1)).invalidate();
    }
}