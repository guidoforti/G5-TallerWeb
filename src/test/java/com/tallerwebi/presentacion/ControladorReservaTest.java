package com.tallerwebi.presentacion;

import com.mercadopago.resources.preference.Preference;
import com.tallerwebi.dominio.Entity.*;
import com.tallerwebi.dominio.Enums.EstadoDeViaje;
import com.tallerwebi.dominio.Enums.EstadoReserva;
import com.tallerwebi.dominio.IServicio.*;
import com.tallerwebi.dominio.excepcion.*;
import com.tallerwebi.presentacion.Controller.ControladorReserva;
import com.tallerwebi.presentacion.DTO.InputsDTO.MarcarAsistenciaInputDTO;
import com.tallerwebi.presentacion.DTO.InputsDTO.RechazoReservaInputDTO;
import com.tallerwebi.presentacion.DTO.InputsDTO.SolicitudReservaInputDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ReservaActivaDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ReservaVistaDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ViajeReservaSolicitudDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    private ServicioConductor servicioConductor;
    private RedirectAttributes redirectAttributesMock;
    private ServicioValoracion servicioValoracionMock;
    private ServicioNotificacion servicioNotificacionMock;

    @BeforeEach
    public void init() {
        servicioReservaMock = mock(ServicioReserva.class);
        servicioViajeMock = mock(ServicioViaje.class);
        servicioViajeroMock = mock(ServicioViajero.class);
        servicioConductor = mock(ServicioConductor.class);
        redirectAttributesMock = mock(RedirectAttributes.class);
        servicioValoracionMock = mock(ServicioValoracion.class);
        servicioNotificacionMock = mock(ServicioNotificacion.class);

        controladorReserva = new ControladorReserva(servicioReservaMock, servicioViajeMock, servicioViajeroMock, servicioConductor, servicioValoracionMock, servicioNotificacionMock);
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
        ModelAndView mav = controladorReserva.mostrarFormularioSolicitud(viajeId, sessionMock, null);

        // then
        assertThat(mav.getViewName(), is("solicitarReserva"));
        assertThat(mav.getModel().get("viaje"), instanceOf(ViajeReservaSolicitudDTO.class));
        assertThat(mav.getModel().get("solicitud"), instanceOf(SolicitudReservaInputDTO.class));
        verify(servicioViajeMock, times(1)).obtenerViajePorId(viajeId);
    }

    @Test
    public void deberiaRedirigirALoginSiNoHaySesionEnFormulario() throws ViajeNoEncontradoException, NotFoundException, UsuarioNoAutorizadoException {
        // given
        when(sessionMock.getAttribute("idUsuario")).thenReturn(null);

        // when
        ModelAndView mav = controladorReserva.mostrarFormularioSolicitud(1L, sessionMock, null);

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
        ModelAndView mav = controladorReserva.mostrarFormularioSolicitud(viajeId, sessionMock, redirectAttributesMock);

        // then
        assertThat(mav.getViewName(), is("redirect:/viaje/buscar"));
        verify(servicioViajeMock, times(1)).obtenerViajePorId(viajeId);
        verify(redirectAttributesMock, times(1)).addFlashAttribute("error", "Viaje no encontrado");
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
    public void deberiaRedirigirALoginSiNoHaySesionEnPost() throws ReservaYaExisteException, DatoObligatorioException, ViajeYaIniciadoException, SinAsientosDisponiblesException, ViajeNoEncontradoException, NotFoundException, UsuarioNoAutorizadoException, UsuarioInexistente {
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
    public void deberiaMostrarErrorSiReservaYaExiste() throws Exception {
        // given
        Long usuarioId = 1L;
        Long viajeId = 10L;
        SolicitudReservaInputDTO solicitudDTO = new SolicitudReservaInputDTO(viajeId, usuarioId);

        Viaje viajeMock = new Viaje();
        viajeMock.setId(viajeId);
        Viajero viajeroMock = new Viajero();

        when(sessionMock.getAttribute("idUsuario")).thenReturn(usuarioId);
        when(servicioViajeMock.obtenerViajePorId(viajeId)).thenReturn(viajeMock);
        when(servicioViajeroMock.obtenerViajero(usuarioId)).thenReturn(viajeroMock);
        when(servicioReservaMock.solicitarReserva(viajeMock, viajeroMock))
                .thenThrow(new ReservaYaExisteException("Ya existe una reserva"));

        // when
        ModelAndView mav = controladorReserva.solicitarReserva(solicitudDTO, sessionMock);

        // then
        assertThat(mav.getViewName(), is("solicitarReserva"));
        assertThat(mav.getModel().get("error"), is("Ya tienes una reserva para este viaje"));
        assertThat(mav.getModel().get("viaje"), instanceOf(ViajeReservaSolicitudDTO.class));
        assertThat(mav.getModel().get("solicitud"), instanceOf(SolicitudReservaInputDTO.class));
        verify(servicioReservaMock, times(1)).solicitarReserva(viajeMock, viajeroMock);
        verify(servicioViajeMock, times(2)).obtenerViajePorId(viajeId); // Once for attempt, once for re-display
    }

    @Test
    public void deberiaMostrarErrorSiNoHayAsientosDisponibles() throws Exception {
        // given
        Long usuarioId = 1L;
        Long viajeId = 10L;
        SolicitudReservaInputDTO solicitudDTO = new SolicitudReservaInputDTO(viajeId, usuarioId);

        Viaje viajeMock = new Viaje();
        viajeMock.setId(viajeId);
        Viajero viajeroMock = new Viajero();

        when(sessionMock.getAttribute("idUsuario")).thenReturn(usuarioId);
        when(servicioViajeMock.obtenerViajePorId(viajeId)).thenReturn(viajeMock);
        when(servicioViajeroMock.obtenerViajero(usuarioId)).thenReturn(viajeroMock);
        when(servicioReservaMock.solicitarReserva(viajeMock, viajeroMock))
                .thenThrow(new SinAsientosDisponiblesException("No hay asientos"));

        // when
        ModelAndView mav = controladorReserva.solicitarReserva(solicitudDTO, sessionMock);

        // then
        assertThat(mav.getViewName(), is("solicitarReserva"));
        assertThat(mav.getModel().get("error"), is("No hay asientos disponibles para este viaje"));
        assertThat(mav.getModel().get("viaje"), instanceOf(ViajeReservaSolicitudDTO.class));
        assertThat(mav.getModel().get("solicitud"), instanceOf(SolicitudReservaInputDTO.class));
        verify(servicioReservaMock, times(1)).solicitarReserva(viajeMock, viajeroMock);
        verify(servicioViajeMock, times(2)).obtenerViajePorId(viajeId);
    }

    @Test
    public void deberiaMostrarErrorSiViajeYaInicio() throws Exception {
        // given
        Long usuarioId = 1L;
        Long viajeId = 10L;
        SolicitudReservaInputDTO solicitudDTO = new SolicitudReservaInputDTO(viajeId, usuarioId);

        Viaje viajeMock = new Viaje();
        viajeMock.setId(viajeId);
        Viajero viajeroMock = new Viajero();

        when(sessionMock.getAttribute("idUsuario")).thenReturn(usuarioId);
        when(servicioViajeMock.obtenerViajePorId(viajeId)).thenReturn(viajeMock);
        when(servicioViajeroMock.obtenerViajero(usuarioId)).thenReturn(viajeroMock);
        when(servicioReservaMock.solicitarReserva(viajeMock, viajeroMock))
                .thenThrow(new ViajeYaIniciadoException("El viaje ya inició"));

        // when
        ModelAndView mav = controladorReserva.solicitarReserva(solicitudDTO, sessionMock);

        // then
        assertThat(mav.getViewName(), is("solicitarReserva"));
        assertThat(mav.getModel().get("error"), is("El viaje ya ha iniciado, no se pueden solicitar reservas"));
        assertThat(mav.getModel().get("viaje"), instanceOf(ViajeReservaSolicitudDTO.class));
        assertThat(mav.getModel().get("solicitud"), instanceOf(SolicitudReservaInputDTO.class));
        verify(servicioReservaMock, times(1)).solicitarReserva(viajeMock, viajeroMock);
        verify(servicioViajeMock, times(2)).obtenerViajePorId(viajeId);
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
        ModelAndView mav = controladorReserva.listarReservasDeViaje(viajeId, sessionMock, null);

        // then
        assertThat(mav.getViewName(), is("misReservas"));
        assertThat(mav.getModel().get("viaje"), is(viajeMock));
        assertThat(mav.getModel().get("reservas"), instanceOf(List.class));
        verify(servicioReservaMock, times(1)).listarReservasPorViaje(viajeMock);
    }

    @Test
    public void deberiaRedirigirALoginSiNoHaySesionEnListar() {
        // given
        when(sessionMock.getAttribute("idUsuario")).thenReturn(null);

        // when
        ModelAndView mav = controladorReserva.listarReservasDeViaje(1L, sessionMock, null);

        // then
        assertThat(mav.getViewName(), is("redirect:/login"));
        verify(servicioReservaMock, never()).listarReservasPorViaje(any());
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

    // --- TESTS DE CONFIRMAR RESERVA ---

    @Test
    public void deberiaConfirmarReservaExitosamente() throws Exception {
        // given
        Long conductorId = 1L;
        Long reservaId = 10L;

        when(sessionMock.getAttribute("idUsuario")).thenReturn(conductorId);
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");
        doNothing().when(servicioReservaMock).confirmarReserva(reservaId, conductorId);
        when(servicioConductor.obtenerConductor(conductorId)).thenReturn(null);
        when(servicioViajeMock.listarViajesPorConductor(any())).thenReturn(Arrays.asList());

        // when
        ModelAndView mav = controladorReserva.confirmarReserva(reservaId, sessionMock);

        // then
        assertThat(mav.getViewName(), is("misReservas"));
        assertThat(mav.getModel().get("mensaje"), is("Reserva confirmada exitosamente"));
        assertThat(mav.getModel().get("reservas"), instanceOf(List.class));
        verify(servicioReservaMock, times(1)).confirmarReserva(reservaId, conductorId);
    }

    @Test
    public void deberiaRedirigirALoginSiNoHaySesionEnConfirmar() throws Exception {
        // given
        when(sessionMock.getAttribute("idUsuario")).thenReturn(null);

        // when
        ModelAndView mav = controladorReserva.confirmarReserva(1L, sessionMock);

        // then
        assertThat(mav.getViewName(), is("redirect:/login"));
        verify(servicioReservaMock, never()).confirmarReserva(anyLong(), anyLong());
    }

    @Test
    public void deberiaMostrarErrorSiReservaNoExisteAlConfirmar() throws Exception {
        // given
        Long conductorId = 1L;
        Long reservaId = 999L;

        when(sessionMock.getAttribute("idUsuario")).thenReturn(conductorId);
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");
        doThrow(new NotFoundException("No se encontró la reserva"))
                .when(servicioReservaMock).confirmarReserva(reservaId, conductorId);
        when(servicioConductor.obtenerConductor(conductorId)).thenReturn(null);
        when(servicioViajeMock.listarViajesPorConductor(any())).thenReturn(Arrays.asList());

        // when
        ModelAndView mav = controladorReserva.confirmarReserva(reservaId, sessionMock);

        // then
        assertThat(mav.getViewName(), is("misReservas"));
        assertThat(mav.getModel().get("error"), is("No se encontró la reserva"));
        assertThat(mav.getModel().get("reservas"), instanceOf(List.class));
        verify(servicioReservaMock, times(1)).confirmarReserva(reservaId, conductorId);
    }

    @Test
    public void deberiaMostrarErrorSiConductorNoAutorizadoAlConfirmar() throws Exception {
        // given
        Long conductorId = 1L;
        Long reservaId = 10L;

        when(sessionMock.getAttribute("idUsuario")).thenReturn(conductorId);
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");
        doThrow(new UsuarioNoAutorizadoException("No tienes permiso"))
                .when(servicioReservaMock).confirmarReserva(reservaId, conductorId);
        when(servicioConductor.obtenerConductor(conductorId)).thenReturn(null);
        when(servicioViajeMock.listarViajesPorConductor(any())).thenReturn(Arrays.asList());

        // when
        ModelAndView mav = controladorReserva.confirmarReserva(reservaId, sessionMock);

        // then
        assertThat(mav.getViewName(), is("misReservas"));
        assertThat(mav.getModel().get("error"), is("No tienes permiso"));
        assertThat(mav.getModel().get("reservas"), instanceOf(List.class));
        verify(servicioReservaMock, times(1)).confirmarReserva(reservaId, conductorId);
    }

    @Test
    public void deberiaMostrarErrorSiNoHayAsientosDisponiblesAlConfirmar() throws Exception {
        // given
        Long conductorId = 1L;
        Long reservaId = 10L;

        when(sessionMock.getAttribute("idUsuario")).thenReturn(conductorId);
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");
        doThrow(new SinAsientosDisponiblesException("No hay asientos disponibles"))
                .when(servicioReservaMock).confirmarReserva(reservaId, conductorId);
        when(servicioConductor.obtenerConductor(conductorId)).thenReturn(null);
        when(servicioViajeMock.listarViajesPorConductor(any())).thenReturn(Arrays.asList());

        // when
        ModelAndView mav = controladorReserva.confirmarReserva(reservaId, sessionMock);

        // then
        assertThat(mav.getViewName(), is("misReservas"));
        assertThat(mav.getModel().get("error"), is("No hay asientos disponibles"));
        assertThat(mav.getModel().get("reservas"), instanceOf(List.class));
        verify(servicioReservaMock, times(1)).confirmarReserva(reservaId, conductorId);
    }

    // --- TESTS DE RECHAZAR RESERVA (GET - MOSTRAR FORMULARIO) ---

    @Test
    public void deberiaMostrarFormularioRechazo() {
        // given
        Long conductorId = 1L;
        Long reservaId = 10L;

        when(sessionMock.getAttribute("idUsuario")).thenReturn(conductorId);

        // when
        ModelAndView mav = controladorReserva.mostrarFormularioRechazo(reservaId, sessionMock);

        // then
        assertThat(mav.getViewName(), is("rechazarReserva"));
        assertThat(mav.getModel().get("rechazoDTO"), instanceOf(RechazoReservaInputDTO.class));
        RechazoReservaInputDTO dto = (RechazoReservaInputDTO) mav.getModel().get("rechazoDTO");
        assertThat(dto.getReservaId(), is(reservaId));
    }

    @Test
    public void deberiaRedirigirALoginSiNoHaySesionEnFormularioRechazo() {
        // given
        when(sessionMock.getAttribute("idUsuario")).thenReturn(null);

        // when
        ModelAndView mav = controladorReserva.mostrarFormularioRechazo(1L, sessionMock);

        // then
        assertThat(mav.getViewName(), is("redirect:/login"));
    }

    // --- TESTS DE RECHAZAR RESERVA (POST - PROCESAR RECHAZO) ---

    @Test
    public void deberiaRechazarReservaExitosamente() throws Exception {
        // given
        Long conductorId = 1L;
        RechazoReservaInputDTO rechazoDTO = new RechazoReservaInputDTO(10L, "No hay espacio suficiente");

        when(sessionMock.getAttribute("idUsuario")).thenReturn(conductorId);
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");
        doNothing().when(servicioReservaMock).rechazarReserva(rechazoDTO.getReservaId(), conductorId, rechazoDTO.getMotivo());
        when(servicioConductor.obtenerConductor(conductorId)).thenReturn(null);
        when(servicioViajeMock.listarViajesPorConductor(any())).thenReturn(Arrays.asList());

        // when
        ModelAndView mav = controladorReserva.rechazarReserva(rechazoDTO, sessionMock);

        // then
        assertThat(mav.getViewName(), is("misReservas"));
        assertThat(mav.getModel().get("mensaje"), is("Reserva rechazada exitosamente"));
        assertThat(mav.getModel().get("reservas"), instanceOf(List.class));
        verify(servicioReservaMock, times(1)).rechazarReserva(rechazoDTO.getReservaId(), conductorId, rechazoDTO.getMotivo());
    }

    @Test
    public void deberiaRedirigirALoginSiNoHaySesionEnRechazar() throws Exception {
        // given
        RechazoReservaInputDTO rechazoDTO = new RechazoReservaInputDTO(10L, "Motivo");
        when(sessionMock.getAttribute("idUsuario")).thenReturn(null);

        // when
        ModelAndView mav = controladorReserva.rechazarReserva(rechazoDTO, sessionMock);

        // then
        assertThat(mav.getViewName(), is("redirect:/login"));
        verify(servicioReservaMock, never()).rechazarReserva(anyLong(), anyLong(), anyString());
    }

    @Test
    public void deberiaMostrarErrorSiMotivoVacioAlRechazar() throws Exception {
        // given
        Long conductorId = 1L;
        RechazoReservaInputDTO rechazoDTO = new RechazoReservaInputDTO(10L, "");

        when(sessionMock.getAttribute("idUsuario")).thenReturn(conductorId);
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");
        doThrow(new DatoObligatorioException("El motivo del rechazo es obligatorio"))
                .when(servicioReservaMock).rechazarReserva(rechazoDTO.getReservaId(), conductorId, rechazoDTO.getMotivo());

        // when
        ModelAndView mav = controladorReserva.rechazarReserva(rechazoDTO, sessionMock);

        // then
        assertThat(mav.getViewName(), is("rechazarReserva"));
        assertThat(mav.getModel().get("error"), is("El motivo del rechazo es obligatorio"));
        assertThat(mav.getModel().get("rechazoDTO"), is(rechazoDTO));
        verify(servicioReservaMock, times(1)).rechazarReserva(rechazoDTO.getReservaId(), conductorId, rechazoDTO.getMotivo());
    }

    @Test
    public void deberiaMostrarErrorSiReservaNoExisteAlRechazar() throws Exception {
        // given
        Long conductorId = 1L;
        RechazoReservaInputDTO rechazoDTO = new RechazoReservaInputDTO(999L, "Motivo");

        when(sessionMock.getAttribute("idUsuario")).thenReturn(conductorId);
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");
        doThrow(new NotFoundException("No se encontró la reserva"))
                .when(servicioReservaMock).rechazarReserva(rechazoDTO.getReservaId(), conductorId, rechazoDTO.getMotivo());

        // when
        ModelAndView mav = controladorReserva.rechazarReserva(rechazoDTO, sessionMock);

        // then
        assertThat(mav.getViewName(), is("rechazarReserva"));
        assertThat(mav.getModel().get("error"), is("No se encontró la reserva"));
        assertThat(mav.getModel().get("rechazoDTO"), is(rechazoDTO));
        verify(servicioReservaMock, times(1)).rechazarReserva(rechazoDTO.getReservaId(), conductorId, rechazoDTO.getMotivo());
    }

    @Test
    public void deberiaMostrarErrorSiConductorNoAutorizadoAlRechazar() throws Exception {
        // given
        Long conductorId = 1L;
        RechazoReservaInputDTO rechazoDTO = new RechazoReservaInputDTO(10L, "Motivo");

        when(sessionMock.getAttribute("idUsuario")).thenReturn(conductorId);
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");
        doThrow(new UsuarioNoAutorizadoException("No tienes permiso"))
                .when(servicioReservaMock).rechazarReserva(rechazoDTO.getReservaId(), conductorId, rechazoDTO.getMotivo());

        // when
        ModelAndView mav = controladorReserva.rechazarReserva(rechazoDTO, sessionMock);

        // then
        assertThat(mav.getViewName(), is("rechazarReserva"));
        assertThat(mav.getModel().get("error"), is("No tienes permiso"));
        assertThat(mav.getModel().get("rechazoDTO"), is(rechazoDTO));
        verify(servicioReservaMock, times(1)).rechazarReserva(rechazoDTO.getReservaId(), conductorId, rechazoDTO.getMotivo());
    }

    // --- TESTS DE LISTAR RESERVAS PENDIENTES Y RECHAZADAS (VIAJERO) ---

    @Test
    public void deberiaMostrarReservasPendientesYRechazadasCuandoViajeroLogueado() throws Exception {
        // given
        Long viajeroId = 1L;
        List<Reserva> reservas = Arrays.asList(new Reserva(), new Reserva());

        when(sessionMock.getAttribute("idUsuario")).thenReturn(viajeroId);
        when(sessionMock.getAttribute("ROL")).thenReturn("VIAJERO");
        when(servicioReservaMock.listarReservasActivasPorViajero(viajeroId)).thenReturn(reservas);

        // when
        ModelAndView mav = controladorReserva.listarReservasActivas(sessionMock);

        // then
        assertThat(mav.getViewName(), is("misReservasActivas"));
        assertThat(mav.getModel().get("reservasPendientes"), notNullValue());
        assertThat(mav.getModel().get("reservasRechazadas"), notNullValue());
        assertThat(mav.getModel().get("reservasConfirmadas"), notNullValue());
        verify(servicioReservaMock, times(1)).listarReservasActivasPorViajero(viajeroId);
    }

    @Test
    public void deberiaRedirigirALoginSiNoHaySesionEnReservasPendientes() throws Exception {
        // given
        when(sessionMock.getAttribute("idUsuario")).thenReturn(null);

        // when
        ModelAndView mav = controladorReserva.listarReservasActivas(sessionMock);

        // then
        assertThat(mav.getViewName(), is("redirect:/login"));
        verify(servicioReservaMock, never()).listarReservasActivasPorViajero(anyLong());
    }

    @Test
    public void deberiaRedirigirALoginSiRolNoEsViajeroEnReservasPendientes() throws Exception {
        // given
        when(sessionMock.getAttribute("idUsuario")).thenReturn(1L);
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");

        // when
        ModelAndView mav = controladorReserva.listarReservasActivas(sessionMock);

        // then
        assertThat(mav.getViewName(), is("redirect:/login"));
        verify(servicioReservaMock, never()).listarReservasActivasPorViajero(anyLong());
    }

    @Test
    public void deberiaMostrarErrorSiViajeroNoExisteEnReservasPendientes() throws Exception {
        // given
        Long viajeroId = 999L;

        when(sessionMock.getAttribute("idUsuario")).thenReturn(viajeroId);
        when(sessionMock.getAttribute("ROL")).thenReturn("VIAJERO");
        when(servicioReservaMock.listarReservasActivasPorViajero(viajeroId))
                .thenThrow(new UsuarioInexistente("No se encontró el viajero"));

        // when
        ModelAndView mav = controladorReserva.listarReservasActivas(sessionMock);

        // then
        assertThat(mav.getViewName(), is("error"));
        assertThat(mav.getModel().get("error"), is("No se encontró el viajero"));
        verify(servicioReservaMock, times(1)).listarReservasActivasPorViajero(viajeroId);
    }

    // --- TESTS DE LISTAR MIS VIAJES (VIAJERO) ---

    @Test
    public void deberiaMostrarMisViajesCuandoViajeroLogueado() throws Exception {
        // given
        Long viajeroId = 1L;

        // Crear viajes con estados para categorización
        Viaje viajeProximo = new Viaje();
        viajeProximo.setEstado(EstadoDeViaje.DISPONIBLE);
        viajeProximo.setFechaHoraDeSalida(java.time.LocalDateTime.now().plusDays(1));

        Viaje viajeEnCurso = new Viaje();
        viajeEnCurso.setEstado(EstadoDeViaje.EN_CURSO);
        viajeEnCurso.setFechaHoraDeSalida(java.time.LocalDateTime.now());

        Reserva reserva1 = new Reserva();
        reserva1.setViaje(viajeProximo);

        Reserva reserva2 = new Reserva();
        reserva2.setViaje(viajeEnCurso);

        List<Reserva> reservas = Arrays.asList(reserva1, reserva2);

        when(sessionMock.getAttribute("idUsuario")).thenReturn(viajeroId);
        when(sessionMock.getAttribute("ROL")).thenReturn("VIAJERO");
        when(servicioReservaMock.listarViajesConfirmadosPorViajero(viajeroId)).thenReturn(reservas);

        // when
        ModelAndView mav = controladorReserva.listarMisViajes(sessionMock);

        // then
        assertThat(mav.getViewName(), is("misViajes"));
        assertThat(mav.getModel().get("viajesProximos"), notNullValue());
        assertThat(mav.getModel().get("viajesEnCurso"), notNullValue());
        assertThat(mav.getModel().get("viajesFinalizados"), notNullValue());
        verify(servicioReservaMock, times(1)).listarViajesConfirmadosPorViajero(viajeroId);
    }

    @Test
    public void deberiaRedirigirALoginSiNoHaySesionEnMisViajes() throws Exception {
        // given
        when(sessionMock.getAttribute("idUsuario")).thenReturn(null);

        // when
        ModelAndView mav = controladorReserva.listarMisViajes(sessionMock);

        // then
        assertThat(mav.getViewName(), is("redirect:/login"));
        verify(servicioReservaMock, never()).listarViajesConfirmadosPorViajero(anyLong());
    }

    @Test
    public void deberiaRedirigirALoginSiRolNoEsViajeroEnMisViajes() throws Exception {
        // given
        when(sessionMock.getAttribute("idUsuario")).thenReturn(1L);
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");

        // when
        ModelAndView mav = controladorReserva.listarMisViajes(sessionMock);

        // then
        assertThat(mav.getViewName(), is("redirect:/login"));
        verify(servicioReservaMock, never()).listarViajesConfirmadosPorViajero(anyLong());
    }

    @Test
    public void deberiaMostrarErrorSiViajeroNoExisteEnMisViajes() throws Exception {
        // given
        Long viajeroId = 999L;

        when(sessionMock.getAttribute("idUsuario")).thenReturn(viajeroId);
        when(sessionMock.getAttribute("ROL")).thenReturn("VIAJERO");
        when(servicioReservaMock.listarViajesConfirmadosPorViajero(viajeroId))
                .thenThrow(new UsuarioInexistente("No se encontró el viajero"));

        // when
        ModelAndView mav = controladorReserva.listarMisViajes(sessionMock);

        // then
        assertThat(mav.getViewName(), is("error"));
        assertThat(mav.getModel().get("error"), is("No se encontró el viajero"));
        verify(servicioReservaMock, times(1)).listarViajesConfirmadosPorViajero(viajeroId);
    }


    @Test
    public void deberiaRedirigirAMercadoPagoSiLaSesionEsValidaYLaPreferenciaSeCrea() throws Exception {
        // Given (Arrange)
        Long reservaId = 1L;
        Long viajeroId = 10L;
        String urlMp = "http://mercadopago.com/checkout/123";

        // Simulamos una sesión de VIAJERO válida
        when(sessionMock.getAttribute("idUsuario")).thenReturn(viajeroId);
        when(sessionMock.getAttribute("ROL")).thenReturn("VIAJERO");

        // Simulamos la creación exitosa de la preferencia
        Preference preferenciaMock = mock(Preference.class);
        when(preferenciaMock.getInitPoint()).thenReturn(urlMp);
        when(servicioReservaMock.crearPreferenciaDePago(reservaId, viajeroId)).thenReturn(preferenciaMock);

        // When (Act)
        ModelAndView mav = controladorReserva.pagarReservar(sessionMock, reservaId, redirectAttributesMock);

        // Then (Assert)
        // Verificamos que la vista sea un redirect a la URL de MP
        assertThat(mav.getViewName(), equalTo("redirect:" + urlMp));
        verify(servicioReservaMock, times(1)).crearPreferenciaDePago(reservaId, viajeroId);
    }

    @Test
    public void deberiaRedirigirALoginSiUsuarioNoEstaLogueadoAlPagar() throws Exception {
        // Given (Arrange)
        when(sessionMock.getAttribute("idUsuario")).thenReturn(null); // No hay sesión

        // When (Act)
        ModelAndView mav = controladorReserva.pagarReservar(sessionMock, 1L, redirectAttributesMock);

        // Then (Assert)
        assertThat(mav.getViewName(), equalTo("redirect:/login"));
        verify(servicioReservaMock, never()).crearPreferenciaDePago(anyLong(), anyLong());
    }

    @Test
    public void deberiaRedirigirALoginSiUsuarioNoEsViajeroAlPagar() throws Exception {
        // Given (Arrange)
        when(sessionMock.getAttribute("idUsuario")).thenReturn(1L);
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR"); // Rol incorrecto

        // When (Act)
        ModelAndView mav = controladorReserva.pagarReservar(sessionMock, 1L, redirectAttributesMock);

        // Then (Assert)
        assertThat(mav.getViewName(), equalTo("redirect:/login"));
        verify(servicioReservaMock, never()).crearPreferenciaDePago(anyLong(), anyLong());
    }

    @Test
    public void deberiaRedirigirAMisReservasConErrorSiServicioLanzaExcepcionDeNegocio() throws Exception {
        // Given (Arrange)
        Long reservaId = 1L;
        Long viajeroId = 10L;
        when(sessionMock.getAttribute("idUsuario")).thenReturn(viajeroId);
        when(sessionMock.getAttribute("ROL")).thenReturn("VIAJERO");

        // Simulamos que el servicio falla (ej. reserva ya pagada)
        when(servicioReservaMock.crearPreferenciaDePago(reservaId, viajeroId))
                .thenThrow(new AccionNoPermitidaException("La reserva ya se encuentra abonada"));

        // When (Act)
        ModelAndView mav = controladorReserva.pagarReservar(sessionMock, reservaId, redirectAttributesMock);

        // Then (Assert)
        assertThat(mav.getViewName(), equalTo("redirect:/reserva/misReservasActivas"));
        // Verificamos que el error se guarda en el "post-it" (RedirectAttributes)
        verify(redirectAttributesMock, times(1)).addFlashAttribute("error", "Error al iniciar el pago: La reserva ya se encuentra abonada");
    }

    @Test
    public void deberiaConfirmarPagoYMostrarVistaExitosa() throws Exception {
        // Given
        Long reservaId = 1L;
        Long viajeroId = 10L;
        when(sessionMock.getAttribute("idUsuario")).thenReturn(viajeroId);
        when(sessionMock.getAttribute("ROL")).thenReturn("VIAJERO");

        // Simulamos la reserva que se devuelve
        Reserva reservaMock = crearReservaCompletaMock(reservaId, viajeroId);
        when(servicioReservaMock.confirmarPagoReserva(reservaId, viajeroId)).thenReturn(reservaMock);

        // When
        ModelAndView mav = controladorReserva.devolverPagoExitoso(sessionMock, reservaId, redirectAttributesMock);

        // Then
        assertThat(mav.getViewName(), equalTo("pagoExitoso"));
        assertThat(mav.getModel().get("pagoOK"), equalTo("El pago fue exitoso"));
        assertThat(mav.getModel().get("reserva"), instanceOf(ReservaActivaDTO.class));
        verify(servicioReservaMock, times(1)).confirmarPagoReserva(reservaId, viajeroId);
    }

    @Test
    public void deberiaRedirigirAMisReservasSiConfirmacionDePagoFalla() throws Exception {
        // Given
        Long reservaId = 1L;
        Long viajeroId = 10L;
        when(sessionMock.getAttribute("idUsuario")).thenReturn(viajeroId);
        when(sessionMock.getAttribute("ROL")).thenReturn("VIAJERO");

        // Simulamos que el usuario logueado no es el dueño de la reserva
        when(servicioReservaMock.confirmarPagoReserva(reservaId, viajeroId))
                .thenThrow(new UsuarioNoAutorizadoException("No tienes permiso"));

        // When
        ModelAndView mav = controladorReserva.devolverPagoExitoso(sessionMock, reservaId, redirectAttributesMock);

        // Then
        assertThat(mav.getViewName(), equalTo("redirect:/reserva/misReservasActivas"));
        verify(redirectAttributesMock, times(1)).addFlashAttribute("error", "No tienes permiso");
    }

    @Test
    public void deberiaMostrarVistaDePagoFallido() throws Exception {
        // Given
        Long reservaId = 1L;
        Long viajeroId = 10L;
        when(sessionMock.getAttribute("idUsuario")).thenReturn(viajeroId);
        when(sessionMock.getAttribute("ROL")).thenReturn("VIAJERO");

        Reserva reservaMock = crearReservaCompletaMock(reservaId, viajeroId);
        // Usamos el método seguro que valida pertenencia (como en tu código)
        when(servicioReservaMock.obtenerReservaPorId(reservaId)).thenReturn(reservaMock);

        // When
        ModelAndView mav = controladorReserva.devolverPagoFallido(sessionMock, reservaId, redirectAttributesMock);

        // Then
        assertThat(mav.getViewName(), equalTo("pagoFallido"));
        assertThat(mav.getModel().get("error"), equalTo("Tu pago fue rechazado o cancelado."));
        assertThat(mav.getModel().get("reserva"), instanceOf(ReservaActivaDTO.class));
        verify(servicioReservaMock, times(1)).obtenerReservaPorId(reservaId);
    }


    @Test
    public void deberiaMostrarVistaDePagoPendiente() throws Exception {
        // Given
        Long reservaId = 1L;
        Long viajeroId = 10L;
        when(sessionMock.getAttribute("idUsuario")).thenReturn(viajeroId);
        when(sessionMock.getAttribute("ROL")).thenReturn("VIAJERO");

        Reserva reservaMock = crearReservaCompletaMock(reservaId, viajeroId);
        when(servicioReservaMock.obtenerReservaPorId(reservaId)).thenReturn(reservaMock);

        // When
        ModelAndView mav = controladorReserva.devolverPagoPendiente(sessionMock, reservaId, redirectAttributesMock);

        // Then
        assertThat(mav.getViewName(), equalTo("pagoPendiente"));
        assertThat(mav.getModel().get("error"), equalTo("Tu pago esta pendiente"));
        assertThat(mav.getModel().get("reserva"), instanceOf(ReservaActivaDTO.class));
        verify(servicioReservaMock, times(1)).obtenerReservaPorId(reservaId);
    }



    private Reserva crearReservaCompletaMock(Long reservaId, Long viajeroId) {
        // Mockeamos solo lo que el DTO 'ReservaActivaDTO' necesita leer
        Viaje viajeMock = mock(Viaje.class);
        when(viajeMock.getOrigen()).thenReturn(mock(Ciudad.class));
        when(viajeMock.getDestino()).thenReturn(mock(Ciudad.class));
        when(viajeMock.getFechaHoraDeSalida()).thenReturn(LocalDateTime.now());
        when(viajeMock.getPrecio()).thenReturn(100.0);
        when(viajeMock.getConductor()).thenReturn(mock(Conductor.class));

        Viajero viajeroMock = mock(Viajero.class);
        when(viajeroMock.getId()).thenReturn(viajeroId);

        Reserva reservaMock = mock(Reserva.class);
        when(reservaMock.getId()).thenReturn(reservaId);
        when(reservaMock.getViaje()).thenReturn(viajeMock);
        when(reservaMock.getViajero()).thenReturn(viajeroMock);
        when(reservaMock.getEstado()).thenReturn(EstadoReserva.CONFIRMADA);

        return reservaMock;
    }

    @Test
    public void deberiaMostrarErrorGenericoYRecargarFormulario() throws Exception {
        // given
        Long usuarioId = 1L;
        Long viajeId = 10L;
        SolicitudReservaInputDTO solicitudDTO = new SolicitudReservaInputDTO(viajeId, usuarioId);

        Viaje viajeMock = new Viaje();
        viajeMock.setId(viajeId);
        Viajero viajeroMock = new Viajero();

        when(sessionMock.getAttribute("idUsuario")).thenReturn(usuarioId);
        when(servicioViajeMock.obtenerViajePorId(viajeId)).thenReturn(viajeMock);
        when(servicioViajeroMock.obtenerViajero(usuarioId)).thenReturn(viajeroMock);
        // Simular una excepción genérica que cae al catch
        when(servicioReservaMock.solicitarReserva(viajeMock, viajeroMock))
                .thenThrow(new DatoObligatorioException("Faltan datos"));

        // when
        ModelAndView mav = controladorReserva.solicitarReserva(solicitudDTO, sessionMock);

        // then
        assertThat(mav.getViewName(), is("solicitarReserva"));
        assertThat(mav.getModel().get("error"), is("Faltan datos"));
        verify(servicioViajeMock, times(2)).obtenerViajePorId(viajeId); // 1st try, 2nd for re-display
    }

    @Test
    public void deberiaRedirigirABuscarSiFallaRecargarFormulario() throws Exception {
        // given
        Long usuarioId = 1L;
        Long viajeId = 10L;
        SolicitudReservaInputDTO solicitudDTO = new SolicitudReservaInputDTO(viajeId, usuarioId);

        Viaje viajeMock = new Viaje();
        viajeMock.setId(viajeId);
        Viajero viajeroMock = new Viajero();

        when(sessionMock.getAttribute("idUsuario")).thenReturn(usuarioId);
        when(servicioViajeMock.obtenerViajePorId(viajeId)).thenReturn(viajeMock);
        when(servicioViajeroMock.obtenerViajero(usuarioId)).thenReturn(viajeroMock);
        // Simular una excepción de negocio
        when(servicioReservaMock.solicitarReserva(viajeMock, viajeroMock))
                .thenThrow(new UsuarioNoAutorizadoException("No autorizado"));

        // Simular que FALLA al obtener el viaje para el re-display (cae al catch final)
        when(servicioViajeMock.obtenerViajePorId(viajeId)).thenThrow(new ViajeNoEncontradoException("No existe"));

        // when
        ModelAndView mav = controladorReserva.solicitarReserva(solicitudDTO, sessionMock);

        // then
        assertThat(mav.getViewName(), is("redirect:/viaje/buscar"));
        // Se llama 1 vez para el intento de reserva, y 1 vez para el intento de re-display (que falla)
        verify(servicioViajeMock, times(2)).obtenerViajePorId(viajeId);
    }

    // --- TESTS DE LISTAR RESERVAS DE VIAJE (GET /listar) ---

    @Test
    public void deberiaRedirigirABuscarSiViajeNoExisteEnListarReservas() throws NotFoundException, ViajeNoEncontradoException, UsuarioNoAutorizadoException {
        // given
        Long usuarioId = 1L;
        Long viajeId = 999L;

        when(sessionMock.getAttribute("idUsuario")).thenReturn(usuarioId);
        when(servicioViajeMock.obtenerViajePorId(viajeId)).thenThrow(new NotFoundException("Viaje no encontrado"));

        // when
        ModelAndView mav = controladorReserva.listarReservasDeViaje(viajeId, sessionMock, redirectAttributesMock);

        // then
        assertThat(mav.getViewName(), is("redirect:/viaje/buscar"));
        verify(servicioViajeMock, times(1)).obtenerViajePorId(viajeId);
        verify(redirectAttributesMock, times(1)).addFlashAttribute("error", "Viaje no encontrado");
    }

    // --- TESTS DE LISTAR MIS RESERVAS (GET /misReservas - CONDUCTOR) ---

    @Test
    public void deberiaRedirigirALoginSiRolNoEsConductorEnMisReservas() throws Exception {
        // given
        when(sessionMock.getAttribute("idUsuario")).thenReturn(1L);
        when(sessionMock.getAttribute("ROL")).thenReturn("VIAJERO");

        // when
        ModelAndView mav = controladorReserva.listarMisReservas(sessionMock);

        // then
        assertThat(mav.getViewName(), is("redirect:/login"));
        verify(servicioConductor, never()).obtenerConductor(anyLong());
    }

    @Test
    public void deberiaMostrarErrorSiConductorInexistenteEnMisReservas() throws Exception {
        // given
        Long conductorId = 999L;
        when(sessionMock.getAttribute("idUsuario")).thenReturn(conductorId);
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");

        // Simular la falla del servicio de conductor
        when(servicioConductor.obtenerConductor(conductorId)).thenThrow(new UsuarioInexistente("Conductor no encontrado"));

        // when
        ModelAndView mav = controladorReserva.listarMisReservas(sessionMock);

        // then
        assertThat(mav.getViewName(), is("error"));
        assertThat(mav.getModel().get("error").toString(), is("Conductor no encontrado"));
        verify(servicioConductor, times(1)).obtenerConductor(conductorId);
    }

    // --- TESTS DE CONFIRMAR RESERVA (POST /confirmar) ---

    @Test
    public void deberiaRedirigirSiFallaRecargaDatosAlConfirmar() throws Exception {
        // given
        Long conductorId = 1L;
        Long reservaId = 10L;

        when(sessionMock.getAttribute("idUsuario")).thenReturn(conductorId);
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");
        doNothing().when(servicioReservaMock).confirmarReserva(reservaId, conductorId);

        // Simular que la recarga de datos falla (e.g., el conductor ya no existe)
        when(servicioConductor.obtenerConductor(conductorId)).thenThrow(new UsuarioInexistente("Conductor eliminado"));

        // when
        ModelAndView mav = controladorReserva.confirmarReserva(reservaId, sessionMock);

        // then
        assertThat(mav.getViewName(), is("redirect:/reserva/misReservas"));
        verify(servicioReservaMock, times(1)).confirmarReserva(reservaId, conductorId);
        verify(servicioConductor, times(1)).obtenerConductor(conductorId);
    }

    // --- TESTS DE RECHAZAR RESERVA (POST /rechazar) ---

    @Test
    public void deberiaMostrarErrorSiReservaYaFueModificadaAlRechazar() throws Exception {
        // given
        Long conductorId = 1L;
        RechazoReservaInputDTO rechazoDTO = new RechazoReservaInputDTO(10L, "Motivo");

        when(sessionMock.getAttribute("idUsuario")).thenReturn(conductorId);
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");
        doThrow(new ReservaYaExisteException("La reserva ya fue gestionada"))
                .when(servicioReservaMock).rechazarReserva(rechazoDTO.getReservaId(), conductorId, rechazoDTO.getMotivo());

        // when
        ModelAndView mav = controladorReserva.rechazarReserva(rechazoDTO, sessionMock);

        // then
        assertThat(mav.getViewName(), is("rechazarReserva"));
        assertThat(mav.getModel().get("error"), is("La reserva ya fue gestionada"));
        verify(servicioReservaMock, times(1)).rechazarReserva(rechazoDTO.getReservaId(), conductorId, rechazoDTO.getMotivo());
    }

    @Test
    public void deberiaRedirigirSiFallaRecargaDatosAlRechazar() throws Exception {
        // given
        Long conductorId = 1L;
        RechazoReservaInputDTO rechazoDTO = new RechazoReservaInputDTO(10L, "Motivo");

        when(sessionMock.getAttribute("idUsuario")).thenReturn(conductorId);
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");
        doNothing().when(servicioReservaMock).rechazarReserva(rechazoDTO.getReservaId(), conductorId, rechazoDTO.getMotivo());

        // Simular que la recarga de datos falla (e.g., el conductor ya no existe)
        when(servicioConductor.obtenerConductor(conductorId)).thenThrow(new UsuarioInexistente("Conductor eliminado"));

        // when
        ModelAndView mav = controladorReserva.rechazarReserva(rechazoDTO, sessionMock);

        // then
        assertThat(mav.getViewName(), is("redirect:/reserva/misReservas"));
        verify(servicioReservaMock, times(1)).rechazarReserva(rechazoDTO.getReservaId(), conductorId, rechazoDTO.getMotivo());
        verify(servicioConductor, times(1)).obtenerConductor(conductorId);
    }

    // --- TESTS DE MARCAR ASISTENCIA (POST /marcarAsistencia) ---

    @Test
    public void marcarAsistencia_deberiaRedirigirAMisReservasSiNoSePuedeObtenerViajeId() throws NotFoundException, UsuarioNoAutorizadoException, ReservaYaExisteException, AccionNoPermitidaException, DatoObligatorioException {
        // given
        Long conductorId = 1L;
        Long reservaId = 999L;
        MarcarAsistenciaInputDTO inputDTO = new MarcarAsistenciaInputDTO(reservaId, "PRESENTE");

        when(sessionMock.getAttribute("idUsuario")).thenReturn(conductorId);

        // Simular que obtenerReservaPorId falla (viajeId = null)
        when(servicioReservaMock.obtenerReservaPorId(reservaId)).thenThrow(new NotFoundException("Reserva no encontrada"));

        // when
        ModelAndView mav = controladorReserva.marcarAsistencia(inputDTO, sessionMock);

        // then
        assertThat(mav.getViewName(), is("redirect:/reserva/misReservas"));
        verify(servicioReservaMock, never()).marcarAsistencia(anyLong(), anyLong(), anyString());
    }

    @Test
    public void marcarAsistencia_deberiaMostrarErrorSiAccionNoPermitida() throws Exception {
        // given
        Long conductorId = 1L;
        Long reservaId = 10L;
        Long viajeId = 100L;
        MarcarAsistenciaInputDTO inputDTO = new MarcarAsistenciaInputDTO(reservaId, "PRESENTE");

        Reserva reservaMock = mock(Reserva.class);
        Viaje viajeMock = mock(Viaje.class);
        when(reservaMock.getViaje()).thenReturn(viajeMock);
        when(viajeMock.getId()).thenReturn(viajeId);

        when(sessionMock.getAttribute("idUsuario")).thenReturn(conductorId);
        when(servicioReservaMock.obtenerReservaPorId(reservaId)).thenReturn(reservaMock);

        doThrow(new AccionNoPermitidaException("El viaje no está en curso"))
                .when(servicioReservaMock).marcarAsistencia(reservaId, conductorId, inputDTO.getAsistencia());

        // Simular recarga de datos (debe ser exitosa)
        when(servicioViajeMock.obtenerViajePorId(viajeId)).thenReturn(viajeMock);
        when(servicioReservaMock.listarViajerosConfirmados(viajeId, conductorId)).thenReturn(Arrays.asList());

        // when
        ModelAndView mav = controladorReserva.marcarAsistencia(inputDTO, sessionMock);

        // then
        assertThat(mav.getViewName(), is("viajerosConfirmados"));
        assertThat(mav.getModel().get("error"), is("El viaje no está en curso"));
        verify(servicioReservaMock, times(1)).marcarAsistencia(reservaId, conductorId, inputDTO.getAsistencia());
        verify(servicioViajeMock, times(1)).obtenerViajePorId(viajeId);
    }

    @Test
    public void marcarAsistencia_deberiaMostrarErrorSiDatoInvalido() throws Exception {
        // given
        Long conductorId = 1L;
        Long reservaId = 10L;
        Long viajeId = 100L;
        MarcarAsistenciaInputDTO inputDTO = new MarcarAsistenciaInputDTO(reservaId, "INVALIDO");

        Reserva reservaMock = mock(Reserva.class);
        Viaje viajeMock = mock(Viaje.class);
        when(reservaMock.getViaje()).thenReturn(viajeMock);
        when(viajeMock.getId()).thenReturn(viajeId);

        when(sessionMock.getAttribute("idUsuario")).thenReturn(conductorId);
        when(servicioReservaMock.obtenerReservaPorId(reservaId)).thenReturn(reservaMock);

        doThrow(new DatoObligatorioException("Valor de asistencia inválido"))
                .when(servicioReservaMock).marcarAsistencia(reservaId, conductorId, inputDTO.getAsistencia());

        // Simular recarga de datos
        when(servicioViajeMock.obtenerViajePorId(viajeId)).thenReturn(viajeMock);
        when(servicioReservaMock.listarViajerosConfirmados(viajeId, conductorId)).thenReturn(Arrays.asList());

        // when
        ModelAndView mav = controladorReserva.marcarAsistencia(inputDTO, sessionMock);

        // then
        assertThat(mav.getViewName(), is("viajerosConfirmados"));
        assertThat(mav.getModel().get("error"), is("Valor de asistencia inválido"));
        verify(servicioReservaMock, times(1)).marcarAsistencia(reservaId, conductorId, inputDTO.getAsistencia());
    }

    @Test
    public void marcarAsistencia_deberiaRedirigirSiFallaRecargaDatos() throws Exception {
        // given
        Long conductorId = 1L;
        Long reservaId = 10L;
        Long viajeId = 100L;
        MarcarAsistenciaInputDTO inputDTO = new MarcarAsistenciaInputDTO(reservaId, "PRESENTE");

        Reserva reservaMock = mock(Reserva.class);
        Viaje viajeMock = mock(Viaje.class);
        when(reservaMock.getViaje()).thenReturn(viajeMock);
        when(viajeMock.getId()).thenReturn(viajeId);

        when(sessionMock.getAttribute("idUsuario")).thenReturn(conductorId);
        when(servicioReservaMock.obtenerReservaPorId(reservaId)).thenReturn(reservaMock);
        doNothing().when(servicioReservaMock).marcarAsistencia(reservaId, conductorId, inputDTO.getAsistencia());

        // Simular que el intento de recarga de datos falla (e.g., ViajeNoEncontradoException)
        when(servicioViajeMock.obtenerViajePorId(viajeId)).thenThrow(new ViajeNoEncontradoException("No existe"));

        // when
        ModelAndView mav = controladorReserva.marcarAsistencia(inputDTO, sessionMock);

        // then
        assertThat(mav.getViewName(), is("redirect:/reserva/misReservas"));
        verify(servicioReservaMock, times(1)).marcarAsistencia(reservaId, conductorId, inputDTO.getAsistencia());
        verify(servicioViajeMock, times(1)).obtenerViajePorId(viajeId);
    }
}
