package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Entity.Notificacion;
import com.tallerwebi.dominio.IServicio.ServicioNotificacion;
import com.tallerwebi.presentacion.Controller.ControladorNotificacionAPI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class ControladorNotificacionAPITest {

    private ControladorNotificacionAPI controlador;
    private ServicioNotificacion servicioNotificacionMock;
    private HttpSession sessionMock;
    private final Long USER_ID = 1L;

    @BeforeEach
    public void setUp() {
        servicioNotificacionMock = mock(ServicioNotificacion.class);
        sessionMock = mock(HttpSession.class);
        controlador = new ControladorNotificacionAPI(servicioNotificacionMock);
    }

    @Test
    public void getNotificacionesPendientes_deberiaDevolver401SiNoHaySesion() {
        // GIVEN: La sesión no contiene el ID de usuario
        when(sessionMock.getAttribute("idUsuario")).thenReturn(null);

        // WHEN
        ResponseEntity<List<Notificacion>> response = controlador.getNotificacionesPendientes(sessionMock);

        // THEN
        // 1. Debe devolver el código 401 UNAUTHORIZED
        assertThat(response.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
        // 2. El servicio NUNCA debe ser llamado
        verify(servicioNotificacionMock, never()).obtenerNoVistasYMarcarComoVistas(anyLong());
    }

    @Test
    public void getNotificacionesPendientes_deberiaDevolverNotificacionesCon200() {
        // GIVEN: La sesión contiene el ID de usuario
        Notificacion notif = new Notificacion();
        notif.setMensaje("Test");
        List<Notificacion> notificaciones = Collections.singletonList(notif);

        when(sessionMock.getAttribute("idUsuario")).thenReturn(USER_ID);
        // El servicio devuelve una lista con notificaciones
        when(servicioNotificacionMock.obtenerNoVistasYMarcarComoVistas(USER_ID)).thenReturn(notificaciones);

        // WHEN
        ResponseEntity<List<Notificacion>> response = controlador.getNotificacionesPendientes(sessionMock);

        // THEN
        // 1. Debe devolver el código 200 OK
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        // 2. El cuerpo de la respuesta debe contener la lista de notificaciones
        assertThat(response.getBody(), is(notificaciones));
        // 3. El servicio debe ser llamado con el ID correcto de la sesión
        verify(servicioNotificacionMock, times(1)).obtenerNoVistasYMarcarComoVistas(USER_ID);
    }

    @Test
    public void getNotificacionesPendientes_sinNotificaciones_deberiaDevolverListaVaciaCon200() {
        // GIVEN: El servicio devuelve una lista vacía (como en tu error actual)
        List<Notificacion> notificacionesVacias = Collections.emptyList();

        when(sessionMock.getAttribute("idUsuario")).thenReturn(USER_ID);
        when(servicioNotificacionMock.obtenerNoVistasYMarcarComoVistas(USER_ID)).thenReturn(notificacionesVacias);

        // WHEN
        ResponseEntity<List<Notificacion>> response = controlador.getNotificacionesPendientes(sessionMock);

        // THEN
        // 1. Debe devolver 200 OK, incluso si está vacía
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        // 2. El cuerpo debe ser una lista vacía
        assertThat(response.getBody(), is(empty()));
        verify(servicioNotificacionMock, times(1)).obtenerNoVistasYMarcarComoVistas(USER_ID);
    }
}