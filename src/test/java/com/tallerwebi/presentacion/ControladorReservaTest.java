package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Entity.Reserva;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.Enums.EstadoReserva;
import com.tallerwebi.dominio.IServicio.ServicioReserva;
import com.tallerwebi.dominio.IServicio.ServicioViaje;
import com.tallerwebi.dominio.IServicio.ServicioViajero;
import com.tallerwebi.dominio.excepcion.*;
import com.tallerwebi.presentacion.Controller.ControladorReserva;
import com.tallerwebi.presentacion.DTO.InputsDTO.SolicitudReservaInputDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ReservaVistaDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ControladorReservaTest {

    private ControladorReserva controladorReserva;
    private ServicioReserva servicioReservaMock;
    private ServicioViaje servicioViajeMock;
    private ServicioViajero servicioViajeroMock;
    private HttpSession sessionMock;

    @BeforeEach
    public void init() {
        servicioReservaMock = mock(ServicioReserva.class);
        servicioViajeMock = mock(ServicioViaje.class);
        servicioViajeroMock = mock(ServicioViajero.class);
        controladorReserva = new ControladorReserva(servicioReservaMock, servicioViajeMock, servicioViajeroMock);
        sessionMock = mock(HttpSession.class);
    }

    // --- TESTS DE MOSTRAR FORMULARIO DE SOLICITUD ---

    @Test
    public void deberiaMostrarFormularioSolicitudCuandoUsuarioLogueado() throws NotFoundException, UsuarioNoAutorizadoException, ViajeNoEncontradoException {
        // given
        Long usuarioId = 1L;
        Long viajeId = 10L;
        Viaje viajeMock = new Viaje();
        viajeMock.setId(viajeId);

        when(sessionMock.getAttribute("idUsuario")).thenReturn(usuarioId);
        when(servicioViajeMock.obtenerViajePorId(viajeId)).thenReturn(viajeMock);

        // when
        ModelAndView mav = controladorReserva.mostrarFormularioSolicitud(viajeId, sessionMock);

        // then
        assertThat(mav.getViewName(), is("solicitarReserva"));
        assertThat(mav.getModel().get("viaje"), is(viajeMock));
        assertThat(mav.getModel().get("solicitud"), instanceOf(SolicitudReservaInputDTO.class));
        verify(servicioViajeMock, times(1)).obtenerViajePorId(viajeId);
    }

    @Test
    public void deberiaRedirigirALoginSiNoHaySesionEnFormulario() throws ViajeNoEncontradoException, NotFoundException, UsuarioNoAutorizadoException {
        // given
        when(sessionMock.getAttribute("idUsuario")).thenReturn(null);

        // when
        ModelAndView mav = controladorReserva.mostrarFormularioSolicitud(1L, sessionMock);

        // then
        assertThat(mav.getViewName(), is("redirect:/login"));
        verify(servicioViajeMock, never()).obtenerViajePorId(anyLong());
    }

    @Test
    public void deberiaRedirigirABuscarSiViajeNoExiste() throws NotFoundException, ViajeNoEncontradoException, UsuarioNoAutorizadoException {
        // given
        Long usuarioId = 1L;
        Long viajeId = 999L;

        when(sessionMock.getAttribute("idUsuario")).thenReturn(usuarioId);
        when(servicioViajeMock.obtenerViajePorId(viajeId)).thenThrow(new NotFoundException("Viaje no encontrado"));

        // when
        ModelAndView mav = controladorReserva.mostrarFormularioSolicitud(viajeId, sessionMock);

        // then
        assertThat(mav.getViewName(), is("redirect:/viaje/buscar"));
        verify(servicioViajeMock, times(1)).obtenerViajePorId(viajeId);
    }

    // --- TESTS DE SOLICITAR RESERVA (POST) ---

    @Test
    public void deberiaSolicitarReservaExitosamente() throws Exception {
        // given
        Long usuarioId = 1L;
        Long viajeId = 10L;
        SolicitudReservaInputDTO solicitudDTO = new SolicitudReservaInputDTO(viajeId, usuarioId);

        Viaje viajeMock = new Viaje();
        viajeMock.setId(viajeId);

        Viajero viajeroMock = new Viajero();
        viajeroMock.setId(usuarioId);

        Reserva reservaMock = new Reserva();
        reservaMock.setId(1L);
        reservaMock.setViaje(viajeMock);
        reservaMock.setViajero(viajeroMock);
        reservaMock.setEstado(EstadoReserva.PENDIENTE);
        reservaMock.setFechaSolicitud(LocalDateTime.now());

        when(sessionMock.getAttribute("idUsuario")).thenReturn(usuarioId);
        when(servicioViajeMock.obtenerViajePorId(viajeId)).thenReturn(viajeMock);
        when(servicioViajeroMock.obtenerViajero(usuarioId)).thenReturn(viajeroMock);
        when(servicioReservaMock.solicitarReserva(viajeMock, viajeroMock)).thenReturn(reservaMock);

        // when
        ModelAndView mav = controladorReserva.solicitarReserva(solicitudDTO, sessionMock);

        // then
        assertThat(mav.getViewName(), is("reservaExitosa"));
        assertThat(mav.getModel().get("mensaje"), notNullValue());
        assertThat(mav.getModel().get("reserva"), instanceOf(ReservaVistaDTO.class));
        verify(servicioReservaMock, times(1)).solicitarReserva(viajeMock, viajeroMock);
    }

    @Test
    public void deberiaRedirigirALoginSiNoHaySesionEnPost() throws ReservaYaExisteException, DatoObligatorioException, ViajeYaIniciadoException, SinAsientosDisponiblesException {
        // given
        SolicitudReservaInputDTO solicitudDTO = new SolicitudReservaInputDTO(1L, 1L);
        when(sessionMock.getAttribute("idUsuario")).thenReturn(null);

        // when
        ModelAndView mav = controladorReserva.solicitarReserva(solicitudDTO, sessionMock);

        // then
        assertThat(mav.getViewName(), is("redirect:/login"));
        verify(servicioReservaMock, never()).solicitarReserva(any(), any());
    }

    @Test
    public void deberiaRedirigirABuscarSiReservaYaExiste() throws Exception {
        // given
        Long usuarioId = 1L;
        Long viajeId = 10L;
        SolicitudReservaInputDTO solicitudDTO = new SolicitudReservaInputDTO(viajeId, usuarioId);

        Viaje viajeMock = new Viaje();
        Viajero viajeroMock = new Viajero();

        when(sessionMock.getAttribute("idUsuario")).thenReturn(usuarioId);
        when(servicioViajeMock.obtenerViajePorId(viajeId)).thenReturn(viajeMock);
        when(servicioViajeroMock.obtenerViajero(usuarioId)).thenReturn(viajeroMock);
        when(servicioReservaMock.solicitarReserva(viajeMock, viajeroMock))
                .thenThrow(new ReservaYaExisteException("Ya existe una reserva"));

        // when
        ModelAndView mav = controladorReserva.solicitarReserva(solicitudDTO, sessionMock);

        // then
        assertThat(mav.getViewName(), is("redirect:/viaje/buscar"));
        verify(servicioReservaMock, times(1)).solicitarReserva(viajeMock, viajeroMock);
    }

    @Test
    public void deberiaRedirigirABuscarSiNoHayAsientosDisponibles() throws Exception {
        // given
        Long usuarioId = 1L;
        Long viajeId = 10L;
        SolicitudReservaInputDTO solicitudDTO = new SolicitudReservaInputDTO(viajeId, usuarioId);

        Viaje viajeMock = new Viaje();
        Viajero viajeroMock = new Viajero();

        when(sessionMock.getAttribute("idUsuario")).thenReturn(usuarioId);
        when(servicioViajeMock.obtenerViajePorId(viajeId)).thenReturn(viajeMock);
        when(servicioViajeroMock.obtenerViajero(usuarioId)).thenReturn(viajeroMock);
        when(servicioReservaMock.solicitarReserva(viajeMock, viajeroMock))
                .thenThrow(new SinAsientosDisponiblesException("No hay asientos"));

        // when
        ModelAndView mav = controladorReserva.solicitarReserva(solicitudDTO, sessionMock);

        // then
        assertThat(mav.getViewName(), is("redirect:/viaje/buscar"));
        verify(servicioReservaMock, times(1)).solicitarReserva(viajeMock, viajeroMock);
    }

    @Test
    public void deberiaRedirigirABuscarSiViajeYaInicio() throws Exception {
        // given
        Long usuarioId = 1L;
        Long viajeId = 10L;
        SolicitudReservaInputDTO solicitudDTO = new SolicitudReservaInputDTO(viajeId, usuarioId);

        Viaje viajeMock = new Viaje();
        Viajero viajeroMock = new Viajero();

        when(sessionMock.getAttribute("idUsuario")).thenReturn(usuarioId);
        when(servicioViajeMock.obtenerViajePorId(viajeId)).thenReturn(viajeMock);
        when(servicioViajeroMock.obtenerViajero(usuarioId)).thenReturn(viajeroMock);
        when(servicioReservaMock.solicitarReserva(viajeMock, viajeroMock))
                .thenThrow(new ViajeYaIniciadoException("El viaje ya inició"));

        // when
        ModelAndView mav = controladorReserva.solicitarReserva(solicitudDTO, sessionMock);

        // then
        assertThat(mav.getViewName(), is("redirect:/viaje/buscar"));
        verify(servicioReservaMock, times(1)).solicitarReserva(viajeMock, viajeroMock);
    }

    // --- TESTS DE LISTAR RESERVAS DE VIAJE ---

    @Test
    public void deberiaListarReservasDeViajeCorrectamente() throws NotFoundException, ViajeNoEncontradoException, UsuarioNoAutorizadoException {
        // given
        Long usuarioId = 1L;
        Long viajeId = 10L;

        Viaje viajeMock = new Viaje();
        viajeMock.setId(viajeId);

        Reserva reserva1 = new Reserva();
        reserva1.setId(1L);
        reserva1.setEstado(EstadoReserva.CONFIRMADA);

        Reserva reserva2 = new Reserva();
        reserva2.setId(2L);
        reserva2.setEstado(EstadoReserva.PENDIENTE);

        List<Reserva> reservas = Arrays.asList(reserva1, reserva2);

        when(sessionMock.getAttribute("idUsuario")).thenReturn(usuarioId);
        when(servicioViajeMock.obtenerViajePorId(viajeId)).thenReturn(viajeMock);
        when(servicioReservaMock.listarReservasPorViaje(viajeMock)).thenReturn(reservas);

        // when
        ModelAndView mav = controladorReserva.listarReservasDeViaje(viajeId, sessionMock);

        // then
        assertThat(mav.getViewName(), is("listarReservasViaje"));
        assertThat(mav.getModel().get("viaje"), is(viajeMock));
        assertThat(mav.getModel().get("reservas"), instanceOf(List.class));
        verify(servicioReservaMock, times(1)).listarReservasPorViaje(viajeMock);
    }

    @Test
    public void deberiaRedirigirALoginSiNoHaySesionEnListar() {
        // given
        when(sessionMock.getAttribute("idUsuario")).thenReturn(null);

        // when
        ModelAndView mav = controladorReserva.listarReservasDeViaje(1L, sessionMock);

        // then
        assertThat(mav.getViewName(), is("redirect:/login"));
        verify(servicioReservaMock, never()).listarReservasPorViaje(any());
    }

    // --- TESTS DE LISTAR MIS RESERVAS ---

    @Test
    public void deberiaListarMisReservasCorrectamente() throws UsuarioInexistente {
        // given
        Long usuarioId = 1L;
        Viajero viajeroMock = new Viajero();
        viajeroMock.setId(usuarioId);

        Reserva reserva1 = new Reserva();
        reserva1.setId(1L);
        reserva1.setEstado(EstadoReserva.CONFIRMADA);

        List<Reserva> reservas = Arrays.asList(reserva1);

        when(sessionMock.getAttribute("idUsuario")).thenReturn(usuarioId);
        when(servicioViajeroMock.obtenerViajero(usuarioId)).thenReturn(viajeroMock);
        when(servicioReservaMock.listarReservasPorViajero(viajeroMock)).thenReturn(reservas);

        // when
        ModelAndView mav = controladorReserva.listarMisReservas(sessionMock);

        // then
        assertThat(mav.getViewName(), is("misReservas"));
        assertThat(mav.getModel().get("reservas"), instanceOf(List.class));
        verify(servicioReservaMock, times(1)).listarReservasPorViajero(viajeroMock);
    }

    @Test
    public void deberiaRedirigirALoginSiNoHaySesionEnMisReservas() {
        // given
        when(sessionMock.getAttribute("idUsuario")).thenReturn(null);

        // when
        ModelAndView mav = controladorReserva.listarMisReservas(sessionMock);

        // then
        assertThat(mav.getViewName(), is("redirect:/login"));
        verify(servicioReservaMock, never()).listarReservasPorViajero(any());
    }
}
