package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Entity.*;
import com.tallerwebi.dominio.IRepository.RepositorioHistorialReserva;
import com.tallerwebi.dominio.Enums.EstadoReserva;
import com.tallerwebi.dominio.Enums.EstadoDeViaje;
import com.tallerwebi.integracion.config.DataBaseTestInitilizationConfig;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HibernateTestConfig.class, DataBaseTestInitilizationConfig.class})
@Transactional
public class RepositorioHistorialReservaTest {

    @Autowired
    private SessionFactory sessionFactory;

    private RepositorioHistorialReserva repositorioHistorialReserva;

    @BeforeEach
    void setUp() {
        this.repositorioHistorialReserva = new RepositorioHistorialReservaImpl(this.sessionFactory);
    }

    // --- HELPERS PARA CREAR ENTIDADES PERSISTENTES DE FORMA SEGURA ---

    /** Crea y persiste una instancia de Viaje con estado y conductor básico. */
    private Viaje crearViajePersistente(Long id, EstadoDeViaje estado, Conductor conductor) {
        Viaje viaje = new Viaje();
        viaje.setId(id);
        viaje.setEstado(estado);
        viaje.setConductor(conductor);
        // Asignar FKs NOT NULL para evitar fallos de integridad (ej: Origen/Destino)
        Ciudad ciudadDummy = new Ciudad();
        sessionFactory.getCurrentSession().save(ciudadDummy);
        viaje.setOrigen(ciudadDummy);
        viaje.setDestino(ciudadDummy);

        sessionFactory.getCurrentSession().saveOrUpdate(viaje);
        return viaje;
    }

    /** Crea y persiste una instancia de Conductor/Viajero. */
    private <T extends Usuario> T crearUsuarioPersistente(T usuario, Long id) {
        usuario.setId(id);
        usuario.setRol(usuario instanceof Conductor ? "CONDUCTOR" : "VIAJERO");
        usuario.setEmail(usuario.getRol() + id + "@test.com");
        sessionFactory.getCurrentSession().save(usuario);
        // Persistir la tabla Joined para el rol específico
        if (usuario instanceof Conductor) {
            Conductor c = (Conductor) usuario;
            sessionFactory.getCurrentSession().save(c);
        } else if (usuario instanceof Viajero) {
            Viajero v = (Viajero) usuario;
            sessionFactory.getCurrentSession().save(v);
        }
        return usuario;
    }
    // ----------------------------------------------------------------------


    @Test
    public void deberiaGuardarHistorialReserva() {
        // given
        Viajero viajero = crearUsuarioPersistente(new Viajero(), 40L);
        Conductor conductor = crearUsuarioPersistente(new Conductor(), 20L);
        Viaje viaje = crearViajePersistente(1L, EstadoDeViaje.DISPONIBLE, conductor);

        // 1. Crear y guardar la RESERVA (Requiere Viajero y Viaje NO NULOS)
        Reserva reserva = new Reserva();
        reserva.setViaje(viaje);
        reserva.setViajero(viajero); // 🔥 Asignación CRÍTICA (NOT NULL)
        reserva.setFechaSolicitud(LocalDateTime.now());
        reserva.setEstado(EstadoReserva.PENDIENTE);
        sessionFactory.getCurrentSession().save(reserva);

        // 2. Crear Historial
        HistorialReserva historial = new HistorialReserva();
        historial.setReserva(reserva);
        historial.setViaje(viaje);
        historial.setViajero(viajero);
        historial.setConductor(conductor);
        historial.setFechaEvento(LocalDateTime.now());
        historial.setEstadoAnterior(EstadoReserva.PENDIENTE);
        historial.setEstadoNuevo(EstadoReserva.CONFIRMADA);

        // when
        repositorioHistorialReserva.save(historial);
        sessionFactory.getCurrentSession().flush();

        // then
        List<HistorialReserva> resultados = sessionFactory.getCurrentSession()
                .createQuery("FROM HistorialReserva", HistorialReserva.class)
                .getResultList();

        assertThat(resultados, hasSize(1));
        assertThat(resultados.get(0).getEstadoAnterior(), is(EstadoReserva.PENDIENTE));
        assertThat(resultados.get(0).getEstadoNuevo(), is(EstadoReserva.CONFIRMADA));
    }

    @Test
    public void deberiaBuscarHistorialPorViaje() {
        // given
        Viajero viajero = crearUsuarioPersistente(new Viajero(), 41L);
        Conductor conductor = crearUsuarioPersistente(new Conductor(), 21L);
        Viaje viaje = crearViajePersistente(2L, EstadoDeViaje.DISPONIBLE, conductor);

        // 1. Crear y guardar la RESERVA
        Reserva reserva = new Reserva();
        reserva.setViaje(viaje);
        reserva.setViajero(viajero);
        reserva.setFechaSolicitud(LocalDateTime.now());
        reserva.setEstado(EstadoReserva.PENDIENTE);
        sessionFactory.getCurrentSession().save(reserva);

        // 2. Historial 1 (Antiguo)
        HistorialReserva historial1 = new HistorialReserva();
        historial1.setReserva(reserva);
        historial1.setViaje(viaje);
        historial1.setViajero(viajero);
        historial1.setConductor(conductor);
        historial1.setFechaEvento(LocalDateTime.now().minusDays(2));
        historial1.setEstadoAnterior(EstadoReserva.PENDIENTE);
        historial1.setEstadoNuevo(EstadoReserva.CONFIRMADA);

        // 3. Historial 2 (Reciente)
        HistorialReserva historial2 = new HistorialReserva();
        historial2.setReserva(reserva);
        historial2.setViaje(viaje);
        historial2.setViajero(viajero);
        historial2.setConductor(conductor);
        historial2.setFechaEvento(LocalDateTime.now().minusDays(1));
        historial2.setEstadoAnterior(EstadoReserva.CONFIRMADA);
        historial2.setEstadoNuevo(EstadoReserva.CANCELADA_POR_VIAJERO);

        sessionFactory.getCurrentSession().save(historial1);
        sessionFactory.getCurrentSession().save(historial2);
        sessionFactory.getCurrentSession().flush();

        // when
        List<HistorialReserva> resultados = repositorioHistorialReserva.findByViaje(viaje);

        // then
        assertThat(resultados, hasSize(2));
        assertThat(resultados.get(0).getEstadoNuevo(), is(EstadoReserva.CONFIRMADA));
        assertThat(resultados.get(1).getEstadoNuevo(), is(EstadoReserva.CANCELADA_POR_VIAJERO));
    }

    @Test
    public void deberiaBuscarPrimerHistorialPorIdViaje() {
        // given
        Viajero viajero = crearUsuarioPersistente(new Viajero(), 42L);
        Conductor conductor = crearUsuarioPersistente(new Conductor(), 22L);
        Viaje viaje = crearViajePersistente(3L, EstadoDeViaje.DISPONIBLE, conductor);

        Reserva reserva = new Reserva();
        reserva.setViaje(viaje);
        reserva.setViajero(viajero);
        reserva.setFechaSolicitud(LocalDateTime.now());
        reserva.setEstado(EstadoReserva.PENDIENTE);
        sessionFactory.getCurrentSession().save(reserva);

        HistorialReserva historial = new HistorialReserva();
        historial.setReserva(reserva);
        historial.setViaje(viaje);
        historial.setViajero(viajero);
        historial.setConductor(conductor);
        historial.setFechaEvento(LocalDateTime.now());
        historial.setEstadoAnterior(EstadoReserva.PENDIENTE);
        historial.setEstadoNuevo(EstadoReserva.CONFIRMADA);

        sessionFactory.getCurrentSession().save(historial);
        sessionFactory.getCurrentSession().flush();

        // when
        Optional<HistorialReserva> resultado = repositorioHistorialReserva.findByViajeId(viaje.getId());

        // then
        assertTrue(resultado.isPresent());
        assertThat(resultado.get().getEstadoNuevo(), is(EstadoReserva.CONFIRMADA));
    }

    @Test
    public void deberiaRetornarOptionalVacioSiNoHayHistorialParaViaje() {
        // given
        // Creamos un viaje que no tiene historial asociado (ID 4)
        Viaje viajeSinHistorial = crearViajePersistente(4L, EstadoDeViaje.DISPONIBLE, crearUsuarioPersistente(new Conductor(), 23L));

        // when
        Optional<HistorialReserva> resultado = repositorioHistorialReserva.findByViajeId(viajeSinHistorial.getId());

        // then
        assertTrue(resultado.isEmpty());
    }


}