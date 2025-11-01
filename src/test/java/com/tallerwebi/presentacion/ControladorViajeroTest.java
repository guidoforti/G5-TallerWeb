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

    @Test
    void siUsuarioNoEstaEnSesionEnHomeDeberiaRedirigirALoginCentral() {
        // Arrange: Mockeamos las claves CORRECTAS: idUsuario y ROL
        when(sessionMock.getAttribute("idUsuario")).thenReturn(null);
        when(sessionMock.getAttribute("ROL")).thenReturn(null);

        // Act
        ModelAndView mav = controladorViajero.irAHome(sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("redirect:/login"));
        verify(sessionMock, times(1)).getAttribute("idUsuario");
        // El controlador lee "ROL" si "idUsuario" es null, por lo que esta verificación es opcional pero correcta.
        verify(sessionMock, times(1)).getAttribute("ROL");
    }

    @Test
    void siUsuarioNoEsViajeroDeberiaRedirigirALoginCentral() throws UsuarioInexistente {
        // Arrange: Mockeamos las claves CORRECTAS: idUsuario y ROL
        when(sessionMock.getAttribute("idUsuario")).thenReturn(1L);
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR"); // Rol incorrecto

        // Act
        ModelAndView mav = controladorViajero.irAHome(sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("redirect:/login"));
        verify(servicioViajeroMock, never()).obtenerViajero(anyLong());
    }


    @Test
    void siUsuarioEstaEnSesionYEsViajeroDeberiaMostrarHomeConNombre() throws UsuarioInexistente {
        // Arrange: Mockeamos las claves CORRECTAS: idUsuario y ROL
        when(sessionMock.getAttribute("idUsuario")).thenReturn(1L);
        when(sessionMock.getAttribute("ROL")).thenReturn("VIAJERO");
        when(servicioViajeroMock.obtenerViajero(1L)).thenReturn(viajeroMock);

        // Act
        ModelAndView mav = controladorViajero.irAHome(sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("homeViajero"));
        // El modelo del controlador usa la clave "nombreConductor" (aún inconsistente, pero el test la respeta)
        assertThat(mav.getModel().get("nombreViajero").toString(), equalTo(viajeroMock.getNombre()));
        assertThat(mav.getModel().get("rol").toString(), equalTo("VIAJERO"));
        verify(servicioViajeroMock, times(1)).obtenerViajero(1L);
    }

    @Test
    void siUsuarioInexistenteEnSesionDeberiaInvalidarSesionYRedirigirALogin() throws UsuarioInexistente {
        // Arrange: Mockeamos las claves CORRECTAS: idUsuario y ROL
        when(sessionMock.getAttribute("idUsuario")).thenReturn(99L);
        when(sessionMock.getAttribute("ROL")).thenReturn("VIAJERO");

        // Hacemos que la llamada al servicio falle (entra al catch)
        doThrow(new UsuarioInexistente("Error de sesión"))
                .when(servicioViajeroMock).obtenerViajero(99L);

        // Act
        ModelAndView mav = controladorViajero.irAHome(sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("redirect:/login"));
        verify(sessionMock, times(1)).invalidate();
    }
}