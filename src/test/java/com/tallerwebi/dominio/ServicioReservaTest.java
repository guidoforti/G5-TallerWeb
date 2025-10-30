package com.tallerwebi.dominio;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Reserva;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.Enums.EstadoAsistencia;
import com.tallerwebi.dominio.Enums.EstadoDeViaje;
import com.tallerwebi.dominio.Enums.EstadoReserva;
import com.tallerwebi.dominio.IRepository.ReservaRepository;
import com.tallerwebi.dominio.IServicio.ServicioReserva;
import com.tallerwebi.dominio.IServicio.ServicioViaje;
import com.tallerwebi.dominio.IServicio.ServicioViajero;
import com.tallerwebi.dominio.ServiceImpl.ServicioReservaImpl;
import com.tallerwebi.dominio.excepcion.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ServicioReservaTest {

    private ReservaRepository repositorioReservaMock;
    private ServicioReserva servicioReserva;
    private ServicioViaje servicioViaje;
    private ServicioViajero servicioViajero;

    @BeforeEach
    void setUp() {
        repositorioReservaMock = mock(ReservaRepository.class);
        servicioViaje = mock(ServicioViaje.class);
        servicioViajero = mock(ServicioViajero.class);
        servicioReserva = new ServicioReservaImpl(repositorioReservaMock, servicioViaje, servicioViajero);
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

        Viaje viaje = crearViajeMock(1L, 3, EstadoDeViaje.DISPONIBLE, LocalDateTime.now().plusDays(1));
        viaje.setConductor(conductor);

        Reserva reserva = crearReservaMock(reservaId, EstadoReserva.PENDIENTE);
        reserva.setViaje(viaje);

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

        Conductor conductor = new Conductor();
        conductor.setId(conductorId);

        Viaje viaje = crearViajeMock(1L, 3, EstadoDeViaje.DISPONIBLE, LocalDateTime.now().plusDays(1));
        viaje.setConductor(conductor);

        Reserva reserva = crearReservaMock(reservaId, EstadoReserva.PENDIENTE);
        reserva.setViaje(viaje);

        when(repositorioReservaMock.findById(reservaId)).thenReturn(Optional.of(reserva));

        // when
        servicioReserva.rechazarReserva(reservaId, conductorId, motivo);

        // then
        assertThat(reserva.getEstado(), is(EstadoReserva.RECHAZADA));
        assertThat(reserva.getMotivoRechazo(), is(motivo));
        verify(repositorioReservaMock, times(1)).update(reserva);
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
    void deberiaListarReservasPendientesYRechazadasCorrectamente() throws Exception {
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
        List<Reserva> resultado = servicioReserva.listarReservasPendientesYRechazadas(viajeroId);

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
        List<Reserva> resultado = servicioReserva.listarReservasPendientesYRechazadas(viajeroId);

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
            servicioReserva.listarReservasPendientesYRechazadas(viajeroId);
        });

        verify(servicioViajero, times(1)).obtenerViajero(viajeroId);
        verify(repositorioReservaMock, never())
                .findByViajeroAndEstadoInOrderByViajesFechaSalidaAsc(any(), any());
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
}
