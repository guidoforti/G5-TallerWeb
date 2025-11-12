package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.IServicio.ServicioNotificacion;
import com.tallerwebi.dominio.IServicio.ServicioViajero;
import com.tallerwebi.dominio.excepcion.NotFoundException;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.presentacion.Controller.ControladorViajero;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ViajeroPerfilOutPutDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class ControladorViajeroTest {

    private ControladorViajero controladorViajero;
    private ServicioViajero servicioViajeroMock;
    private ServicioNotificacion servicioNotificacionMock;
    private HttpSession sessionMock;
    private Viajero viajeroMock;

    private final Long VIAJERO_ID = 1L;
    private final Long OTRO_VIAJERO_ID = 5L;

    @BeforeEach
    public void init() throws NotFoundException {
        servicioViajeroMock = mock(ServicioViajero.class);
        servicioNotificacionMock = mock(ServicioNotificacion.class);
        controladorViajero = new ControladorViajero(servicioViajeroMock, servicioNotificacionMock);
        sessionMock = mock(HttpSession.class);
        viajeroMock = mock(Viajero.class);

        when(viajeroMock.getId()).thenReturn(VIAJERO_ID);
        when(viajeroMock.getNombre()).thenReturn("Juan Pérez");
        when(servicioNotificacionMock.contarNoLeidas(anyLong())).thenReturn(3L);
    }

    // =================================================================================
    // Tests: irAHome (GET /home)
    // =================================================================================

    @Test
    void siUsuarioNoEstaEnSesionEnHomeDeberiaRedirigirALoginCentral() throws UsuarioInexistente{
        // Arrange
        when(sessionMock.getAttribute("idUsuario")).thenReturn(null);

        // Act
        ModelAndView mav = controladorViajero.irAHome(sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("redirect:/login"));
        verify(servicioViajeroMock, never()).obtenerViajero(anyLong());
    }

    @Test
    void siUsuarioNoEsViajeroDeberiaRedirigirALoginCentral() throws UsuarioInexistente {
        // Arrange
        when(sessionMock.getAttribute("idUsuario")).thenReturn(VIAJERO_ID);
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR"); // Rol incorrecto

        // Act
        ModelAndView mav = controladorViajero.irAHome(sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("redirect:/login"));
        verify(servicioViajeroMock, never()).obtenerViajero(anyLong());
    }


    @Test
    void siUsuarioEstaEnSesionYEsViajeroDeberiaMostrarHomeConNombreYContador() throws UsuarioInexistente, NotFoundException {
        // Arrange
        when(sessionMock.getAttribute("idUsuario")).thenReturn(VIAJERO_ID);
        when(sessionMock.getAttribute("ROL")).thenReturn("VIAJERO");
        when(servicioViajeroMock.obtenerViajero(VIAJERO_ID)).thenReturn(viajeroMock);

        // Act
        ModelAndView mav = controladorViajero.irAHome(sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("homeViajero"));
        assertThat(mav.getModel().get("nombreViajero").toString(), equalTo(viajeroMock.getNombre()));
        assertThat(mav.getModel().get("ROL").toString(), equalTo("VIAJERO"));
        assertThat(mav.getModel().get("contadorNotificaciones"), is(3));
        verify(servicioViajeroMock, times(1)).obtenerViajero(VIAJERO_ID);
    }

    @Test
    void siUsuarioInexistenteEnSesionDeberiaInvalidarSesionYRedirigirALogin() throws UsuarioInexistente {
        // Arrange
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

    // =================================================================================
    // Tests: verMiPerfil (GET /perfil)
    // =================================================================================

    @Test
    void verMiPerfil_deberiaMostrarPerfilCorrectamente() throws UsuarioInexistente {
        // Arrange
        when(sessionMock.getAttribute("idUsuario")).thenReturn(VIAJERO_ID);
        when(sessionMock.getAttribute("ROL")).thenReturn("VIAJERO");

        ViajeroPerfilOutPutDTO perfilDTO = new ViajeroPerfilOutPutDTO();
        perfilDTO.setNombre("Yo Mismo");
        when(servicioViajeroMock.obtenerPerfilViajero(VIAJERO_ID)).thenReturn(perfilDTO);

        // Act
        ModelAndView mav = controladorViajero.verMiPerfil(sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("perfilViajero"));
        assertThat(mav.getModel().get("perfil"), instanceOf(ViajeroPerfilOutPutDTO.class));
        assertThat(((ViajeroPerfilOutPutDTO) mav.getModel().get("perfil")).getNombre(), is("Yo Mismo"));
        assertThat(mav.getModel().get("contadorNotificaciones"), is(3));
    }

    @Test
    void verMiPerfil_deberiaRedirigirALoginSiSesionNula() throws UsuarioInexistente {
        // Arrange
        when(sessionMock.getAttribute("idUsuario")).thenReturn(null);

        // Act
        ModelAndView mav = controladorViajero.verMiPerfil(sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("redirect:/login"));
        verify(servicioViajeroMock, never()).obtenerPerfilViajero(anyLong());
    }

    @Test
    void verMiPerfil_deberiaMostrarErrorSiUsuarioInexistente() throws UsuarioInexistente {
        // Arrange
        when(sessionMock.getAttribute("idUsuario")).thenReturn(VIAJERO_ID);
        when(sessionMock.getAttribute("ROL")).thenReturn("VIAJERO");

        doThrow(new UsuarioInexistente("No existe"))
                .when(servicioViajeroMock).obtenerPerfilViajero(VIAJERO_ID);

        // Act
        ModelAndView mav = controladorViajero.verMiPerfil(sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("errorPerfilViajero"));
        assertThat(mav.getModel().get("error").toString(), is("Su perfil no existe."));
    }

    @Test
    void verMiPerfil_deberiaPonerContadorNotificacionesEnCeroSiNotFound() throws UsuarioInexistente, NotFoundException {
        // Arrange
        when(sessionMock.getAttribute("idUsuario")).thenReturn(VIAJERO_ID);
        when(sessionMock.getAttribute("ROL")).thenReturn("VIAJERO");

        doThrow(new NotFoundException("No hay contador")).when(servicioNotificacionMock).contarNoLeidas(VIAJERO_ID);
        when(servicioViajeroMock.obtenerPerfilViajero(VIAJERO_ID)).thenReturn(new ViajeroPerfilOutPutDTO()); // Camino feliz del servicio

        // Act
        ModelAndView mav = controladorViajero.verMiPerfil(sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("perfilViajero"));
        assertThat(mav.getModel().get("contadorNotificaciones"), is(0));
        verify(servicioNotificacionMock, times(1)).contarNoLeidas(VIAJERO_ID);
    }

    // =================================================================================
    // Tests: verPerfilViajeroPorId (GET /perfil/{id})
    // =================================================================================

    @Test
    void verPerfilViajeroPorId_cuandoConductorIntentaAccederDebeSerExitoso() throws UsuarioInexistente {
        // Arrange
        when(sessionMock.getAttribute("idUsuario")).thenReturn(OTRO_VIAJERO_ID); // Conductor en sesión
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR"); // Rol correcto

        Long viajeroSolicitadoId = VIAJERO_ID;
        ViajeroPerfilOutPutDTO perfil = new ViajeroPerfilOutPutDTO();
        perfil.setNombre("Viajero solicitado");
        when(servicioViajeroMock.obtenerPerfilViajero(viajeroSolicitadoId)).thenReturn(perfil);

        // Act
        ModelAndView mav = controladorViajero.verPerfilViajeroPorId(viajeroSolicitadoId, sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("perfilViajero"));
        assertThat(mav.getModel().get("perfil"), equalTo(perfil));
        assertThat(mav.getModel().get("ROL"), is("CONDUCTOR"));
        verify(servicioViajeroMock, times(1)).obtenerPerfilViajero(viajeroSolicitadoId);
    }

    @Test
    void verPerfilViajeroPorId_siUsuarioNoEstaEnSesionDeberiaRedirigirALogin() throws UsuarioInexistente{
        // Arrange
        when(sessionMock.getAttribute("idUsuario")).thenReturn(null);

        // Act
        ModelAndView mav = controladorViajero.verPerfilViajeroPorId(OTRO_VIAJERO_ID, sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("redirect:/login"));
        verify(servicioViajeroMock, never()).obtenerPerfilViajero(anyLong());
    }

    @Test
    void verPerfilViajeroPorId_siUsuarioNoEsConductorDebeDarErrorAutorizacion() throws UsuarioInexistente{
        // Arrange
        when(sessionMock.getAttribute("idUsuario")).thenReturn(OTRO_VIAJERO_ID);
        when(sessionMock.getAttribute("ROL")).thenReturn("VIAJERO"); // Rol incorrecto

        // Act
        ModelAndView mav = controladorViajero.verPerfilViajeroPorId(VIAJERO_ID, sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("errorAutorizacion"));
        assertThat(mav.getModel().get("error").toString(),
                equalTo("Solo los conductores pueden ver perfiles de otros viajeros."));
        verify(servicioViajeroMock, never()).obtenerPerfilViajero(anyLong());
    }

    @Test
    void verPerfilViajeroPorId_siViajeroSolicitadoInexistenteLanzaError() throws UsuarioInexistente {
        // Arrange
        Long viajeroSolicitadoId = 99L;
        when(sessionMock.getAttribute("idUsuario")).thenReturn(OTRO_VIAJERO_ID);
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");

        when(servicioViajeroMock.obtenerPerfilViajero(viajeroSolicitadoId))
                .thenThrow(new UsuarioInexistente("Viajero no encontrado"));

        // Act
        ModelAndView mav = controladorViajero.verPerfilViajeroPorId(viajeroSolicitadoId, sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("errorPerfilViajero"));
        assertThat(mav.getModel().get("error").toString(),
                equalTo("El perfil solicitado no existe."));
        verify(servicioViajeroMock, times(1)).obtenerPerfilViajero(viajeroSolicitadoId);
    }

    @Test
    void verPerfilViajeroPorId_deberiaPonerContadorNotificacionesEnCeroSiNotFound() throws NotFoundException, UsuarioInexistente {
        // Arrange
        when(sessionMock.getAttribute("idUsuario")).thenReturn(OTRO_VIAJERO_ID);
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");

        doThrow(new NotFoundException("No hay contador")).when(servicioNotificacionMock).contarNoLeidas(OTRO_VIAJERO_ID);
        when(servicioViajeroMock.obtenerPerfilViajero(VIAJERO_ID)).thenReturn(new ViajeroPerfilOutPutDTO()); // Camino feliz del servicio

        // Act
        ModelAndView mav = controladorViajero.verPerfilViajeroPorId(VIAJERO_ID, sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("perfilViajero"));
        assertThat(mav.getModel().get("contadorNotificaciones"), is(0));
        verify(servicioNotificacionMock, times(1)).contarNoLeidas(OTRO_VIAJERO_ID);
    }
}