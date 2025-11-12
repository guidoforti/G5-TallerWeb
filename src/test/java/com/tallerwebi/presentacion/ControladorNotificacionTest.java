package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.IServicio.ServicioNotificacion;
import com.tallerwebi.dominio.Entity.Notificacion;
import com.tallerwebi.dominio.Enums.TipoNotificacion;
import com.tallerwebi.dominio.excepcion.NotFoundException;
import com.tallerwebi.presentacion.Controller.ControladorNotificacion;
import com.tallerwebi.presentacion.DTO.OutputsDTO.NotificacionHistorialDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class ControladorNotificacionTest {

    private ControladorNotificacion controladorNotificacion;
    private ServicioNotificacion servicioNotificacionMock;
    private HttpSession sessionMock;

    private final Long USER_ID = 1L;

    @BeforeEach
    public void init() {
        servicioNotificacionMock = mock(ServicioNotificacion.class);
        controladorNotificacion = new ControladorNotificacion(servicioNotificacionMock);
        sessionMock = mock(HttpSession.class);
    }


    @Test
    void contadorNoLeido_deberiaRetornarElNumeroCorrectoDeNotificaciones() throws NotFoundException {
        // Arrange
        when(sessionMock.getAttribute("idUsuario")).thenReturn(USER_ID);
        when(servicioNotificacionMock.contarNoLeidas(USER_ID)).thenReturn(7L);

        // Act
        Long resultado = controladorNotificacion.getContadorNoLeido(sessionMock);

        // Assert
        assertThat(resultado, is(7L));
        verify(servicioNotificacionMock, times(1)).contarNoLeidas(USER_ID);
    }

    @Test
    void contadorNoLeido_siUsuarioNoEstaEnSesionDeberiaRetornarCero() {
        // Arrange
        when(sessionMock.getAttribute("idUsuario")).thenReturn(null);

        // Act
        Long resultado = controladorNotificacion.getContadorNoLeido(sessionMock);

        // Assert
        assertThat(resultado, is(0L));
        verify(servicioNotificacionMock, never()).contarNoLeidas(anyLong());
    }

    @Test
    void contadorNoLeido_siServicioLanzaNotFoundExceptionDeberiaRetornarCero() throws NotFoundException {
        // Arrange
        when(sessionMock.getAttribute("idUsuario")).thenReturn(USER_ID);
        // Simular que el servicio falla al contar (Ej: el usuario no existe)
        doThrow(new NotFoundException("Usuario no existe")).when(servicioNotificacionMock).contarNoLeidas(USER_ID);

        // Act
        Long resultado = controladorNotificacion.getContadorNoLeido(sessionMock);

        // Assert
        assertThat(resultado, is(0L));
    }


    @Test
    void verHistorial_siUsuarioNoEstaEnSesionDeberiaRedirigirALogin() {
        // Arrange
        when(sessionMock.getAttribute("idUsuario")).thenReturn(null);

        // Act
        ModelAndView mav = controladorNotificacion.verHistorial(sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("redirect:/login"));
        verify(servicioNotificacionMock, never()).obtenerYMarcarComoLeidas(anyLong());
    }

    @Test
    void verHistorial_deberiaMostrarHistorialYMarcarComoLeidas() throws NotFoundException {
        // Arrange
        when(sessionMock.getAttribute("idUsuario")).thenReturn(USER_ID);
        when(sessionMock.getAttribute("ROL")).thenReturn("VIAJERO");

        // Crear notificaciones mock para el DTO
        Notificacion n1 = crearNotificacion(1L, "Mensaje 1", LocalDateTime.now());
        Notificacion n2 = crearNotificacion(2L, "Mensaje 2", LocalDateTime.now());
        List<Notificacion> notificaciones = List.of(n1, n2);

        when(servicioNotificacionMock.obtenerYMarcarComoLeidas(USER_ID)).thenReturn(notificaciones);

        // Act
        ModelAndView mav = controladorNotificacion.verHistorial(sessionMock);

        // Assert
        assertThat(mav.getViewName(), is("historialNotificaciones"));
        assertThat(mav.getModel().get("notificaciones"), is(notNullValue()));
        assertThat(((List<NotificacionHistorialDTO>) mav.getModel().get("notificaciones")), hasSize(2));
        assertThat(mav.getModel().get("userRole"), is("VIAJERO"));

        // Verificar que se llamó al servicio para obtener y marcar
        verify(servicioNotificacionMock, times(1)).obtenerYMarcarComoLeidas(USER_ID);
    }

    @Test
    void verHistorial_siServicioLanzaNotFoundExceptionDeberiaMostrarError() throws NotFoundException {
        // Arrange
        when(sessionMock.getAttribute("idUsuario")).thenReturn(USER_ID);
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");

        // Simular la falla del servicio
        doThrow(new NotFoundException("No hay historial para este usuario")).when(servicioNotificacionMock).obtenerYMarcarComoLeidas(USER_ID);

        // Act
        ModelAndView mav = controladorNotificacion.verHistorial(sessionMock);

        // Assert
        assertThat(mav.getViewName(), is("errorGeneral"));
        assertThat(mav.getModel().get("error").toString(), containsString("Usuario no encontrado"));
        verify(servicioNotificacionMock, times(1)).obtenerYMarcarComoLeidas(USER_ID);
    }


    @Test
    void marcarComoLeida_deberiaRetornarStatusOK() throws NotFoundException {
        // Arrange
        Long idNotificacion = 5L;
        when(sessionMock.getAttribute("idUsuario")).thenReturn(USER_ID);
        // No es necesario stubear el servicio ya que es un método void

        // Act
        ResponseEntity<Void> response = controladorNotificacion.marcarComoLeida(idNotificacion, sessionMock);

        // Assert
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        verify(servicioNotificacionMock, times(1)).marcarComoLeida(idNotificacion);
    }

    @Test
    void marcarComoLeida_siUsuarioNoEstaEnSesionDeberiaRetornarUnauthorized() {
        // Arrange
        when(sessionMock.getAttribute("idUsuario")).thenReturn(null);

        // Act
        ResponseEntity<Void> response = controladorNotificacion.marcarComoLeida(5L, sessionMock);

        // Assert
        assertThat(response.getStatusCode(), is(HttpStatus.UNAUTHORIZED)); // 401
        verify(servicioNotificacionMock, never()).marcarComoLeida(anyLong());
    }

    @Test
    void marcarComoLeida_siNotificacionNoExisteDeberiaRetornarNotFound() throws NotFoundException {
        // Arrange
        Long idNotificacion = 99L;
        when(sessionMock.getAttribute("idUsuario")).thenReturn(USER_ID);
        // Simular que el servicio lanza NotFoundException
        doThrow(new NotFoundException("Notificación no existe")).when(servicioNotificacionMock).marcarComoLeida(idNotificacion);

        // Act
        ResponseEntity<Void> response = controladorNotificacion.marcarComoLeida(idNotificacion, sessionMock);

        // Assert
        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND)); // 404
        verify(servicioNotificacionMock, times(1)).marcarComoLeida(idNotificacion);
    }

    @Test
    void marcarComoLeida_siOcurreOtroErrorDeberiaRetornarInternalServerError() throws NotFoundException {
        // Arrange
        Long idNotificacion = 10L;
        when(sessionMock.getAttribute("idUsuario")).thenReturn(USER_ID);
        // Simular que el servicio lanza una excepción genérica
        doThrow(new RuntimeException("Error inesperado")).when(servicioNotificacionMock).marcarComoLeida(idNotificacion);

        // Act
        ResponseEntity<Void> response = controladorNotificacion.marcarComoLeida(idNotificacion, sessionMock);

        // Assert
        assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR)); // 500
        verify(servicioNotificacionMock, times(1)).marcarComoLeida(idNotificacion);
    }

    private Notificacion crearNotificacion(Long id, String mensaje, LocalDateTime fecha) {
        Notificacion n = new Notificacion();
        n.setId(id);
        n.setMensaje(mensaje);
        n.setFechaCreacion(fecha);
        n.setLeida(false);
        n.setTipo(TipoNotificacion.RESERVA_APROBADA);
        return n;
    }
}