package com.tallerwebi.dominio;

import com.mercadopago.client.payment.PaymentRefundClient;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import com.tallerwebi.dominio.Entity.*;
import com.tallerwebi.dominio.Enums.*;
import com.tallerwebi.dominio.IRepository.RepositorioHistorialReserva;
import com.tallerwebi.dominio.IRepository.ReservaRepository;
import com.tallerwebi.dominio.IServicio.ServicioNotificacion;
import com.tallerwebi.dominio.IServicio.ServicioReserva;
import com.tallerwebi.dominio.IServicio.ServicioViaje;
import com.tallerwebi.dominio.IServicio.ServicioViajero;
import com.tallerwebi.dominio.ServiceImpl.ServicioReservaImpl;
import com.tallerwebi.dominio.excepcion.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ServicioReservaTest {

    private ReservaRepository repositorioReservaMock;
    private ServicioReserva servicioReserva;
    private ServicioViaje servicioViaje;
    private ServicioViajero servicioViajero;
    private RepositorioHistorialReserva repositorioHistorialReserva;
    private PreferenceClient preferenceClient;
    private ServicioNotificacion servicioNotificacionMock;
    private PaymentRefundClient reunfClientMock;

    @BeforeEach
    void setUp() {
        repositorioReservaMock = mock(ReservaRepository.class);
        servicioViaje = mock(ServicioViaje.class);
        servicioViajero = mock(ServicioViajero.class);
        repositorioHistorialReserva = mock(RepositorioHistorialReserva.class);
        preferenceClient = mock(PreferenceClient.class);
        servicioNotificacionMock = mock(ServicioNotificacion.class);
        reunfClientMock = mock(PaymentRefundClient.class);
        servicioReserva = new ServicioReservaImpl(repositorioReservaMock, servicioViaje, servicioViajero, repositorioHistorialReserva,preferenceClient, servicioNotificacionMock, reunfClientMock);
    }

    // --- TESTS DE SOLICITAR RESERVA ---

    @Test
    void deberiaSolicitarReservaCorrectamenteCuandoNoExisteYHayAsientosDisponibles() throws Exception {
        // given
        Viaje viaje = crearViajeMock(1L, 3, EstadoDeViaje.DISPONIBLE, LocalDateTime.now().plusDays(1));
        Viajero viajero = crearViajeroMock(1L);

        when(repositorioReservaMock.findByViajeAndViajero(viaje, viajero)).thenReturn(Optional.empty());
        when(servicioViaje.obtenerViajePorId(viaje.getId())).thenReturn(viaje);
        when(servicioViajero.obtenerViajero(viajero.getId())).thenReturn(viajero);
        when(repositorioReservaMock.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Reserva reserva = servicioReserva.solicitarReserva(viaje, viajero);

        // then
        assertThat(reserva.getViaje(), is(viaje));
        assertThat(reserva.getViajero(), is(viajero));
        assertThat(reserva.getEstado(), is(EstadoReserva.PENDIENTE));
        assertThat(reserva.getFechaSolicitud(), notNullValue());
        verify(repositorioReservaMock, times(1)).findByViajeAndViajero(viaje, viajero);
        verify(servicioViaje, times(1)).obtenerViajePorId(viaje.getId());
        verify(servicioViajero, times(1)).obtenerViajero(viajero.getId());
        verify(repositorioReservaMock, times(1)).save(any(Reserva.class));
    }


    @Test
    void deberiaLanzarExcepcionCuandoYaExisteReservaParaElMismoViajeYViajero() {
        // given
        Viaje viaje = crearViajeMock(1L, 3, EstadoDeViaje.DISPONIBLE, LocalDateTime.now().plusDays(1));
        Viajero viajero = crearViajeroMock(1L);
        Reserva reservaExistente = new Reserva();

        when(repositorioReservaMock.findByViajeAndViajero(viaje, viajero)).thenReturn(Optional.of(reservaExistente));

        // when & then
        assertThrows(ReservaYaExisteException.class, () -> {
            servicioReserva.solicitarReserva(viaje, viajero);
        });

        verify(repositorioReservaMock, times(1)).findByViajeAndViajero(viaje, viajero);
        verify(repositorioReservaMock, never()).save(any(Reserva.class));
    }

    @Test
    void deberiaLanzarExcepcionCuandoNoHayAsientosDisponibles() {
        // given
        Viaje viaje = crearViajeMock(1L, 0, EstadoDeViaje.COMPLETO, LocalDateTime.now().plusDays(1));
        Viajero viajero = crearViajeroMock(1L);

        when(repositorioReservaMock.findByViajeAndViajero(viaje, viajero)).thenReturn(Optional.empty());

        // when & then
        assertThrows(SinAsientosDisponiblesException.class, () -> {
            servicioReserva.solicitarReserva(viaje, viajero);
        });

        verify(repositorioReservaMock, times(1)).findByViajeAndViajero(viaje, viajero);
        verify(repositorioReservaMock, never()).save(any(Reserva.class));
    }

    @Test
    void deberiaLanzarExcepcionCuandoElViajeYaInicio() {
        // given
        Viaje viaje = crearViajeMock(1L, 3, EstadoDeViaje.DISPONIBLE, LocalDateTime.now().minusHours(1));
        Viajero viajero = crearViajeroMock(1L);

        when(repositorioReservaMock.findByViajeAndViajero(viaje, viajero)).thenReturn(Optional.empty());

        // when & then
        assertThrows(ViajeYaIniciadoException.class, () -> {
            servicioReserva.solicitarReserva(viaje, viajero);
        });

        verify(repositorioReservaMock, times(1)).findByViajeAndViajero(viaje, viajero);
        verify(repositorioReservaMock, never()).save(any(Reserva.class));
    }

    @Test
    void deberiaLanzarExcepcionCuandoFaltanDatosObligatorios() {
        // given - viaje es null
        Viajero viajero = crearViajeroMock(1L);

        // when & then
        assertThrows(DatoObligatorioException.class, () -> {
            servicioReserva.solicitarReserva(null, viajero);
        });

        verify(repositorioReservaMock, never()).findByViajeAndViajero(any(), any());
        verify(repositorioReservaMock, never()).save(any(Reserva.class));
    }

    @Test
    void deberiaLanzarExcepcionCuandoViajeroEsNull() {
        // given - viajero es null
        Viaje viaje = crearViajeMock(1L, 3, EstadoDeViaje.DISPONIBLE, LocalDateTime.now().plusDays(1));

        // when & then
        assertThrows(DatoObligatorioException.class, () -> {
            servicioReserva.solicitarReserva(viaje, null);
        });

        verify(repositorioReservaMock, never()).findByViajeAndViajero(any(), any());
        verify(repositorioReservaMock, never()).save(any(Reserva.class));
    }

    // --- TESTS DE LISTAR RESERVAS POR VIAJE ---

    @Test
    void deberiaListarReservasPorViajeCorrectamente() {
        // given
        Viaje viaje = crearViajeMock(1L, 3, EstadoDeViaje.DISPONIBLE, LocalDateTime.now().plusDays(1));
        Reserva reserva1 = crearReservaMock(1L, EstadoReserva.CONFIRMADA);
        Reserva reserva2 = crearReservaMock(2L, EstadoReserva.PENDIENTE);
        List<Reserva> reservasEsperadas = Arrays.asList(reserva1, reserva2);

        when(repositorioReservaMock.findByViaje(viaje)).thenReturn(reservasEsperadas);

        // when
        List<Reserva> reservas = servicioReserva.listarReservasPorViaje(viaje);

        // then
        assertThat(reservas, hasSize(2));
        assertThat(reservas, is(reservasEsperadas));
        verify(repositorioReservaMock, times(1)).findByViaje(viaje);
    }

    // --- TESTS DE LISTAR RESERVAS POR VIAJERO ---

    @Test
    void deberiaListarReservasPorViajeroCorrectamente() {
        // given
        Viajero viajero = crearViajeroMock(1L);
        Reserva reserva1 = crearReservaMock(1L, EstadoReserva.CONFIRMADA);
        Reserva reserva2 = crearReservaMock(2L, EstadoReserva.PENDIENTE);
        List<Reserva> reservasEsperadas = Arrays.asList(reserva1, reserva2);

        when(repositorioReservaMock.findByViajero(viajero)).thenReturn(reservasEsperadas);

        // when
        List<Reserva> reservas = servicioReserva.listarReservasPorViajero(viajero);

        // then
        assertThat(reservas, hasSize(2));
        assertThat(reservas, is(reservasEsperadas));
        verify(repositorioReservaMock, times(1)).findByViajero(viajero);
    }

    // --- TESTS DE OBTENER RESERVA POR ID ---

    @Test
    void deberiaObtenerReservaPorIdCorrectamente() throws NotFoundException {
        // given
        Long reservaId = 1L;
        Reserva reservaEsperada = crearReservaMock(reservaId, EstadoReserva.PENDIENTE);

        when(repositorioReservaMock.findById(reservaId)).thenReturn(Optional.of(reservaEsperada));

        // when
        Reserva reserva = servicioReserva.obtenerReservaPorId(reservaId);

        // then
        assertThat(reserva, is(reservaEsperada));
        verify(repositorioReservaMock, times(1)).findById(reservaId);
    }

    @Test
    void deberiaLanzarExcepcionCuandoReservaNoExiste() {
        // given
        Long reservaId = 999L;

        when(repositorioReservaMock.findById(reservaId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> {
            servicioReserva.obtenerReservaPorId(reservaId);
        });

        verify(repositorioReservaMock, times(1)).findById(reservaId);
    }

    // --- TESTS DE LISTAR VIAJEROS CONFIRMADOS ---

    @Test
    void deberiaListarViajerosConfirmadosCorrectamente() throws Exception {
        // given
        Long viajeId = 1L;
        Long conductorId = 1L;

        Conductor conductor = new Conductor();
        conductor.setId(conductorId);

        Viaje viaje = crearViajeMock(viajeId, 2, EstadoDeViaje.DISPONIBLE, LocalDateTime.now().plusDays(1));
        viaje.setConductor(conductor);

        Viajero viajero1 = crearViajeroMock(1L);
        viajero1.setNombre("Juan Perez");
        viajero1.setEmail("juan@example.com");

        Viajero viajero2 = crearViajeroMock(2L);
        viajero2.setNombre("Maria Lopez");
        viajero2.setEmail("maria@example.com");

        Reserva reserva1 = crearReservaConViajero(1L, EstadoReserva.CONFIRMADA, viajero1);
        reserva1.setViaje(viaje);

        Reserva reserva2 = crearReservaConViajero(2L, EstadoReserva.CONFIRMADA, viajero2);
        reserva2.setViaje(viaje);

        List<Reserva> reservasConfirmadas = Arrays.asList(reserva1, reserva2);

        when(servicioViaje.obtenerViajePorId(viajeId)).thenReturn(viaje);
        when(repositorioReservaMock.findConfirmadasByViaje(viaje)).thenReturn(reservasConfirmadas);

        // when
        List<Reserva> resultado = servicioReserva.listarViajerosConfirmados(viajeId, conductorId);

        // then
        assertThat(resultado, hasSize(2));
        assertThat(resultado.get(0).getViajero().getNombre(), is("Juan Perez"));
        assertThat(resultado.get(1).getViajero().getNombre(), is("Maria Lopez"));
        verify(servicioViaje, times(1)).obtenerViajePorId(viajeId);
        verify(repositorioReservaMock, times(1)).findConfirmadasByViaje(viaje);
    }

    @Test
    void deberiaLanzarExcepcionSiConductorNoEsDuenioDelViajeAlListarViajeros() throws Exception {
        // given
        Long viajeId = 1L;
        Long conductorId = 1L;
        Long otroConductorId = 2L;

        Conductor otroConductor = new Conductor();
        otroConductor.setId(otroConductorId);

        Viaje viaje = crearViajeMock(viajeId, 2, EstadoDeViaje.DISPONIBLE, LocalDateTime.now().plusDays(1));
        viaje.setConductor(otroConductor);

        when(servicioViaje.obtenerViajePorId(viajeId)).thenReturn(viaje);

        // when & then
        assertThrows(UsuarioNoAutorizadoException.class, () -> {
            servicioReserva.listarViajerosConfirmados(viajeId, conductorId);
        });

        verify(repositorioReservaMock, never()).findConfirmadasByViaje(any());
    }

    @Test
    void deberiaLanzarExcepcionSiViajeNoExisteAlListarViajeros() throws Exception {
        // given
        Long viajeId = 999L;
        Long conductorId = 1L;

        when(servicioViaje.obtenerViajePorId(viajeId)).thenThrow(new ViajeNoEncontradoException("No se encontró el viaje"));

        // when & then
        assertThrows(ViajeNoEncontradoException.class, () -> {
            servicioReserva.listarViajerosConfirmados(viajeId, conductorId);
        });

        verify(repositorioReservaMock, never()).findConfirmadasByViaje(any());
    }

    // --- TESTS DE MARCAR ASISTENCIA ---

    @Test
    void deberiaMarcarAsistenciaComoPresente() throws Exception {
        // given
        Long reservaId = 1L;
        Long conductorId = 1L;
        String asistenciaValor = "PRESENTE";

        Conductor conductor = new Conductor();
        conductor.setId(conductorId);

        Viaje viaje = crearViajeMock(1L, 2, EstadoDeViaje.DISPONIBLE, LocalDateTime.now().plusMinutes(20)); // 20 min antes
        viaje.setConductor(conductor);

        Viajero viajero = crearViajeroMock(1L);
        Reserva reserva = crearReservaConViajero(reservaId, EstadoReserva.CONFIRMADA, viajero);
        reserva.setViaje(viaje);

        when(repositorioReservaMock.findById(reservaId)).thenReturn(Optional.of(reserva));

        // when
        servicioReserva.marcarAsistencia(reservaId, conductorId, asistenciaValor);

        // then
        assertThat(reserva.getAsistencia().name(), is("PRESENTE"));
        verify(repositorioReservaMock, times(1)).update(reserva);
    }

    @Test
    void deberiaLanzarExcepcionSiAsistenciaEsNoMarcado() {
        // given
        Long reservaId = 1L;
        Long conductorId = 1L;
        String asistenciaValor = "NO_MARCADO";

        Conductor conductor = new Conductor();
        conductor.setId(conductorId);

        Viaje viaje = crearViajeMock(1L, 2, EstadoDeViaje.DISPONIBLE, LocalDateTime.now().plusMinutes(20));
        viaje.setConductor(conductor);

        Reserva reserva = crearReservaConViajero(reservaId, EstadoReserva.CONFIRMADA, crearViajeroMock(1L));
        reserva.setViaje(viaje);

        when(repositorioReservaMock.findById(reservaId)).thenReturn(Optional.of(reserva));
        DatoObligatorioException exception = assertThrows(DatoObligatorioException.class, () -> {
            servicioReserva.marcarAsistencia(reservaId, conductorId, asistenciaValor);
        });

        assertThat(exception.getMessage(), is("El valor de asistencia debe ser PRESENTE o AUSENTE"));
        verify(repositorioReservaMock, never()).update(any());
    }

    @Test
    void deberiaMarcarAsistenciaComoAusente() throws Exception {
        // given
        Long reservaId = 1L;
        Long conductorId = 1L;
        String asistenciaValor = "AUSENTE";

        Conductor conductor = new Conductor();
        conductor.setId(conductorId);

        Viaje viaje = crearViajeMock(1L, 2, EstadoDeViaje.DISPONIBLE, LocalDateTime.now().plusMinutes(20));
        viaje.setConductor(conductor);

        Viajero viajero = crearViajeroMock(1L);
        Reserva reserva = crearReservaConViajero(reservaId, EstadoReserva.CONFIRMADA, viajero);
        reserva.setViaje(viaje);

        when(repositorioReservaMock.findById(reservaId)).thenReturn(Optional.of(reserva));

        // when
        servicioReserva.marcarAsistencia(reservaId, conductorId, asistenciaValor);

        // then
        assertThat(reserva.getAsistencia().name(), is("AUSENTE"));
        verify(repositorioReservaMock, times(1)).update(reserva);
    }

    @Test
    void deberiaLanzarExcepcionSiIntentaMarcarAsistenciaAntesDe30Minutos() {
        // given
        Long reservaId = 1L;
        Long conductorId = 1L;
        String asistenciaValor = "PRESENTE";

        Conductor conductor = new Conductor();
        conductor.setId(conductorId);

        Viaje viaje = crearViajeMock(1L, 2, EstadoDeViaje.DISPONIBLE, LocalDateTime.now().plusHours(2)); // 2 horas antes
        viaje.setConductor(conductor);

        Viajero viajero = crearViajeroMock(1L);
        Reserva reserva = crearReservaConViajero(reservaId, EstadoReserva.CONFIRMADA, viajero);
        reserva.setViaje(viaje);

        when(repositorioReservaMock.findById(reservaId)).thenReturn(Optional.of(reserva));

        // when & then
        assertThrows(AccionNoPermitidaException.class, () -> {
            servicioReserva.marcarAsistencia(reservaId, conductorId, asistenciaValor);
        });

        verify(repositorioReservaMock, never()).update(any());
    }

    @Test
    void deberiaLanzarExcepcionSiReservaNoEstaConfirmadaAlMarcarAsistencia() {
        // given
        Long reservaId = 1L;
        Long conductorId = 1L;
        String asistenciaValor = "PRESENTE";

        Conductor conductor = new Conductor();
        conductor.setId(conductorId);

        Viaje viaje = crearViajeMock(1L, 2, EstadoDeViaje.DISPONIBLE, LocalDateTime.now().plusMinutes(20));
        viaje.setConductor(conductor);

        Viajero viajero = crearViajeroMock(1L);
        Reserva reserva = crearReservaConViajero(reservaId, EstadoReserva.PENDIENTE, viajero); // No confirmada
        reserva.setViaje(viaje);

        when(repositorioReservaMock.findById(reservaId)).thenReturn(Optional.of(reserva));

        // when & then
        assertThrows(ReservaYaExisteException.class, () -> {
            servicioReserva.marcarAsistencia(reservaId, conductorId, asistenciaValor);
        });

        verify(repositorioReservaMock, never()).update(any());
    }

    @Test
    void deberiaLanzarExcepcionSiConductorNoEsDuenioDelViajeAlMarcarAsistencia() {
        // given
        Long reservaId = 1L;
        Long conductorId = 1L;
        Long otroConductorId = 2L;
        String asistenciaValor = "PRESENTE";

        Conductor otroConductor = new Conductor();
        otroConductor.setId(otroConductorId);

        Viaje viaje = crearViajeMock(1L, 2, EstadoDeViaje.DISPONIBLE, LocalDateTime.now().plusMinutes(20));
        viaje.setConductor(otroConductor);

        Viajero viajero = crearViajeroMock(1L);
        Reserva reserva = crearReservaConViajero(reservaId, EstadoReserva.CONFIRMADA, viajero);
        reserva.setViaje(viaje);

        when(repositorioReservaMock.findById(reservaId)).thenReturn(Optional.of(reserva));

        // when & then
        assertThrows(UsuarioNoAutorizadoException.class, () -> {
            servicioReserva.marcarAsistencia(reservaId, conductorId, asistenciaValor);
        });

        verify(repositorioReservaMock, never()).update(any());
    }

    @Test
    void deberiaLanzarExcepcionSiAsistenciaEsInvalida() {
        // given
        Long reservaId = 1L;
        Long conductorId = 1L;
        String asistenciaInvalida = "INVALIDO";

        Conductor conductor = new Conductor();
        conductor.setId(conductorId);

        Viaje viaje = crearViajeMock(1L, 2, EstadoDeViaje.DISPONIBLE, LocalDateTime.now().plusMinutes(20));
        viaje.setConductor(conductor);

        Viajero viajero = crearViajeroMock(1L);
        Reserva reserva = crearReservaConViajero(reservaId, EstadoReserva.CONFIRMADA, viajero);
        reserva.setViaje(viaje);

        when(repositorioReservaMock.findById(reservaId)).thenReturn(Optional.of(reserva));

        // when & then
        assertThrows(DatoObligatorioException.class, () -> {
            servicioReserva.marcarAsistencia(reservaId, conductorId, asistenciaInvalida);
        });

        verify(repositorioReservaMock, never()).update(any());
    }

    // --- TESTS DE CONFIRMAR RESERVA ---

    @Test
    void deberiaConfirmarReservaPendienteYDecrementarAsientos() throws Exception {
        // given
        Long reservaId = 1L;
        Long conductorId = 1L;

        Conductor conductor = new Conductor();
        conductor.setId(conductorId);
        Ciudad ciudadDestinoMock = mock(Ciudad.class);
        when(ciudadDestinoMock.getNombre()).thenReturn("Rosario");

        Viaje viaje = crearViajeMock(1L, 3, EstadoDeViaje.DISPONIBLE, LocalDateTime.now().plusDays(1));
        viaje.setConductor(conductor);
        viaje.setDestino(ciudadDestinoMock);

        Reserva reserva = crearReservaMock(reservaId, EstadoReserva.PENDIENTE);
        reserva.setViaje(viaje);

        Viajero viajeroMock = mock(Viajero.class);
        reserva.setViajero(viajeroMock);

        when(repositorioReservaMock.findById(reservaId)).thenReturn(Optional.of(reserva));
        when(servicioViaje.obtenerViajePorId(viaje.getId())).thenReturn(viaje);

        // when
        servicioReserva.confirmarReserva(reservaId, conductorId);

        // then
        assertThat(reserva.getEstado(), is(EstadoReserva.CONFIRMADA));
        assertThat(viaje.getAsientosDisponibles(), is(2)); // Decrementado de 3 a 2
        // No se llama explícitamente a modificarViaje porque Hibernate usa dirty checking automático
        // El viaje se actualizará automáticamente al finalizar la transacción
        verify(repositorioReservaMock, times(1)).update(reserva);
        verify(servicioNotificacionMock, times(1)).crearYEnviar(
                eq(viajeroMock),
                eq(TipoNotificacion.RESERVA_APROBADA),
                anyString(),
                anyString()
        );
    }

    @Test
    void deberiaLanzarExcepcionSiReservaNoExisteAlConfirmar() {
        // given
        Long reservaId = 999L;
        Long conductorId = 1L;

        when(repositorioReservaMock.findById(reservaId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> {
            servicioReserva.confirmarReserva(reservaId, conductorId);
        });

        verify(repositorioReservaMock, never()).update(any());
    }

    @Test
    void deberiaLanzarExcepcionSiConductorNoEsDuenioDelViajeAlConfirmar() {
        // given
        Long reservaId = 1L;
        Long conductorId = 1L;
        Long otroConductorId = 2L;

        Conductor conductor = new Conductor();
        conductor.setId(otroConductorId); // Diferente conductor

        Viaje viaje = crearViajeMock(1L, 3, EstadoDeViaje.DISPONIBLE, LocalDateTime.now().plusDays(1));
        viaje.setConductor(conductor);

        Reserva reserva = crearReservaMock(reservaId, EstadoReserva.PENDIENTE);
        reserva.setViaje(viaje);

        when(repositorioReservaMock.findById(reservaId)).thenReturn(Optional.of(reserva));

        // when & then
        assertThrows(UsuarioNoAutorizadoException.class, () -> {
            servicioReserva.confirmarReserva(reservaId, conductorId);
        });

        verify(repositorioReservaMock, never()).update(any());
    }

    @Test
    void deberiaLanzarExcepcionSiReservaNoEstaPendienteAlConfirmar() {
        // given
        Long reservaId = 1L;
        Long conductorId = 1L;

        Conductor conductor = new Conductor();
        conductor.setId(conductorId);

        Viaje viaje = crearViajeMock(1L, 3, EstadoDeViaje.DISPONIBLE, LocalDateTime.now().plusDays(1));
        viaje.setConductor(conductor);

        Reserva reserva = crearReservaMock(reservaId, EstadoReserva.CONFIRMADA); // Ya confirmada
        reserva.setViaje(viaje);

        when(repositorioReservaMock.findById(reservaId)).thenReturn(Optional.of(reserva));

        // when & then
        assertThrows(ReservaYaExisteException.class, () -> {
            servicioReserva.confirmarReserva(reservaId, conductorId);
        });

        verify(repositorioReservaMock, never()).update(any());
    }

    @Test
    void deberiaLanzarExcepcionSiNoHayAsientosDisponiblesAlConfirmar() {
        // given
        Long reservaId = 1L;
        Long conductorId = 1L;

        Conductor conductor = new Conductor();
        conductor.setId(conductorId);

        Viaje viaje = crearViajeMock(1L, 0, EstadoDeViaje.COMPLETO, LocalDateTime.now().plusDays(1)); // Sin asientos
        viaje.setConductor(conductor);

        Reserva reserva = crearReservaMock(reservaId, EstadoReserva.PENDIENTE);
        reserva.setViaje(viaje);

        when(repositorioReservaMock.findById(reservaId)).thenReturn(Optional.of(reserva));

        // when & then
        assertThrows(SinAsientosDisponiblesException.class, () -> {
            servicioReserva.confirmarReserva(reservaId, conductorId);
        });

        verify(repositorioReservaMock, never()).update(any());
    }

    // --- TESTS DE RECHAZAR RESERVA ---

    @Test
    void deberiaRechazarReservaPendienteConMotivo() throws Exception {
        // given
        Long reservaId = 1L;
        Long conductorId = 1L;
        String motivo = "No hay lugar para equipaje grande";

        Ciudad ciudadDestino = mock(Ciudad.class);
        when(ciudadDestino.getNombre()).thenReturn("Mar del Plata");

        Viajero viajero = mock(Viajero.class);
        when(viajero.getId()).thenReturn(2L);
        when(viajero.getNombre()).thenReturn("Juan Viajero");
        Conductor conductor = new Conductor();
        conductor.setId(conductorId);

        Viaje viaje = crearViajeMock(1L, 3, EstadoDeViaje.DISPONIBLE, LocalDateTime.now().plusDays(1));
        viaje.setConductor(conductor);
        viaje.setDestino(ciudadDestino);

        Reserva reserva = crearReservaMock(reservaId, EstadoReserva.PENDIENTE);
        reserva.setViaje(viaje);
        reserva.setViajero(viajero);
        when(repositorioReservaMock.findById(reservaId)).thenReturn(Optional.of(reserva));

        // when
        servicioReserva.rechazarReserva(reservaId, conductorId, motivo);

        // then
        assertThat(reserva.getEstado(), is(EstadoReserva.RECHAZADA));
        assertThat(reserva.getMotivoRechazo(), is(motivo));
        verify(repositorioReservaMock, times(1)).update(reserva);
        verify(servicioNotificacionMock, times(1)).crearYEnviar(
                eq(viajero),
                eq(TipoNotificacion.RESERVA_RECHAZADA),
                anyString(),
                anyString()
        );
    }

    @Test
    void deberiaLanzarExcepcionSiMotivoEstaVacioAlRechazar() {
        // given
        Long reservaId = 1L;
        Long conductorId = 1L;
        String motivoVacio = "";

        // when & then
        assertThrows(DatoObligatorioException.class, () -> {
            servicioReserva.rechazarReserva(reservaId, conductorId, motivoVacio);
        });

        verify(repositorioReservaMock, never()).findById(any());
        verify(repositorioReservaMock, never()).update(any());
    }

    @Test
    void deberiaLanzarExcepcionSiMotivoEsNullAlRechazar() {
        // given
        Long reservaId = 1L;
        Long conductorId = 1L;
        String motivoNull = null;

        // when & then
        assertThrows(DatoObligatorioException.class, () -> {
            servicioReserva.rechazarReserva(reservaId, conductorId, motivoNull);
        });

        verify(repositorioReservaMock, never()).findById(any());
        verify(repositorioReservaMock, never()).update(any());
    }

    @Test
    void deberiaLanzarExcepcionSiReservaNoExisteAlRechazar() {
        // given
        Long reservaId = 999L;
        Long conductorId = 1L;
        String motivo = "Motivo válido";

        when(repositorioReservaMock.findById(reservaId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> {
            servicioReserva.rechazarReserva(reservaId, conductorId, motivo);
        });

        verify(repositorioReservaMock, never()).update(any());
    }

    @Test
    void deberiaLanzarExcepcionSiConductorNoEsDuenioDelViajeAlRechazar() {
        // given
        Long reservaId = 1L;
        Long conductorId = 1L;
        Long otroConductorId = 2L;
        String motivo = "Motivo válido";

        Conductor conductor = new Conductor();
        conductor.setId(otroConductorId);

        Viaje viaje = crearViajeMock(1L, 3, EstadoDeViaje.DISPONIBLE, LocalDateTime.now().plusDays(1));
        viaje.setConductor(conductor);

        Reserva reserva = crearReservaMock(reservaId, EstadoReserva.PENDIENTE);
        reserva.setViaje(viaje);

        when(repositorioReservaMock.findById(reservaId)).thenReturn(Optional.of(reserva));

        // when & then
        assertThrows(UsuarioNoAutorizadoException.class, () -> {
            servicioReserva.rechazarReserva(reservaId, conductorId, motivo);
        });

        verify(repositorioReservaMock, never()).update(any());
    }

    @Test
    void deberiaLanzarExcepcionSiReservaNoEstaPendienteAlRechazar() {
        // given
        Long reservaId = 1L;
        Long conductorId = 1L;
        String motivo = "Motivo válido";

        Conductor conductor = new Conductor();
        conductor.setId(conductorId);

        Viaje viaje = crearViajeMock(1L, 3, EstadoDeViaje.DISPONIBLE, LocalDateTime.now().plusDays(1));
        viaje.setConductor(conductor);

        Reserva reserva = crearReservaMock(reservaId, EstadoReserva.RECHAZADA); // Ya rechazada
        reserva.setViaje(viaje);

        when(repositorioReservaMock.findById(reservaId)).thenReturn(Optional.of(reserva));

        // when & then
        assertThrows(ReservaYaExisteException.class, () -> {
            servicioReserva.rechazarReserva(reservaId, conductorId, motivo);
        });

        verify(repositorioReservaMock, never()).update(any());
    }

    // --- TESTS DE LISTAR RESERVAS PENDIENTES Y RECHAZADAS ---

    @Test
    void deberiaListarReservasActivasPorViajeroCorrectamente() throws Exception {
        // given
        Long viajeroId = 1L;
        Viajero viajero = crearViajeroMock(viajeroId);

        Viaje viaje1 = crearViajeMock(1L, 3, EstadoDeViaje.DISPONIBLE, LocalDateTime.now().plusDays(1));
        Viaje viaje2 = crearViajeMock(2L, 2, EstadoDeViaje.DISPONIBLE, LocalDateTime.now().plusDays(2));

        Reserva reservaPendiente = crearReservaMock(1L, EstadoReserva.PENDIENTE);
        reservaPendiente.setViaje(viaje1);
        reservaPendiente.setViajero(viajero);

        Reserva reservaRechazada = crearReservaMock(2L, EstadoReserva.RECHAZADA);
        reservaRechazada.setViaje(viaje2);
        reservaRechazada.setViajero(viajero);
        reservaRechazada.setMotivoRechazo("No hay espacio");

        List<Reserva> reservas = Arrays.asList(reservaPendiente, reservaRechazada);

        when(servicioViajero.obtenerViajero(viajeroId)).thenReturn(viajero);
        when(repositorioReservaMock.findByViajeroAndEstadoInOrderByViajesFechaSalidaAsc(eq(viajero), any()))
                .thenReturn(reservas);

        // when
        List<Reserva> resultado = servicioReserva.listarReservasActivasPorViajero(viajeroId);

        // then
        assertThat(resultado, hasSize(2));
        assertThat(resultado.get(0).getEstado(), is(EstadoReserva.PENDIENTE));
        assertThat(resultado.get(1).getEstado(), is(EstadoReserva.RECHAZADA));
        verify(servicioViajero, times(1)).obtenerViajero(viajeroId);
        verify(repositorioReservaMock, times(1))
                .findByViajeroAndEstadoInOrderByViajesFechaSalidaAsc(eq(viajero), any());
    }

    @Test
    void deberiaRetornarListaVaciaCuandoNoHayReservasPendientesNiRechazadas() throws Exception {
        // given
        Long viajeroId = 1L;
        Viajero viajero = crearViajeroMock(viajeroId);

        when(servicioViajero.obtenerViajero(viajeroId)).thenReturn(viajero);
        when(repositorioReservaMock.findByViajeroAndEstadoInOrderByViajesFechaSalidaAsc(eq(viajero), any()))
                .thenReturn(Arrays.asList());

        // when
        List<Reserva> resultado = servicioReserva.listarReservasActivasPorViajero(viajeroId);

        // then
        assertThat(resultado, hasSize(0));
        verify(servicioViajero, times(1)).obtenerViajero(viajeroId);
        verify(repositorioReservaMock, times(1))
                .findByViajeroAndEstadoInOrderByViajesFechaSalidaAsc(eq(viajero), any());
    }

    @Test
    void deberiaLanzarExcepcionCuandoViajeroNoExiste() throws UsuarioInexistente {
        // given
        Long viajeroId = 999L;

        when(servicioViajero.obtenerViajero(viajeroId))
                .thenThrow(new UsuarioInexistente("No se encontró el viajero con id: " + viajeroId));

        // when & then
        assertThrows(UsuarioInexistente.class, () -> {
            servicioReserva.listarReservasActivasPorViajero(viajeroId);
        });

        verify(servicioViajero, times(1)).obtenerViajero(viajeroId);
        verify(repositorioReservaMock, never())
                .findByViajeroAndEstadoInOrderByViajesFechaSalidaAsc(any(), any());
    }

    // --- TESTS DE LISTAR VIAJES CONFIRMADOS POR VIAJERO ---

    @Test
    void deberiaListarViajesConfirmadosPorViajeroCorrectamente() throws Exception {
        // given
        Long viajeroId = 1L;
        Viajero viajero = crearViajeroMock(viajeroId);

        Viaje viaje1 = crearViajeMock(1L, 3, EstadoDeViaje.DISPONIBLE, LocalDateTime.now().plusDays(5));
        Viaje viaje2 = crearViajeMock(2L, 2, EstadoDeViaje.EN_CURSO, LocalDateTime.now());
        Viaje viaje3 = crearViajeMock(3L, 1, EstadoDeViaje.FINALIZADO, LocalDateTime.now().minusDays(2));

        Reserva reserva1 = crearReservaMock(1L, EstadoReserva.CONFIRMADA);
        reserva1.setViaje(viaje1);
        reserva1.setViajero(viajero);

        Reserva reserva2 = crearReservaMock(2L, EstadoReserva.CONFIRMADA);
        reserva2.setViaje(viaje2);
        reserva2.setViajero(viajero);

        Reserva reserva3 = crearReservaMock(3L, EstadoReserva.CONFIRMADA);
        reserva3.setViaje(viaje3);
        reserva3.setViajero(viajero);

        List<Reserva> reservas = Arrays.asList(reserva1, reserva2, reserva3);

        when(servicioViajero.obtenerViajero(viajeroId)).thenReturn(viajero);
        when(repositorioReservaMock.findViajesConfirmadosPorViajero(viajero)).thenReturn(reservas);

        // when
        List<Reserva> resultado = servicioReserva.listarViajesConfirmadosPorViajero(viajeroId);

        // then
        assertThat(resultado, hasSize(3));
        assertThat(resultado.get(0).getEstado(), is(EstadoReserva.CONFIRMADA));
        assertThat(resultado.get(1).getEstado(), is(EstadoReserva.CONFIRMADA));
        assertThat(resultado.get(2).getEstado(), is(EstadoReserva.CONFIRMADA));
        verify(servicioViajero, times(1)).obtenerViajero(viajeroId);
        verify(repositorioReservaMock, times(1)).findViajesConfirmadosPorViajero(viajero);
    }

    @Test
    void deberiaRetornarListaVaciaCuandoNoHayViajesConfirmados() throws Exception {
        // given
        Long viajeroId = 1L;
        Viajero viajero = crearViajeroMock(viajeroId);

        when(servicioViajero.obtenerViajero(viajeroId)).thenReturn(viajero);
        when(repositorioReservaMock.findViajesConfirmadosPorViajero(viajero)).thenReturn(Arrays.asList());

        // when
        List<Reserva> resultado = servicioReserva.listarViajesConfirmadosPorViajero(viajeroId);

        // then
        assertThat(resultado, hasSize(0));
        verify(servicioViajero, times(1)).obtenerViajero(viajeroId);
        verify(repositorioReservaMock, times(1)).findViajesConfirmadosPorViajero(viajero);
    }

    @Test
    void deberiaLanzarExcepcionCuandoViajeroNoExisteAlListarViajesConfirmados() throws UsuarioInexistente {
        // given
        Long viajeroId = 999L;

        when(servicioViajero.obtenerViajero(viajeroId))
                .thenThrow(new UsuarioInexistente("No se encontró el viajero con id: " + viajeroId));

        // when & then
        assertThrows(UsuarioInexistente.class, () -> {
            servicioReserva.listarViajesConfirmadosPorViajero(viajeroId);
        });

        verify(servicioViajero, times(1)).obtenerViajero(viajeroId);
        verify(repositorioReservaMock, never()).findViajesConfirmadosPorViajero(any());
    }



    @Test
    public void deberiaCrearPreferenciaDePagoCorrectamente() throws Exception {
        // given
        Long reservaId = 1L;
        Long viajeroId = 10L;
        Reserva reserva = crearReservaCompletaParaPago(reservaId, viajeroId, EstadoReserva.CONFIRMADA, EstadoPago.NO_PAGADO);
        Preference preferenciaMock = new Preference(); // Mock de la respuesta de MP


        // when
        when(servicioViajero.obtenerViajero(viajeroId)).thenReturn(reserva.getViajero());
        when(repositorioReservaMock.findById(reservaId)).thenReturn(Optional.of(reserva));
        when(preferenceClient.create(any(PreferenceRequest.class))).thenReturn(preferenciaMock);

        // then
        Preference resultado = servicioReserva.crearPreferenciaDePago(reservaId, viajeroId);

        // assert
        assertThat(resultado, notNullValue());

        verify(preferenceClient, times(1)).create(any(PreferenceRequest.class));
    }

    @Test
    public void deberiaEnviarNotificacionAlConductorAlConfirmarPago() throws Exception {
        // given
        Long reservaId = 1L;
        Long viajeroId = 10L;
        Long conductorId = 50L;
        Long paymentId = 1234L;

        // Configuración detallada para que la notificación no falle:
        Viajero viajero = mock(Viajero.class);
        when(viajero.getId()).thenReturn(viajeroId);
        when(viajero.getNombre()).thenReturn("Viajero Test");

        Conductor conductor = mock(Conductor.class);
        when(conductor.getId()).thenReturn(conductorId);

        Ciudad destino = mock(Ciudad.class);
        when(destino.getNombre()).thenReturn("Córdoba");

        Viaje viaje = mock(Viaje.class);
        when(viaje.getId()).thenReturn(5L);
        when(viaje.getConductor()).thenReturn(conductor);
        when(viaje.getDestino()).thenReturn(destino);

        Reserva reserva = crearReservaCompletaParaPago(reservaId, viajeroId, EstadoReserva.CONFIRMADA, EstadoPago.NO_PAGADO);
        // Sobrescribir mocks parciales:
        reserva.setViajero(viajero);
        reserva.setViaje(viaje);
        when(repositorioReservaMock.findById(reservaId)).thenReturn(Optional.of(reserva));
        servicioReserva.confirmarPagoReserva(reservaId, viajeroId, paymentId);
        verify(repositorioReservaMock, times(1)).update(reserva);
        verify(servicioNotificacionMock, times(1)).crearYEnviar(
                eq(conductor), // Debe ir al conductor
                eq(TipoNotificacion.PAGO_RECIBIDO),
                anyString(),
                anyString()
        );
    }

    @Test
    public void deberiaLanzarNotFoundExceptionSiReservaNoExisteAlCrearPreferencia() throws UsuarioInexistente {
        // given
        Long reservaId = 99L;
        Long viajeroId = 10L;
        when(servicioViajero.obtenerViajero(viajeroId)).thenReturn(new Viajero());
        when(repositorioReservaMock.findById(reservaId)).thenReturn(Optional.empty());

        // then
        assertThrows(NotFoundException.class, () -> {
            servicioReserva.crearPreferenciaDePago(reservaId, viajeroId);
        });
    }

    @Test
    public void deberiaLanzarUsuarioNoAutorizadoExceptionSiViajeroNoEsDuenoDeReserva() throws UsuarioInexistente {
        // given
        Long reservaId = 1L;
        Long viajeroIdLogueado = 10L;
        Long viajeroIdDueno = 20L; // <-- IDs diferentes
        Reserva reserva = crearReservaCompletaParaPago(reservaId, viajeroIdDueno, EstadoReserva.CONFIRMADA, EstadoPago.NO_PAGADO);

        // when
        when(servicioViajero.obtenerViajero(viajeroIdLogueado)).thenReturn(new Viajero()); // No importa el viajero, solo el ID
        when(repositorioReservaMock.findById(reservaId)).thenReturn(Optional.of(reserva));

        // then
        assertThrows(UsuarioNoAutorizadoException.class, () -> {
            servicioReserva.crearPreferenciaDePago(reservaId, viajeroIdLogueado);
        });
    }

    @Test
    public void deberiaLanzarAccionNoPermitidaExceptionSiReservaNoEstaConfirmada() throws UsuarioInexistente {
        // given
        Long reservaId = 1L;
        Long viajeroId = 10L;
        // Estado PENDIENTE (incorrecto)
        Reserva reserva = crearReservaCompletaParaPago(reservaId, viajeroId, EstadoReserva.PENDIENTE, EstadoPago.NO_PAGADO);

        // when
        when(servicioViajero.obtenerViajero(viajeroId)).thenReturn(reserva.getViajero());
        when(repositorioReservaMock.findById(reservaId)).thenReturn(Optional.of(reserva));

        // then
        assertThrows(AccionNoPermitidaException.class, () -> {
            servicioReserva.crearPreferenciaDePago(reservaId, viajeroId);
        });
    }

    @Test
    public void deberiaLanzarAccionNoPermitidaExceptionSiReservaYaEstaPagada() throws UsuarioInexistente {
        // given
        Long reservaId = 1L;
        Long viajeroId = 10L;
        // Estado PAGADO (incorrecto)
        Reserva reserva = crearReservaCompletaParaPago(reservaId, viajeroId, EstadoReserva.CONFIRMADA, EstadoPago.PAGADO);

        // when
        when(servicioViajero.obtenerViajero(viajeroId)).thenReturn(reserva.getViajero());
        when(repositorioReservaMock.findById(reservaId)).thenReturn(Optional.of(reserva));

        // then
        assertThrows(AccionNoPermitidaException.class, () -> {
            servicioReserva.crearPreferenciaDePago(reservaId, viajeroId);
        });
    }

    // ===============================================================
    // TESTS PARA: confirmarPagoReserva
    // ===============================================================

    @Test
    public void deberiaConfirmarPagoReservaCorrectamente() throws Exception {
        // given
        Long reservaId = 1L;
        Long viajeroId = 10L;
        Long paymentId = 1234L;
        Reserva reserva = crearReservaCompletaParaPago(reservaId, viajeroId, EstadoReserva.CONFIRMADA, EstadoPago.NO_PAGADO);

        // when
        when(repositorioReservaMock.findById(reservaId)).thenReturn(Optional.of(reserva));

        // then
        Reserva resultado = servicioReserva.confirmarPagoReserva(reservaId, viajeroId, paymentId);

        // assert
        assertThat(resultado.getEstadoPago(), is(EstadoPago.PAGADO));
        verify(repositorioReservaMock, times(1)).update(reserva);
    }

    @Test
    public void deberiaLanzarNotFoundExceptionSiReservaNoExisteAlConfirmarPago() {
        // given
        Long reservaId = 99L;
        Long viajeroId = 10L;
        Long paymentId = 1234L;
        when(repositorioReservaMock.findById(reservaId)).thenReturn(Optional.empty());

        // then
        assertThrows(NotFoundException.class, () -> {
            servicioReserva.confirmarPagoReserva(reservaId, viajeroId, paymentId);
        });
        verify(repositorioReservaMock, never()).update(any());
    }

    @Test
    public void deberiaLanzarUsuarioNoAutorizadoExceptionSiViajeroNoEsDuenoAlConfirmarPago() {
        // given
        Long reservaId = 1L;
        Long viajeroIdLogueado = 10L;
        Long paymentId = 1234L;
        Long viajeroIdDueno = 20L; // <-- IDs diferentes
        Reserva reserva = crearReservaCompletaParaPago(reservaId, viajeroIdDueno, EstadoReserva.CONFIRMADA, EstadoPago.NO_PAGADO);

        // when
        when(repositorioReservaMock.findById(reservaId)).thenReturn(Optional.of(reserva));

        // then
        assertThrows(UsuarioNoAutorizadoException.class, () -> {
            servicioReserva.confirmarPagoReserva(reservaId, viajeroIdLogueado, paymentId);
        });
        verify(repositorioReservaMock, never()).update(any());
    }

    @Test
    public void deberiaLanzarAccionNoPermitidaExceptionSiReservaNoEstaConfirmadaAlConfirmarPago() {
        // given
        Long reservaId = 1L;
        Long viajeroId = 10L;
        Long paymentId = 1234L;
        // Estado PENDIENTE (incorrecto)
        Reserva reserva = crearReservaCompletaParaPago(reservaId, viajeroId, EstadoReserva.PENDIENTE, EstadoPago.NO_PAGADO);

        // when
        when(repositorioReservaMock.findById(reservaId)).thenReturn(Optional.of(reserva));

        // then
        assertThrows(AccionNoPermitidaException.class, () -> {
            servicioReserva.confirmarPagoReserva(reservaId, viajeroId, paymentId);
        });
        verify(repositorioReservaMock, never()).update(any());
    }



    // ===============================================================
    // MÉTODO HELPER (Necesario para los tests de 'crearPreferencia')
    // ===============================================================

    /**
     * Helper para crear una Reserva con todos los mocks anidados necesarios
     * para probar 'crearPreferenciaDePago'.
     */
    private Reserva crearReservaCompletaParaPago(Long reservaId, Long viajeroId, EstadoReserva estado, EstadoPago estadoPago) {
        // 1. Mocks de Ciudades
        Ciudad origen = mock(Ciudad.class);
        when(origen.getNombre()).thenReturn("Buenos Aires");

        Ciudad destino = mock(Ciudad.class);
        when(destino.getNombre()).thenReturn("Córdoba");

        // 2. Mock de Viaje
        Viaje viaje = mock(Viaje.class);
        when(viaje.getOrigen()).thenReturn(origen);
        when(viaje.getDestino()).thenReturn(destino);
        when(viaje.getFechaHoraDeSalida()).thenReturn(LocalDateTime.of(2025, 12, 24, 10, 0));
        when(viaje.getPrecio()).thenReturn(5000.0);

        // 3. Mock de Viajero
        Viajero viajero = mock(Viajero.class);
        when(viajero.getId()).thenReturn(viajeroId);

        // 4. Crear Reserva
        Reserva reserva = new Reserva();
        reserva.setId(reservaId);
        reserva.setViajero(viajero);
        reserva.setViaje(viaje);
        reserva.setEstado(estado);
        reserva.setEstadoPago(estadoPago);

        return reserva;
    }

    // --- MÉTODOS AUXILIARES PARA CREAR MOCKS ---

    private Viaje crearViajeMock(Long id, Integer asientosDisponibles, EstadoDeViaje estado, LocalDateTime fechaSalida) {
        Viaje viaje = new Viaje();
        viaje.setId(id);
        viaje.setAsientosDisponibles(asientosDisponibles);
        viaje.setEstado(estado);
        viaje.setFechaHoraDeSalida(fechaSalida);
        return viaje;
    }

    private Viajero crearViajeroMock(Long id) {
        Viajero viajero = new Viajero();
        viajero.setId(id);
        return viajero;
    }

    private Reserva crearReservaMock(Long id, EstadoReserva estado) {
        Reserva reserva = new Reserva();
        reserva.setId(id);
        reserva.setEstado(estado);
        reserva.setFechaSolicitud(LocalDateTime.now());
        return reserva;
    }

    private Reserva crearReservaConViajero(Long id, EstadoReserva estado, Viajero viajero) {
        Reserva reserva = crearReservaMock(id, estado);
        reserva.setViajero(viajero);
        return reserva;
    }

    @Test
    void deberiaLlamarAlServicioDeNotificacionAlSolicitarReserva() throws Exception {
        // given
        Long ID_CONDUCTOR = 42L;
        Conductor conductor = new Conductor();
        conductor.setId(ID_CONDUCTOR);

        Ciudad ciudadDestino = new Ciudad();
        ciudadDestino.setNombre("Cordoba");

        Viaje viaje = crearViajeMock(1L, 3, EstadoDeViaje.DISPONIBLE, LocalDateTime.now().plusDays(1));
        viaje.setConductor(conductor);
        viaje.setDestino(ciudadDestino);

        // 2. Crear Viajero (el solicitante)
        Viajero viajero = crearViajeroMock(1L);
        viajero.setNombre("Viajero Juan");

        // 3. Configurar Mocks para el flujo exitoso
        when(repositorioReservaMock.findByViajeAndViajero(viaje, viajero)).thenReturn(Optional.empty());
        when(servicioViaje.obtenerViajePorId(viaje.getId())).thenReturn(viaje);
        when(servicioViajero.obtenerViajero(viajero.getId())).thenReturn(viajero);

        // El save debe devolver la misma Reserva (ahora "Managed")
        when(repositorioReservaMock.save(any(Reserva.class))).thenAnswer(invocation -> {
            Reserva reservaGuardada = invocation.getArgument(0);
            reservaGuardada.getViaje().getConductor().getId(); // Acceder para simular
            return reservaGuardada;
        });

        // when
        servicioReserva.solicitarReserva(viaje, viajero);

        // then
        // 1. Verificar la lógica principal (guardar la reserva)
        verify(repositorioReservaMock, times(1)).save(any(Reserva.class));

        // 2. Verificar la lógica de Notificación (WebSocket)
        String destinoEsperado = "/topic/notificaciones/" + ID_CONDUCTOR;

        // Verificamos que se llamó a convertAndSend() una vez
        verify(servicioNotificacionMock, times(1)).crearYEnviar(
                any(Usuario.class),
                eq(TipoNotificacion.RESERVA_SOLICITADA),
                anyString(),
                anyString()
        );
    }


    @Test
    void deberiaListarReservasCanceladasEInicializarViajesCorrectamente() throws Exception {
        // given
        Long viajeroId = 1L;
        Viajero viajeroMock = crearViajeroMock(viajeroId);
        
        // 1. Crear un Viaje MOCK
        Viaje viajeMock = mock(Viaje.class);
        
        when(viajeMock.getOrigen()).thenReturn(mock(Ciudad.class));
        when(viajeMock.getDestino()).thenReturn(mock(Ciudad.class));
        when(viajeMock.getConductor()).thenReturn(mock(Conductor.class));
        when(viajeMock.getVehiculo()).thenReturn(mock(Vehiculo.class));

        Reserva reserva = crearReservaMock(100L, EstadoReserva.CANCELADA_POR_CONDUCTOR);
        reserva.setViaje(viajeMock); 
        
        // 4. Mockear el flujo de los servicios
        when(servicioViajero.obtenerViajero(viajeroId)).thenReturn(viajeroMock);
        when(repositorioReservaMock.findCanceladasByViajero(viajeroMock)).thenReturn(Arrays.asList(reserva));

        // when
        List<Reserva> reservasObtenidas = servicioReserva.listarViajesCanceladosPorViajero(viajeroId);

        // then
        // Verificación principal del resultado
        assertThat(reservasObtenidas, hasSize(1));
        assertThat(reservasObtenidas.get(0).getViaje(), is(viajeMock)); 

        verify(viajeMock, times(1)).getOrigen();
        verify(viajeMock, times(1)).getDestino();
        verify(viajeMock, times(1)).getConductor();
        verify(viajeMock, times(1)).getVehiculo();
        
        verify(servicioViajero, times(1)).obtenerViajero(viajeroId);
        verify(repositorioReservaMock, times(1)).findCanceladasByViajero(viajeroMock);
    }

    @Test
    void deberiaListarReservasCanceladasSinInicializarSiViajeEsNull() throws Exception {
        // given
        Long viajeroId = 2L;
        Viajero viajeroMock = crearViajeroMock(viajeroId);
     
        Reserva reservaSinViaje = crearReservaMock(200L, EstadoReserva.CANCELADA_POR_CONDUCTOR);
        reservaSinViaje.setViaje(null); 
        
        Viaje viajeNoLlamado = mock(Viaje.class); 

        // Mocking del servicio y repositorio
        when(servicioViajero.obtenerViajero(viajeroId)).thenReturn(viajeroMock);
        when(repositorioReservaMock.findCanceladasByViajero(viajeroMock)).thenReturn(Arrays.asList(reservaSinViaje));

        // when
        List<Reserva> reservasObtenidas = servicioReserva.listarViajesCanceladosPorViajero(viajeroId);

        // then
        assertThat(reservasObtenidas, hasSize(1));
        
        // Verificaciones para cubrir el 'else' (if = FALSE)
        verify(viajeNoLlamado, never()).getOrigen();
        verify(viajeNoLlamado, never()).getDestino();
        verify(viajeNoLlamado, never()).getConductor();
        verify(viajeNoLlamado, never()).getVehiculo();

        // Verificaciones del flujo principal
        verify(servicioViajero, times(1)).obtenerViajero(viajeroId);
        verify(repositorioReservaMock, times(1)).findCanceladasByViajero(viajeroMock);
    }
    
    @Test
    void deberiaRetornarListaVaciaSiNoHayReservasCanceladas() throws Exception {
        // given
        Long viajeroId = 3L;
        Viajero viajeroMock = crearViajeroMock(viajeroId);
        List<Reserva> listaVacia = Collections.emptyList();

        when(servicioViajero.obtenerViajero(viajeroId)).thenReturn(viajeroMock);
        when(repositorioReservaMock.findCanceladasByViajero(viajeroMock)).thenReturn(listaVacia);

        // when
        List<Reserva> reservasObtenidas = servicioReserva.listarViajesCanceladosPorViajero(viajeroId);

        // then
        assertThat(reservasObtenidas, is(listaVacia));
        assertThat(reservasObtenidas, hasSize(0));
        
        verify(servicioViajero, times(1)).obtenerViajero(viajeroId);
        verify(repositorioReservaMock, times(1)).findCanceladasByViajero(viajeroMock);
        
    }
    
    @Test
    void deberiaLanzarExcepcionCuandoElViajeroNoExiste() throws Exception {
        // given
        Long viajeroIdInexistente = 99L;
        
        doThrow(new UsuarioInexistente("El viajero no fue encontrado")).when(servicioViajero).obtenerViajero(viajeroIdInexistente);

        // when & then
        assertThrows(UsuarioInexistente.class, () -> {
            servicioReserva.listarViajesCanceladosPorViajero(viajeroIdInexistente);
        });

        verify(servicioViajero, times(1)).obtenerViajero(viajeroIdInexistente);
        verify(repositorioReservaMock, never()).findCanceladasByViajero(any());
    }


        // 🔹 Caso 1: Cancelación exitosa con reserva PENDIENTE y sin pago
    @Test
    void deberiaCancelarReservaPendienteCorrectamente() throws Exception {
        // given
        Viajero viajero = crearViajeroMock(1L, "Lucas");
        Conductor conductor = crearConductorMock(2L, "Carlos");
        Ciudad destino = new Ciudad();
        destino.setNombre("Buenos Aires");

        Viaje viaje = new Viaje();
        viaje.setConductor(conductor);
        viaje.setDestino(destino);

        Reserva reserva = new Reserva();
        reserva.setId(10L);
        reserva.setViajero(viajero);
        reserva.setViaje(viaje);
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setEstadoPago(EstadoPago.NO_PAGADO);

        when(repositorioReservaMock.findById(10L)).thenReturn(Optional.of(reserva));

        // when
        Reserva resultado = servicioReserva.cancelarReservaPorViajero(10L, viajero);

        // then
        assertThat(resultado.getEstado(), is(EstadoReserva.CANCELADA_POR_VIAJERO));
        assertThat(resultado.getEstadoPago(), is(EstadoPago.NO_PAGADO));
        verify(repositorioReservaMock).update(reserva);
        verify(repositorioHistorialReserva).save(any(HistorialReserva.class));
        verify(servicioNotificacionMock).crearYEnviar(
                eq(conductor),
                eq(TipoNotificacion.VIAJE_CANCELADO),
                contains("Lucas ha cancelado"),
                eq("/reserva/misReservas")
        );
    }

    // 🔹 Caso 3: Usuario no es VIAJERO
    @Test
    void deberiaLanzarExcepcionSiUsuarioNoEsViajero() {
        // given
        Conductor usuario = new Conductor();
        usuario.setId(1L);
        usuario.setRol("CONDUCTOR");

        // when & then
        assertThrows(UsuarioNoAutorizadoException.class, () -> {
            servicioReserva.cancelarReservaPorViajero(1L, usuario);
        });

        verifyNoInteractions(repositorioReservaMock);
    }

    // 🔹 Caso 4: Reserva no encontrada
    @Test
    void deberiaLanzarExcepcionSiReservaNoExiste() {
        // given
        Viajero viajero = crearViajeroMock(1L, "Lucas");
        when(repositorioReservaMock.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(ReservaNoEncontradaException.class, () -> {
            servicioReserva.cancelarReservaPorViajero(999L, viajero);
        });
    }

    // 🔹 Caso 5: Reserva pertenece a otro viajero
    @Test
    void deberiaLanzarExcepcionSiReservaNoPerteneceAlViajero() {
        // given
        Viajero viajeroSesion = crearViajeroMock(1L, "Lucas");
        Viajero otroViajero = crearViajeroMock(2L, "Juan");

        Reserva reserva = new Reserva();
        reserva.setId(33L);
        reserva.setViajero(otroViajero);
        reserva.setViaje(new Viaje());
        reserva.setEstado(EstadoReserva.PENDIENTE);

        when(repositorioReservaMock.findById(33L)).thenReturn(Optional.of(reserva));

        // when & then
        assertThrows(UsuarioNoAutorizadoException.class, () -> {
            servicioReserva.cancelarReservaPorViajero(33L, viajeroSesion);
        });
    }

    // 🔹 Caso 6: Reserva no cancelable (ej: ya cancelada)
    @Test
    void deberiaLanzarExcepcionSiReservaNoEsCancelable() {
        // given
        Viajero viajero = crearViajeroMock(1L, "Lucas");
        Reserva reserva = new Reserva();
        reserva.setId(44L);
        reserva.setViajero(viajero);
        reserva.setViaje(new Viaje());
        reserva.setEstado(EstadoReserva.CANCELADA_POR_CONDUCTOR);

        when(repositorioReservaMock.findById(44L)).thenReturn(Optional.of(reserva));

        // when & then
        assertThrows(IllegalStateException.class, () -> {
            servicioReserva.cancelarReservaPorViajero(44L, viajero);
        });
    }

    // 🔹 Caso 7: Error al enviar notificación (catch block)
    @Test
    void deberiaContinuarAunqueFalleElEnvioDeNotificacion() throws Exception {
        // given
        Viajero viajero = crearViajeroMock(1L, "Lucas");
        Conductor conductor = crearConductorMock(2L, "Carlos");
        Ciudad destino = new Ciudad();
        destino.setNombre("Rosario");

        Viaje viaje = new Viaje();
        viaje.setConductor(conductor);
        viaje.setDestino(destino);

        Reserva reserva = new Reserva();
        reserva.setId(55L);
        reserva.setViajero(viajero);
        reserva.setViaje(viaje);
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        reserva.setEstadoPago(EstadoPago.NO_PAGADO);

        when(repositorioReservaMock.findById(55L)).thenReturn(Optional.of(reserva));
        doThrow(new RuntimeException("Falla simulada")).when(servicioNotificacionMock)
                .crearYEnviar(any(), any(), anyString(), anyString());

        // when
        Reserva resultado = servicioReserva.cancelarReservaPorViajero(55L, viajero);

        // then
        assertThat(resultado.getEstado(), is(EstadoReserva.CANCELADA_POR_VIAJERO));
        verify(repositorioReservaMock).update(reserva);
        verify(repositorioHistorialReserva).save(any(HistorialReserva.class));
        verify(servicioNotificacionMock).crearYEnviar(any(), any(), anyString(), anyString());
    }

    private Viajero crearViajeroMock(Long id, String nombre) {
        Viajero v = new Viajero();
        v.setId(id);
        v.setRol("VIAJERO");
        v.setNombre(nombre);
        return v;
    }

    @Test
    public void generarReembolsoTotalDeberiaLlamarARefundYActualizarEstado() throws MPException, MPApiException {
        // given
        Long reservaId = 1L;
        Long paymentId = 12345L;
        Reserva reservaPagada = crearReservaPagada(reservaId, paymentId, LocalDateTime.now().plusDays(10));
        reservaPagada.setEstadoPago(EstadoPago.PAGADO);

        when(repositorioReservaMock.findById(reservaId)).thenReturn(Optional.of(reservaPagada));

        // when
        servicioReserva.generarReembolsoDeReservaTotal(reservaId, paymentId);

        // then
        // 1. Verifica que se llamó al reembolso TOTAL (sin monto)
        verify(reunfClientMock, times(1)).refund(paymentId);
        // 2. Verifica que NO se llamó al reembolso parcial
        verify(reunfClientMock, never()).refund(eq(paymentId), any(BigDecimal.class));
        // 3. Verifica que el estado se actualizó
        assertThat(reservaPagada.getEstadoPago(), is(EstadoPago.REEMBOLSADA));
        verify(repositorioReservaMock, times(1)).update(reservaPagada);
    }

    @Test
    public void generarReembolsoTotalDeberiaLanzarNotFoundSiReservaNoExiste() throws MPException, MPApiException {
        // given
        when(repositorioReservaMock.findById(99L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> {
            servicioReserva.generarReembolsoDeReservaTotal(99L, 12345L);
        });
        verify(reunfClientMock, never()).refund(anyLong());
    }

    @Test
    public void generarReembolsoTotalNoDeberiaHacerNadaSiReservaNoEstaPagada() throws MPException, MPApiException {
        // given
        Long reservaId = 1L;
        Long paymentId = 12345L;
        Reserva reservaNoPagada = crearReservaPagada(reservaId, paymentId, LocalDateTime.now().plusDays(10));
        reservaNoPagada.setEstadoPago(EstadoPago.NO_PAGADO); // No está pagada

        when(repositorioReservaMock.findById(reservaId)).thenReturn(Optional.of(reservaNoPagada));

        // when
        servicioReserva.generarReembolsoDeReservaTotal(reservaId, paymentId);

        // then
        // No debe llamar a MP ni actualizar la BD
        verify(reunfClientMock, never()).refund(anyLong());
        verify(repositorioReservaMock, never()).update(any(Reserva.class));
    }


    // ===============================================================
    // TESTS PARA: generarReembolsoDeReservaParcial
    // (Usado por el Viajero al cancelar su Reserva)
    // ===============================================================

    @Test
    public void generarReembolsoParcialDeberiaReembolsar100PorCientoSiFaltan7DiasOMas() throws MPException, MPApiException, NotFoundException {
        // given
        Long reservaId = 1L;
        Long paymentId = 12345L;
        // Viaje es en 8 días
        Reserva reservaPagada = crearReservaPagada(reservaId, paymentId, LocalDateTime.now().plusDays(8));

        when(repositorioReservaMock.findById(reservaId)).thenReturn(Optional.of(reservaPagada));

        // when
        servicioReserva.generarReembolsoDeReservaParcial(reservaId, paymentId);

        // then
        // 1. Verifica reembolso TOTAL (montoReembolso == null)
        verify(reunfClientMock, times(1)).refund(paymentId);
        verify(reunfClientMock, never()).refund(eq(paymentId), any(BigDecimal.class));
        // 2. Verifica estado final
        assertThat(reservaPagada.getEstadoPago(), is(EstadoPago.REEMBOLSADA));
        verify(repositorioReservaMock, times(1)).update(reservaPagada);
    }

    @Test
    public void generarReembolsoParcialDeberiaReembolsar50PorCientoSiFaltanEntre1Y6Dias() throws MPException, MPApiException, NotFoundException {
        // given
        Long reservaId = 2L;
        Long paymentId = 12346L;
        // Viaje es en 3 días
        Reserva reservaPagada = crearReservaPagada(reservaId, paymentId, LocalDateTime.now().plusDays(3));
        // El precio del viaje es 1000.0 (ver helper)
        BigDecimal reembolsoEsperado = new BigDecimal("500"); // 50% de 1000

        when(repositorioReservaMock.findById(reservaId)).thenReturn(Optional.of(reservaPagada));

        // when
        servicioReserva.generarReembolsoDeReservaParcial(reservaId, paymentId);

        // then
        // 1. Verifica reembolso PARCIAL (con monto)
        verify(reunfClientMock, times(1)).refund(paymentId, reembolsoEsperado);
        verify(reunfClientMock, never()).refund(paymentId);
        // 2. Verifica estado final
        assertThat(reservaPagada.getEstadoPago(), is(EstadoPago.REEMBOLSADA));
        verify(repositorioReservaMock, times(1)).update(reservaPagada);
    }

    @Test
    public void generarReembolsoParcialDeberiaReembolsar0PorCientoSiFaltaMenosDe1Dia() throws MPException, MPApiException, NotFoundException {
        // given
        Long reservaId = 3L;
        Long paymentId = 12347L;
        // Viaje es en 12 horas
        Reserva reservaPagada = crearReservaPagada(reservaId, paymentId, LocalDateTime.now().plusHours(12));

        when(repositorioReservaMock.findById(reservaId)).thenReturn(Optional.of(reservaPagada));

        // when
        servicioReserva.generarReembolsoDeReservaParcial(reservaId, paymentId);

        // then
        // 1. Verifica que NUNCA se llamó a la API de Mercado Pago
        verify(reunfClientMock, never()).refund(anyLong());
        verify(reunfClientMock, never()).refund(anyLong(), any(BigDecimal.class));
        // 2. Verifica estado final
        assertThat(reservaPagada.getEstadoPago(), is(EstadoPago.NO_CORRESPONDE_REMBOLSO));
        verify(repositorioReservaMock, times(1)).update(reservaPagada);
    }

    @Test
    public void generarReembolsoParcialNoDeberiaHacerNadaSiReservaNoEstaPagada() throws MPException, MPApiException, NotFoundException, MPException, MPApiException {
        // given
        Long reservaId = 4L;
        Long paymentId = 12348L;
        Reserva reservaNoPagada = crearReservaPagada(reservaId, paymentId, LocalDateTime.now().plusDays(10));
        reservaNoPagada.setEstadoPago(EstadoPago.NO_PAGADO); // No está pagada

        when(repositorioReservaMock.findById(reservaId)).thenReturn(Optional.of(reservaNoPagada));

        // when
        servicioReserva.generarReembolsoDeReservaParcial(reservaId, paymentId);

        // then
        // No debe llamar a MP ni actualizar la BD
        verify(reunfClientMock, never()).refund(anyLong());
        verify(repositorioReservaMock, never()).update(any(Reserva.class));
    }


    // ===============================================================
    // TESTS PARA: cancelarReservaPorViajero (MODIFICADO)
    // ===============================================================

    @Test
    public void cancelarReservaPorViajeroDeberiaLlamarAReembolsoSiEstabaPagada() throws Exception {
        // given
        Long reservaId = 5L;
        Long viajeroId = 10L;
        Long paymentId = 12349L;

        Viajero viajero = crearViajeroMock(viajeroId);
        viajero.setRol("VIAJERO");

        // Creamos una reserva pagada, viaje en 10 días (debería dar 100% reembolso)
        Reserva reservaPagada = crearReservaPagada(reservaId, paymentId, LocalDateTime.now().plusDays(10));
        reservaPagada.setViajero(viajero);

        when(repositorioReservaMock.findById(reservaId)).thenReturn(Optional.of(reservaPagada));
        // Mockeamos la notificación para que no falle
        doNothing().when(servicioNotificacionMock).crearYEnviar(any(), any(), any(), any());

        // when
        servicioReserva.cancelarReservaPorViajero(reservaId, viajero);

        // then
        // 1. Verifica que el estado de la reserva cambió
        assertThat(reservaPagada.getEstado(), is(EstadoReserva.CANCELADA_POR_VIAJERO));

        // 2. Verifica que se llamó al reembolso (con la lógica de 100%)
        verify(reunfClientMock, times(1)).refund(paymentId);

        // 3. Verifica que el estado de pago cambió
        assertThat(reservaPagada.getEstadoPago(), is(EstadoPago.REEMBOLSADA));

        // 4. Verifica que se guardó en la BD (una vez en 'generarReembolso' y otra en 'cancelar')
        // (Refactor): Tu código ahora solo guarda 1 vez al final de 'cancelar',
        // y 1 vez al final de 'generarReembolso'. Total = 2.
        verify(repositorioReservaMock, times(2)).update(reservaPagada);
    }

    @Test
    public void cancelarReservaPorViajeroNoDeberiaLlamarAReembolsoSiNoEstabaPagada() throws Exception {
        // given
        Long reservaId = 6L;
        Long viajeroId = 10L;

        Viajero viajero = crearViajeroMock(viajeroId);
        viajero.setRol("VIAJERO");

        // Creamos una reserva NO pagada
        Reserva reservaNoPagada = crearReservaPagada(reservaId, null, LocalDateTime.now().plusDays(10));
        reservaNoPagada.setEstadoPago(EstadoPago.NO_PAGADO);
        reservaNoPagada.setViajero(viajero);

        when(repositorioReservaMock.findById(reservaId)).thenReturn(Optional.of(reservaNoPagada));
        doNothing().when(servicioNotificacionMock).crearYEnviar(any(), any(), any(), any());

        // when
        servicioReserva.cancelarReservaPorViajero(reservaId, viajero);

        // then
        // 1. Verifica que NUNCA se llamó a la API de MP
        verify(reunfClientMock, never()).refund(anyLong());
        verify(reunfClientMock, never()).refund(anyLong(), any(BigDecimal.class));

        // 2. El estado de la reserva SÍ cambió
        assertThat(reservaNoPagada.getEstado(), is(EstadoReserva.CANCELADA_POR_VIAJERO));
        assertThat(reservaNoPagada.getEstadoPago(), is(EstadoPago.NO_PAGADO)); // Sigue igual

        // 3. Se actualizó la BD solo 1 vez (en 'cancelarReservaPorViajero')
        verify(repositorioReservaMock, times(1)).update(reservaNoPagada);
    }

    // ===============================================================
    // MÉTODO HELPER (Copiar y pegar en tu ServicioReservaTest.java)
    // ===============================================================

    /**
     * Helper para crear una Reserva PAGADA con mocks anidados necesarios
     * para probar 'generarReembolsoDeReservaParcial'.
     */
    private Reserva crearReservaPagada(Long reservaId, Long paymentId, LocalDateTime fechaHoraViaje) {
        // 1. Mocks de Ciudades
        Ciudad origen = mock(Ciudad.class);
        when(origen.getNombre()).thenReturn("Buenos Aires");

        Ciudad destino = mock(Ciudad.class);
        when(destino.getNombre()).thenReturn("Córdoba");

        // 2. Mock de Conductor (necesario para la notificación)
        Conductor conductor = mock(Conductor.class);
        when(conductor.getId()).thenReturn(50L);

        // 3. Mock de Viaje
        Viaje viaje = mock(Viaje.class);
        when(viaje.getOrigen()).thenReturn(origen);
        when(viaje.getDestino()).thenReturn(destino);
        when(viaje.getFechaHoraDeSalida()).thenReturn(fechaHoraViaje);
        when(viaje.getPrecio()).thenReturn(1000.0); // Precio base para calcular 50%
        when(viaje.getConductor()).thenReturn(conductor);

        // 4. Mock de Viajero
        Viajero viajero = mock(Viajero.class);
        when(viajero.getId()).thenReturn(10L); // Asumimos un ID de viajero

        // 5. Crear Reserva
        Reserva reserva = new Reserva();
        reserva.setId(reservaId);
        reserva.setViajero(viajero);
        reserva.setViaje(viaje);
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        reserva.setEstadoPago(EstadoPago.PAGADO); // <-- Pagada
        reserva.setMpIdDePago(paymentId); // <-- Con ID de MP (usando tu getter)

        return reserva;
    }
    private Conductor crearConductorMock(Long id, String nombre) {
        Conductor c = new Conductor();
        c.setId(id);
        c.setRol("CONDUCTOR");
        c.setNombre(nombre);
        return c;
    }

}
