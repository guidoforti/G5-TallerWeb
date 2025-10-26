package com.tallerwebi.dominio;

import com.tallerwebi.dominio.Entity.Reserva;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.Enums.EstadoDeViaje;
import com.tallerwebi.dominio.Enums.EstadoReserva;
import com.tallerwebi.dominio.IRepository.ReservaRepository;
import com.tallerwebi.dominio.IServicio.ServicioReserva;
import com.tallerwebi.dominio.IServicio.ServicioViaje;
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

    @BeforeEach
    void setUp() {
        repositorioReservaMock = mock(ReservaRepository.class);
        servicioViaje = mock(ServicioViaje.class);
        servicioReserva = new ServicioReservaImpl(repositorioReservaMock, servicioViaje);
    }

    // --- TESTS DE SOLICITAR RESERVA ---

    @Test
    void deberiaSolicitarReservaCorrectamenteCuandoNoExisteYHayAsientosDisponibles() throws Exception {
        // given
        Viaje viaje = crearViajeMock(1L, 3, EstadoDeViaje.DISPONIBLE, LocalDateTime.now().plusDays(1));
        Viajero viajero = crearViajeroMock(1L);

        when(repositorioReservaMock.findByViajeAndViajero(viaje, viajero)).thenReturn(Optional.empty());
        when(repositorioReservaMock.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Reserva reserva = servicioReserva.solicitarReserva(viaje, viajero);

        // then
        assertThat(reserva.getViaje(), is(viaje));
        assertThat(reserva.getViajero(), is(viajero));
        assertThat(reserva.getEstado(), is(EstadoReserva.PENDIENTE));
        assertThat(reserva.getFechaSolicitud(), notNullValue());
        verify(repositorioReservaMock, times(1)).findByViajeAndViajero(viaje, viajero);
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

    // --- TESTS DE OBTENER VIAJEROS CONFIRMADOS ---


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
