package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Entity.Reserva;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.Enums.EstadoReserva;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import com.tallerwebi.dominio.IServicio.ServicioReserva;
import com.tallerwebi.dominio.IServicio.ServicioViaje;
import com.tallerwebi.dominio.IServicio.ServicioViajero;
import com.tallerwebi.dominio.excepcion.*;
import com.tallerwebi.presentacion.Controller.ControladorReserva;
import com.tallerwebi.presentacion.DTO.InputsDTO.RechazoReservaInputDTO;
import com.tallerwebi.presentacion.DTO.InputsDTO.SolicitudReservaInputDTO;
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

    @BeforeEach
    public void init() {
        servicioReservaMock = mock(ServicioReserva.class);
        servicioViajeMock = mock(ServicioViaje.class);
        servicioViajeroMock = mock(ServicioViajero.class);
        servicioConductor = mock(ServicioConductor.class);
        redirectAttributesMock = mock(RedirectAttributes.class);

        controladorReserva = new ControladorReserva(servicioReservaMock, servicioViajeMock, servicioViajeroMock, servicioConductor);
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
        when(servicioReservaMock.listarReservasPendientesYRechazadas(viajeroId)).thenReturn(reservas);

        // when
        ModelAndView mav = controladorReserva.listarReservasPendientesYRechazadas(sessionMock);

        // then
        assertThat(mav.getViewName(), is("misReservasPendientes"));
        assertThat(mav.getModel().get("reservasPendientes"), notNullValue());
        assertThat(mav.getModel().get("reservasRechazadas"), notNullValue());
        verify(servicioReservaMock, times(1)).listarReservasPendientesYRechazadas(viajeroId);
    }

    @Test
    public void deberiaRedirigirALoginSiNoHaySesionEnReservasPendientes() throws Exception {
        // given
        when(sessionMock.getAttribute("idUsuario")).thenReturn(null);

        // when
        ModelAndView mav = controladorReserva.listarReservasPendientesYRechazadas(sessionMock);

        // then
        assertThat(mav.getViewName(), is("redirect:/login"));
        verify(servicioReservaMock, never()).listarReservasPendientesYRechazadas(anyLong());
    }

    @Test
    public void deberiaRedirigirALoginSiRolNoEsViajeroEnReservasPendientes() throws Exception {
        // given
        when(sessionMock.getAttribute("idUsuario")).thenReturn(1L);
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");

        // when
        ModelAndView mav = controladorReserva.listarReservasPendientesYRechazadas(sessionMock);

        // then
        assertThat(mav.getViewName(), is("redirect:/login"));
        verify(servicioReservaMock, never()).listarReservasPendientesYRechazadas(anyLong());
    }

    @Test
    public void deberiaMostrarErrorSiViajeroNoExisteEnReservasPendientes() throws Exception {
        // given
        Long viajeroId = 999L;

        when(sessionMock.getAttribute("idUsuario")).thenReturn(viajeroId);
        when(sessionMock.getAttribute("ROL")).thenReturn("VIAJERO");
        when(servicioReservaMock.listarReservasPendientesYRechazadas(viajeroId))
                .thenThrow(new UsuarioInexistente("No se encontró el viajero"));

        // when
        ModelAndView mav = controladorReserva.listarReservasPendientesYRechazadas(sessionMock);

        // then
        assertThat(mav.getViewName(), is("error"));
        assertThat(mav.getModel().get("error"), is("No se encontró el viajero"));
        verify(servicioReservaMock, times(1)).listarReservasPendientesYRechazadas(viajeroId);
    }
}
