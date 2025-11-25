package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Entity.Ciudad;
import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import com.tallerwebi.dominio.IServicio.ServicioHistorialReserva;
import com.tallerwebi.dominio.IServicio.ServicioNotificacion;
import com.tallerwebi.dominio.IServicio.ServicioViaje;
import com.tallerwebi.presentacion.DTO.OutputsDTO.HistorialReservaDTO;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.dominio.excepcion.UsuarioNoAutorizadoException;
import com.tallerwebi.dominio.excepcion.ViajeNoEncontradoException;
import com.tallerwebi.presentacion.Controller.ControladorHistorialReserva;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

public class ControladorHistorialReservaTest {

    private ControladorHistorialReserva historialReservaController;
    private ServicioHistorialReserva servicioHistorialReservaMock;
    private ServicioNotificacion servicioNotificacionMock;
    private ServicioConductor servicioConductorMock;
    private ServicioViaje servicioViajeMock;
    private HttpSession sessionMock;

    private final Long ID_VIAJE = 10L;
    private final Long ID_CONDUCTOR = 2L;

    @BeforeEach
    void setUp() {
        servicioHistorialReservaMock = mock(ServicioHistorialReserva.class);
        servicioConductorMock = mock(ServicioConductor.class);
        servicioNotificacionMock = mock(ServicioNotificacion.class);
        servicioViajeMock = mock(ServicioViaje.class);
        sessionMock = mock(HttpSession.class);

        historialReservaController = new ControladorHistorialReserva(servicioHistorialReservaMock, servicioConductorMock, servicioNotificacionMock, servicioViajeMock);
    }

    private Conductor setupConductorSession(Long conductorId) throws UsuarioInexistente {
        Conductor conductor = new Conductor();
        conductor.setId(conductorId);
        conductor.setRol("CONDUCTOR");

        when(sessionMock.getAttribute("idUsuario")).thenReturn(conductorId);
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");
        when(servicioConductorMock.obtenerConductor(conductorId)).thenReturn(conductor);
        return conductor;
    }

    @Test
    public void deberiaMostrarHistorialDeReservasSiElUsuarioEsElConductorDelViaje() throws Exception {
        // given
        Conductor conductor = setupConductorSession(ID_CONDUCTOR);

        Viaje viajeMock = mock(Viaje.class);
        Ciudad origenMock = mock(Ciudad.class);
        Ciudad destinoMock = mock(Ciudad.class);

        when(servicioViajeMock.obtenerViajePorId(ID_VIAJE)).thenReturn(viajeMock);
        when(viajeMock.getOrigen()).thenReturn(origenMock);
        when(viajeMock.getDestino()).thenReturn(destinoMock);
        when(origenMock.getNombre()).thenReturn("Buenos Aires"); 
        when(destinoMock.getNombre()).thenReturn("Córdoba");

        List<HistorialReservaDTO> historialEsperado = Arrays.asList(
                new HistorialReservaDTO(), new HistorialReservaDTO()
        );

        when(servicioHistorialReservaMock.obtenerHistorialPorViaje(eq(ID_VIAJE), any(Usuario.class)))
                .thenReturn(historialEsperado);

        // when
        ModelAndView mav = historialReservaController.verHistorialPorViaje(ID_VIAJE, sessionMock);

        // then
        assertThat(mav.getModel().get("origenNombre"), is("Buenos Aires")); 
        assertThat(mav.getModel().get("destinoNombre"), is("Córdoba"));
        assertThat(mav.getViewName(), is("historialReservas"));
        assertThat(mav.getModel().get("historialReservas"), is(historialEsperado));
        verify(servicioHistorialReservaMock, times(1)).obtenerHistorialPorViaje(eq(ID_VIAJE), any(Usuario.class));
    }


    @Test
    public void siElUsuarioNoEstaEnSesionDeberiaRedirigirALogin() {
        // given

        // when
        ModelAndView mav = historialReservaController.verHistorialPorViaje(ID_VIAJE, sessionMock);

        // then
        assertThat(mav.getViewName(), is("redirect:/login"));
        verifyNoInteractions(servicioHistorialReservaMock);
        verifyNoInteractions(servicioConductorMock);
    }

    @Test
    public void siElUsuarioEsOtroConductorODiferenteDeberiaDevolverErrorDeAutorizacion() throws Exception {
        // given
        Conductor usuarioNoAutorizado = setupConductorSession(99L);

        Viaje viajeMock = mock(Viaje.class);
        Ciudad origenMock = mock(Ciudad.class);
        Ciudad destinoMock = mock(Ciudad.class);
        when(servicioViajeMock.obtenerViajePorId(ID_VIAJE)).thenReturn(viajeMock);
        when(viajeMock.getOrigen()).thenReturn(origenMock);
        when(viajeMock.getDestino()).thenReturn(destinoMock);
        when(origenMock.getNombre()).thenReturn("A"); 
        when(destinoMock.getNombre()).thenReturn("B");

        doThrow(new UsuarioNoAutorizadoException("No tenés permisos para ver el historial de este viaje."))
                .when(servicioHistorialReservaMock).obtenerHistorialPorViaje(eq(ID_VIAJE), any(Usuario.class));

        // when
        ModelAndView mav = historialReservaController.verHistorialPorViaje(ID_VIAJE, sessionMock);

        // then
        assertThat(mav.getViewName(), is("error"));
        assertThat(mav.getModel().get("error").toString(), containsString("Error de Acceso:"));
        verify(servicioHistorialReservaMock, times(1)).obtenerHistorialPorViaje(eq(ID_VIAJE), any(Usuario.class));
    }

    @Test
    public void siElViajeNoExisteDeberiaDevolverErrorDeViajeNoEncontrado() throws Exception {
        // given
        Conductor conductor = setupConductorSession(ID_CONDUCTOR);

        doThrow(new ViajeNoEncontradoException("No se encontró el viaje con ID " + ID_VIAJE))
            .when(servicioViajeMock).obtenerViajePorId(ID_VIAJE);

        // when
        ModelAndView mav = historialReservaController.verHistorialPorViaje(ID_VIAJE, sessionMock);

        // then
        assertThat(mav.getViewName(), is("error"));
        assertThat(mav.getModel().get("error").toString(), containsString("No se encontró el viaje"));
        verify(servicioViajeMock, times(1)).obtenerViajePorId(ID_VIAJE);
        verify(servicioHistorialReservaMock, never()).obtenerHistorialPorViaje(anyLong(), any(Usuario.class));
    }

    @Test
    public void siElUsuarioLogueadoNoEsConductorDeberiaRedirigirALogin() {
        // given
        when(sessionMock.getAttribute("idUsuario")).thenReturn(10L);
        when(sessionMock.getAttribute("ROL")).thenReturn("VIAJERO"); // Rol que NO es CONDUCTOR

        // when
        ModelAndView mav = historialReservaController.verHistorialPorViaje(ID_VIAJE, sessionMock);

        // then
        assertThat(mav.getViewName(), is("redirect:/login"));
        verifyNoInteractions(servicioHistorialReservaMock);
        verifyNoInteractions(servicioConductorMock);
    }

    @Test
    public void siElServicioLanzaUnaExcepcionGenericaDeberiaDevolverErrorInesperado() throws Exception {
        // given
        Conductor conductor = setupConductorSession(ID_CONDUCTOR);

        Viaje viajeMock = mock(Viaje.class);
        Ciudad origenMock = mock(Ciudad.class);
        Ciudad destinoMock = mock(Ciudad.class);
        when(servicioViajeMock.obtenerViajePorId(ID_VIAJE)).thenReturn(viajeMock);
        when(viajeMock.getOrigen()).thenReturn(origenMock);
        when(viajeMock.getDestino()).thenReturn(destinoMock);
        when(origenMock.getNombre()).thenReturn("A"); 
        when(destinoMock.getNombre()).thenReturn("B");

        doThrow(new RuntimeException("Error de base de datos desconocido"))
                .when(servicioHistorialReservaMock).obtenerHistorialPorViaje(eq(ID_VIAJE), any(Usuario.class));

        // when
        ModelAndView mav = historialReservaController.verHistorialPorViaje(ID_VIAJE, sessionMock);

        // then
        assertThat(mav.getViewName(), is("error"));
        assertThat(mav.getModel().get("error").toString(), containsString("Ocurrió un error inesperado"));
        verify(servicioHistorialReservaMock, times(1)).obtenerHistorialPorViaje(eq(ID_VIAJE), any(Usuario.class));
    }

    @Test
    public void siElViajeExistePeroNoTieneHistorialDeberiaMostrarUnaListaVacia() throws Exception {
        // given
        Conductor conductor = setupConductorSession(ID_CONDUCTOR);

        Viaje viajeMock = mock(Viaje.class);
        Ciudad origenMock = mock(Ciudad.class);
        Ciudad destinoMock = mock(Ciudad.class);
        when(servicioViajeMock.obtenerViajePorId(ID_VIAJE)).thenReturn(viajeMock);
        when(viajeMock.getOrigen()).thenReturn(origenMock);
        when(viajeMock.getDestino()).thenReturn(destinoMock);
        when(origenMock.getNombre()).thenReturn("Salta");
        when(destinoMock.getNombre()).thenReturn("Jujuy"); 

        List<HistorialReservaDTO> historialVacio = List.of();

        when(servicioHistorialReservaMock.obtenerHistorialPorViaje(eq(ID_VIAJE), any(Usuario.class)))
                .thenReturn(historialVacio);

        // when
        ModelAndView mav = historialReservaController.verHistorialPorViaje(ID_VIAJE, sessionMock);

        // then
        assertThat(mav.getModel().get("origenNombre"), is("Salta"));
        assertThat(mav.getModel().get("destinoNombre"), is("Jujuy"));
        assertThat(mav.getViewName(), is("historialReservas"));
        assertThat((List<HistorialReservaDTO>) mav.getModel().get("historialReservas"), is(empty()));
        assertThat(mav.getModel().get("mensajeExito").toString(), containsString("Historial cargado correctamente"));
        verify(servicioHistorialReservaMock, times(1)).obtenerHistorialPorViaje(eq(ID_VIAJE), any(Usuario.class));
    }
}