package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.IServicio.ServicioViajero;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.presentacion.Controller.ControladorViajero;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ViajeroPerfilOutPutDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
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

    @Test
    void verPerfilViajeroCuandoUnConductorIntentaAccederCorrectamente() throws UsuarioInexistente {
        // Arrange
    Long viajeroId = 1L;
    Conductor conductor = new Conductor();
    when(sessionMock.getAttribute("usuario")).thenReturn(conductor);

    ViajeroPerfilOutPutDTO perfil = new ViajeroPerfilOutPutDTO();
    perfil.setNombre("Nacho");
    when(servicioViajeroMock.obtenerPerfilViajero(viajeroId)).thenReturn(perfil);

    // Act
    ModelAndView mav = controladorViajero.verPerfilViajero(viajeroId, sessionMock);

    // Assert
    assertThat(mav.getViewName(), equalTo("perfilViajero"));
    assertThat(mav.getModel().get("perfil"), equalTo(perfil));
    verify(servicioViajeroMock, times(1)).obtenerPerfilViajero(viajeroId);
    }

    @Test
    void verPerfilViajeroConsuarioNoConductorDebeDarAccesoDenegado() {
        // Arrange
    Long viajeroId = 1L;
    Viajero usuarioNoConductor = new Viajero();
    when(sessionMock.getAttribute("usuario")).thenReturn(usuarioNoConductor);

    // Act
    ModelAndView mav = controladorViajero.verPerfilViajero(viajeroId, sessionMock);

    // Assert
    assertEquals("errorAutorizacion", mav.getViewName());
    assertThat(mav.getModel().get("error").toString(),
               equalTo("Solo los conductores pueden ver perfiles de viajeros"));
    verifyNoInteractions(servicioViajeroMock); // No debe llamar al servicio
    }

    @Test
    void verPerfilViajeroConViajeroInexistenteLanzaError() throws UsuarioInexistente {
        // Arrange
    Long viajeroId = 99L;
    Conductor conductor = new Conductor();
    when(sessionMock.getAttribute("usuario")).thenReturn(conductor);

    when(servicioViajeroMock.obtenerPerfilViajero(viajeroId))
        .thenThrow(new UsuarioInexistente("Viajero no encontrado"));

    // Act
    ModelAndView mav = controladorViajero.verPerfilViajero(viajeroId, sessionMock);

    // Assert
    assertEquals("errorPerfilViajero", mav.getViewName());
    assertThat(mav.getModel().get("error").toString(),
               equalTo("El viajero que intentás visualizar no existe"));
    verify(servicioViajeroMock, times(1)).obtenerPerfilViajero(viajeroId);
    }
}