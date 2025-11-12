package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Entity.Reserva;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.Enums.EstadoReserva;
import com.tallerwebi.dominio.IRepository.ReservaRepository;
import com.tallerwebi.integracion.config.DataBaseTestInitilizationConfig;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HibernateTestConfig.class, DataBaseTestInitilizationConfig.class})
@Transactional
@WebAppConfiguration
public class RepositorioReservaTest {

    @Autowired
    private SessionFactory sessionFactory;

    private ReservaRepository repositorioReserva;

    @BeforeEach
    void setUp() {
        this.repositorioReserva = new RepositorioReservaImpl(this.sessionFactory);
    }

    @Test
    public void deberiaBuscarReservaPorViajeYViajero() {
        // given
        Viaje viaje = sessionFactory.getCurrentSession().get(Viaje.class, 1L);
        Viajero viajero = sessionFactory.getCurrentSession().get(Viajero.class, 4L);

        Reserva reserva = new Reserva();
        reserva.setViaje(viaje);
        reserva.setViajero(viajero);
        reserva.setFechaSolicitud(LocalDateTime.now());
        reserva.setEstado(EstadoReserva.PENDIENTE);

        repositorioReserva.save(reserva);
        sessionFactory.getCurrentSession().flush();

        // when
        Optional<Reserva> reservaEncontrada = repositorioReserva.findByViajeAndViajero(viaje, viajero);

        // then
        assertTrue(reservaEncontrada.isPresent(), "Debería encontrar la reserva");
        assertThat(reservaEncontrada.get().getViaje().getId(), is(viaje.getId()));
        assertThat(reservaEncontrada.get().getViajero().getId(), is(viajero.getId()));
        assertThat(reservaEncontrada.get().getEstado(), is(EstadoReserva.PENDIENTE));
    }

    @Test
    public void deberiaRetornarOptionalVacioCuandoNoExisteReservaPorViajeYViajero() {
        // given
        Viaje viaje = sessionFactory.getCurrentSession().get(Viaje.class, 1L);
        Viajero viajero = sessionFactory.getCurrentSession().get(Viajero.class, 4L);

        // when
        Optional<Reserva> reservaEncontrada = repositorioReserva.findByViajeAndViajero(viaje, viajero);

        // then
        assertTrue(reservaEncontrada.isEmpty(), "No debería encontrar ninguna reserva");
    }

    @Test
    public void deberiaBuscarTodasLasReservasPorViaje() {
        // given
        Viaje viaje = sessionFactory.getCurrentSession().get(Viaje.class, 1L);
        Viajero viajero1 = sessionFactory.getCurrentSession().get(Viajero.class, 4L);
        Viajero viajero2 = sessionFactory.getCurrentSession().get(Viajero.class, 5L);

        Reserva reserva1 = new Reserva();
        reserva1.setViaje(viaje);
        reserva1.setViajero(viajero1);
        reserva1.setFechaSolicitud(LocalDateTime.now().minusDays(2));
        reserva1.setEstado(EstadoReserva.CONFIRMADA);

        Reserva reserva2 = new Reserva();
        reserva2.setViaje(viaje);
        reserva2.setViajero(viajero2);
        reserva2.setFechaSolicitud(LocalDateTime.now().minusDays(1));
        reserva2.setEstado(EstadoReserva.PENDIENTE);

        repositorioReserva.save(reserva1);
        repositorioReserva.save(reserva2);
        sessionFactory.getCurrentSession().flush();

        // when
        List<Reserva> reservas = repositorioReserva.findByViaje(viaje);

        // then
        assertThat(reservas, hasSize(2));
        // Verificar orden por fecha solicitud ASC
        assertThat(reservas.get(0).getEstado(), is(EstadoReserva.CONFIRMADA));
        assertThat(reservas.get(1).getEstado(), is(EstadoReserva.PENDIENTE));
    }

    @Test
    public void deberiaBuscarTodasLasReservasPorViajero() {
        // given
        Viaje viaje1 = sessionFactory.getCurrentSession().get(Viaje.class, 1L);
        Viaje viaje2 = sessionFactory.getCurrentSession().get(Viaje.class, 2L);
        Viajero viajero = sessionFactory.getCurrentSession().get(Viajero.class, 4L);

        Reserva reserva1 = new Reserva();
        reserva1.setViaje(viaje1);
        reserva1.setViajero(viajero);
        reserva1.setFechaSolicitud(LocalDateTime.now().minusDays(1));
        reserva1.setEstado(EstadoReserva.CONFIRMADA);

        Reserva reserva2 = new Reserva();
        reserva2.setViaje(viaje2);
        reserva2.setViajero(viajero);
        reserva2.setFechaSolicitud(LocalDateTime.now());
        reserva2.setEstado(EstadoReserva.PENDIENTE);

        repositorioReserva.save(reserva1);
        repositorioReserva.save(reserva2);
        sessionFactory.getCurrentSession().flush();

        // when
        List<Reserva> reservas = repositorioReserva.findByViajero(viajero);

        // then
        assertThat(reservas, hasSize(3));
        // Verificar orden por fecha solicitud DESC (más reciente primero)
        assertThat(reservas.get(0).getEstado(), is(EstadoReserva.PENDIENTE));
        assertThat(reservas.get(1).getEstado(), is(EstadoReserva.CONFIRMADA));
    }

    @Test
    public void deberiaGuardarReservaCorrectamente() {
        // given
        Viaje viaje = sessionFactory.getCurrentSession().get(Viaje.class, 1L);
        Viajero viajero = sessionFactory.getCurrentSession().get(Viajero.class, 4L);

        Reserva reserva = new Reserva();
        reserva.setViaje(viaje);
        reserva.setViajero(viajero);
        reserva.setFechaSolicitud(LocalDateTime.now());
        reserva.setEstado(EstadoReserva.PENDIENTE);

        // when
        Reserva reservaGuardada = repositorioReserva.save(reserva);
        sessionFactory.getCurrentSession().flush();

        // then
        assertNotNull(reservaGuardada.getId(), "Debería tener ID asignado");
        assertThat(reservaGuardada.getEstado(), is(EstadoReserva.PENDIENTE));

        // Verificar que se puede recuperar
        Optional<Reserva> reservaRecuperada = repositorioReserva.findById(reservaGuardada.getId());
        assertTrue(reservaRecuperada.isPresent(), "La reserva guardada debe ser recuperable");
        assertThat(reservaRecuperada.get().getId(), is(reservaGuardada.getId()));
    }

    @Test
    public void deberiaBuscarReservaPorId() {
        // given
        Viaje viaje = sessionFactory.getCurrentSession().get(Viaje.class, 1L);
        Viajero viajero = sessionFactory.getCurrentSession().get(Viajero.class, 4L);

        Reserva reserva = new Reserva();
        reserva.setViaje(viaje);
        reserva.setViajero(viajero);
        reserva.setFechaSolicitud(LocalDateTime.now());
        reserva.setEstado(EstadoReserva.CONFIRMADA);

        Reserva reservaGuardada = repositorioReserva.save(reserva);
        sessionFactory.getCurrentSession().flush();

        // when
        Optional<Reserva> reservaEncontrada = repositorioReserva.findById(reservaGuardada.getId());

        // then
        assertTrue(reservaEncontrada.isPresent(), "Debería encontrar la reserva por ID");
        assertThat(reservaEncontrada.get().getId(), is(reservaGuardada.getId()));
        assertThat(reservaEncontrada.get().getEstado(), is(EstadoReserva.CONFIRMADA));
    }

    @Test
    public void deberiaRetornarOptionalVacioCuandoNoExisteReservaPorId() {
        // when
        Optional<Reserva> reservaEncontrada = repositorioReserva.findById(999L);

        // then
        assertTrue(reservaEncontrada.isEmpty(), "No debería encontrar reserva con ID inexistente");
    }

    @Test
    public void deberiaActualizarReservaExistente() {
        // given
        Viaje viaje = sessionFactory.getCurrentSession().get(Viaje.class, 1L);
        Viajero viajero = sessionFactory.getCurrentSession().get(Viajero.class, 4L);

        Reserva reserva = new Reserva();
        reserva.setViaje(viaje);
        reserva.setViajero(viajero);
        reserva.setFechaSolicitud(LocalDateTime.now());
        reserva.setEstado(EstadoReserva.PENDIENTE);

        Reserva reservaGuardada = repositorioReserva.save(reserva);
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        // when
        Reserva reservaAActualizar = repositorioReserva.findById(reservaGuardada.getId()).get();
        reservaAActualizar.setEstado(EstadoReserva.CONFIRMADA);
        repositorioReserva.update(reservaAActualizar);
        sessionFactory.getCurrentSession().flush();

        // then
        Optional<Reserva> reservaActualizada = repositorioReserva.findById(reservaGuardada.getId());
        assertTrue(reservaActualizada.isPresent());
        assertThat(reservaActualizada.get().getEstado(), is(EstadoReserva.CONFIRMADA));
    }

    @Test
    public void deberiaGuardarReservaConMotivoRechazo() {
        // given
        Viaje viaje = sessionFactory.getCurrentSession().get(Viaje.class, 1L);
        Viajero viajero = sessionFactory.getCurrentSession().get(Viajero.class, 4L);

        Reserva reserva = new Reserva();
        reserva.setViaje(viaje);
        reserva.setViajero(viajero);
        reserva.setFechaSolicitud(LocalDateTime.now());
        reserva.setEstado(EstadoReserva.RECHAZADA);
        reserva.setMotivoRechazo("No hay espacio suficiente para equipaje");

        // when
        Reserva reservaGuardada = repositorioReserva.save(reserva);
        sessionFactory.getCurrentSession().flush();

        // then
        Optional<Reserva> reservaRecuperada = repositorioReserva.findById(reservaGuardada.getId());
        assertTrue(reservaRecuperada.isPresent());
        assertThat(reservaRecuperada.get().getEstado(), is(EstadoReserva.RECHAZADA));
        assertThat(reservaRecuperada.get().getMotivoRechazo(), is("No hay espacio suficiente para equipaje"));
    }

    @Test
    public void deberiaBuscarReservasPorViajeroYEstadosOrdenadasPorFechaSalidaDelViaje() {
        // given
        Viaje viaje1 = sessionFactory.getCurrentSession().get(Viaje.class, 1L);
        Viaje viaje2 = sessionFactory.getCurrentSession().get(Viaje.class, 2L);
        Viajero viajero = sessionFactory.getCurrentSession().get(Viajero.class, 4L);

        // Crear reservas con diferentes estados y fechas de viaje
        Reserva reservaPendiente = new Reserva();
        reservaPendiente.setViaje(viaje1);  // Viaje con fecha 2025-10-05 (más lejana según dataTest.sql)
        reservaPendiente.setViajero(viajero);
        reservaPendiente.setFechaSolicitud(LocalDateTime.now());
        reservaPendiente.setEstado(EstadoReserva.PENDIENTE);

        Reserva reservaRechazada = new Reserva();
        reservaRechazada.setViaje(viaje2);  // Viaje con fecha 2025-09-25 (más cercana según dataTest.sql)
        reservaRechazada.setViajero(viajero);
        reservaRechazada.setFechaSolicitud(LocalDateTime.now());
        reservaRechazada.setEstado(EstadoReserva.RECHAZADA);
        reservaRechazada.setMotivoRechazo("No hay espacio");

        repositorioReserva.save(reservaPendiente);
        repositorioReserva.save(reservaRechazada);
        sessionFactory.getCurrentSession().flush();

        // when
        List<EstadoReserva> estados = List.of(EstadoReserva.PENDIENTE, EstadoReserva.RECHAZADA);
        List<Reserva> reservas = repositorioReserva.findByViajeroAndEstadoInOrderByViajesFechaSalidaAsc(viajero, estados);

        // then
        assertThat(reservas, hasSize(2));
        // Verificar que están ordenadas por fecha de salida del viaje (ASC - más cercano primero)
        // viaje2 tiene fecha 2025-09-25, viaje1 tiene fecha 2025-10-05
        assertThat(reservas.get(0).getEstado(), is(EstadoReserva.RECHAZADA));
        assertThat(reservas.get(0).getViaje().getId(), is(viaje2.getId()));
        assertThat(reservas.get(1).getEstado(), is(EstadoReserva.PENDIENTE));
        assertThat(reservas.get(1).getViaje().getId(), is(viaje1.getId()));
    }

    @Test
    public void deberiaRetornarListaVaciaCuandoNoHayReservasPorViajeroYEstados() {
        // given
        Viajero viajero = sessionFactory.getCurrentSession().get(Viajero.class, 4L);
        List<EstadoReserva> estados = List.of(EstadoReserva.PENDIENTE, EstadoReserva.RECHAZADA);

        // when
        List<Reserva> reservas = repositorioReserva.findByViajeroAndEstadoInOrderByViajesFechaSalidaAsc(viajero, estados);

        // then
        assertThat(reservas, hasSize(0));
    }

    @Test
    public void deberiaFiltrarCorrectamenteSoloPendientesYRechazadas() {
        // given
        Viaje viaje1 = sessionFactory.getCurrentSession().get(Viaje.class, 1L);
        Viaje viaje2 = sessionFactory.getCurrentSession().get(Viaje.class, 2L);
        Viaje viaje3 = sessionFactory.getCurrentSession().get(Viaje.class, 3L);
        Viajero viajero = sessionFactory.getCurrentSession().get(Viajero.class, 4L);

        Reserva reservaPendiente = new Reserva();
        reservaPendiente.setViaje(viaje1);
        reservaPendiente.setViajero(viajero);
        reservaPendiente.setFechaSolicitud(LocalDateTime.now());
        reservaPendiente.setEstado(EstadoReserva.PENDIENTE);

        Reserva reservaConfirmada = new Reserva();
        reservaConfirmada.setViaje(viaje2);
        reservaConfirmada.setViajero(viajero);
        reservaConfirmada.setFechaSolicitud(LocalDateTime.now());
        reservaConfirmada.setEstado(EstadoReserva.CONFIRMADA);

        Reserva reservaRechazada = new Reserva();
        reservaRechazada.setViaje(viaje3);
        reservaRechazada.setViajero(viajero);
        reservaRechazada.setFechaSolicitud(LocalDateTime.now());
        reservaRechazada.setEstado(EstadoReserva.RECHAZADA);

        repositorioReserva.save(reservaPendiente);
        repositorioReserva.save(reservaConfirmada);
        repositorioReserva.save(reservaRechazada);
        sessionFactory.getCurrentSession().flush();

        // when
        List<EstadoReserva> estados = List.of(EstadoReserva.PENDIENTE, EstadoReserva.RECHAZADA);
        List<Reserva> reservas = repositorioReserva.findByViajeroAndEstadoInOrderByViajesFechaSalidaAsc(viajero, estados);

        // then
        assertThat(reservas, hasSize(2));
        // Verificar que NO incluye la reserva confirmada
        for (Reserva reserva : reservas) {
            assertThat(reserva.getEstado(), isIn(List.of(EstadoReserva.PENDIENTE, EstadoReserva.RECHAZADA)));
        }
    }

    @Test
    public void deberiaBuscarViajesConfirmadosPorViajero() {
        // given
        Viaje viaje1 = sessionFactory.getCurrentSession().get(Viaje.class, 1L);
        Viaje viaje2 = sessionFactory.getCurrentSession().get(Viaje.class, 2L);
        Viajero viajero = sessionFactory.getCurrentSession().get(Viajero.class, 4L);

        // Crear reservas confirmadas
        Reserva reservaConfirmada1 = new Reserva();
        reservaConfirmada1.setViaje(viaje1);
        reservaConfirmada1.setViajero(viajero);
        reservaConfirmada1.setFechaSolicitud(LocalDateTime.now());
        reservaConfirmada1.setEstado(EstadoReserva.CONFIRMADA);

        Reserva reservaConfirmada2 = new Reserva();
        reservaConfirmada2.setViaje(viaje2);
        reservaConfirmada2.setViajero(viajero);
        reservaConfirmada2.setFechaSolicitud(LocalDateTime.now());
        reservaConfirmada2.setEstado(EstadoReserva.CONFIRMADA);

        repositorioReserva.save(reservaConfirmada1);
        repositorioReserva.save(reservaConfirmada2);
        sessionFactory.getCurrentSession().flush();

        // when
        List<Reserva> reservas = repositorioReserva.findViajesConfirmadosPorViajero(viajero);

        // then
        assertThat(reservas, hasSize(3));
        assertThat(reservas.get(0).getEstado(), is(EstadoReserva.CONFIRMADA));
        assertThat(reservas.get(1).getEstado(), is(EstadoReserva.CONFIRMADA));
    }

    @Test
    public void deberiaRetornarListaVaciaCuandoNoHayReservasConfirmadas() {
        // given
        Viajero viajero = sessionFactory.getCurrentSession().get(Viajero.class, 4L);

        // when
        List<Reserva> reservas = repositorioReserva.findViajesConfirmadosPorViajero(viajero);

        // then
        assertThat(reservas, hasSize(1));
    }

    @Test
    public void deberiaFiltrarSoloReservasConfirmadasYNoOtrosEstados() {
        // given
        Viaje viaje1 = sessionFactory.getCurrentSession().get(Viaje.class, 1L);
        Viaje viaje2 = sessionFactory.getCurrentSession().get(Viaje.class, 2L);
        Viaje viaje3 = sessionFactory.getCurrentSession().get(Viaje.class, 3L);
        Viajero viajero = sessionFactory.getCurrentSession().get(Viajero.class, 4L);

        Reserva reservaConfirmada = new Reserva();
        reservaConfirmada.setViaje(viaje1);
        reservaConfirmada.setViajero(viajero);
        reservaConfirmada.setFechaSolicitud(LocalDateTime.now());
        reservaConfirmada.setEstado(EstadoReserva.CONFIRMADA);

        Reserva reservaPendiente = new Reserva();
        reservaPendiente.setViaje(viaje2);
        reservaPendiente.setViajero(viajero);
        reservaPendiente.setFechaSolicitud(LocalDateTime.now());
        reservaPendiente.setEstado(EstadoReserva.PENDIENTE);

        Reserva reservaRechazada = new Reserva();
        reservaRechazada.setViaje(viaje3);
        reservaRechazada.setViajero(viajero);
        reservaRechazada.setFechaSolicitud(LocalDateTime.now());
        reservaRechazada.setEstado(EstadoReserva.RECHAZADA);

        repositorioReserva.save(reservaConfirmada);
        repositorioReserva.save(reservaPendiente);
        repositorioReserva.save(reservaRechazada);
        sessionFactory.getCurrentSession().flush();

        // when
        List<Reserva> reservas = repositorioReserva.findViajesConfirmadosPorViajero(viajero);

        // then
        assertThat(reservas, hasSize(2));
        assertThat(reservas.get(0).getEstado(), is(EstadoReserva.CONFIRMADA));
    }
}
