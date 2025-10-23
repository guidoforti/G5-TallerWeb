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

    @Test
    void siUsuarioNoEstaEnSesionEnHomeDeberiaRedirigirALoginCentral() {
        // Arrange: Mockeamos las claves CORRECTAS
        when(sessionMock.getAttribute("idUsuario")).thenReturn(null);
        when(sessionMock.getAttribute("ROL")).thenReturn(null);

        // Act
        ModelAndView mav = controladorConductor.irAHome(sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("redirect:/login"));
        // Verificamos la invocación con la clave CORRECTA: idUsuario
        verify(sessionMock, times(1)).getAttribute("idUsuario");
        // No es necesario verificar el rol si el ID es nulo, pero la llamada ocurre:
        verify(sessionMock, times(1)).getAttribute("ROL");
    }

    @Test
    void siUsuarioNoEsConductorDeberiaRedirigirALoginCentral() throws UsuarioInexistente {
        // Arrange: Mockeamos las claves CORRECTAS
        when(sessionMock.getAttribute("idUsuario")).thenReturn(1L);
        when(sessionMock.getAttribute("ROL")).thenReturn("VIAJERO"); // Rol incorrecto

        // Act
        ModelAndView mav = controladorConductor.irAHome(sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("redirect:/login"));
        verify(servicioConductorMock, never()).obtenerConductor(anyLong());
    }

    @Test
    void siUsuarioEstaEnSesionYEsConductorDeberiaMostrarHomeConNombre() throws UsuarioInexistente {
        // Arrange: Mockeamos las claves CORRECTAS
        when(sessionMock.getAttribute("idUsuario")).thenReturn(1L);
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");
        when(servicioConductorMock.obtenerConductor(1L)).thenReturn(conductorMock);

        // Act
        ModelAndView mav = controladorConductor.irAHome(sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("homeConductor"));
        assertThat(mav.getModel().get("nombreConductor").toString(), equalTo(conductorMock.getNombre()));
        assertThat(mav.getModel().get("rol").toString(), equalTo("CONDUCTOR")); // El modelo usa 'rol' minúsculas
        verify(servicioConductorMock, times(1)).obtenerConductor(1L);
    }

    @Test
    void siUsuarioInexistenteEnSesionDeberiaInvalidarSesionYRedirigirALogin() throws UsuarioInexistente {
        // Arrange: Mockeamos las claves CORRECTAS
        when(sessionMock.getAttribute("idUsuario")).thenReturn(99L);
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");

        // Hacemos que la llamada al servicio falle (entra al catch)
        doThrow(new UsuarioInexistente("Error de sesión"))
                .when(servicioConductorMock).obtenerConductor(99L);

        // Act
        ModelAndView mav = controladorConductor.irAHome(sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("redirect:/login"));
        // Verificamos que se llegó al catch y se llamó a invalidate()
        verify(sessionMock, times(1)).invalidate();
    }
}