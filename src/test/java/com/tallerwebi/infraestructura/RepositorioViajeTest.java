package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Entity.Ciudad;
import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Vehiculo;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Enums.EstadoDeViaje;
import com.tallerwebi.dominio.IRepository.ViajeRepository;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HibernateTestConfig.class, DataBaseTestInitilizationConfig.class})
@Transactional
public class RepositorioViajeTest {

    @Autowired
    SessionFactory sessionFactory;

    private ViajeRepository repositorioViaje;

    @BeforeEach
    void setUp() {
        this.repositorioViaje = new RepositorioViajeImpl(this.sessionFactory);
    }

    @Test
    void deberiaGuardarViajeNuevo() {
        // Arrange - Usar conductor y vehiculo existentes de dataTest.sql
        Conductor conductor = sessionFactory.getCurrentSession().get(Conductor.class, 1L);
        Vehiculo vehiculo = sessionFactory.getCurrentSession().get(Vehiculo.class, 1L);

        Viaje viaje = new Viaje();
        viaje.setFechaHoraDeSalida(LocalDateTime.now().plusDays(1));
        viaje.setPrecio(1500.0);
        viaje.setAsientosDisponibles(3);
        viaje.setFechaDeCreacion(LocalDateTime.now());
        viaje.setEstado(EstadoDeViaje.DISPONIBLE);
        viaje.setViajeros(new ArrayList<>());
        viaje.setParadas(new ArrayList<>());
        viaje.setConductor(conductor);
        viaje.setVehiculo(vehiculo);

        // Act
        repositorioViaje.guardarViaje(viaje);

        // Assert
        assertNotNull(viaje.getId(), "El viaje debería tener un ID asignado después de guardarse");

        // Usar directamente Hibernate para recuperar el viaje sin depender del método findById
        Viaje recuperado = sessionFactory.getCurrentSession().get(Viaje.class, viaje.getId());
        assertNotNull(recuperado, "Debería poder recuperarse el viaje guardado");
        assertEquals(1500.0, recuperado.getPrecio());
        assertEquals(3, recuperado.getAsientosDisponibles());
        assertEquals(EstadoDeViaje.DISPONIBLE, recuperado.getEstado());
        assertEquals(conductor.getId(), recuperado.getConductor().getId());
        assertEquals(vehiculo.getId(), recuperado.getVehiculo().getId());
    }

    @Test
    void deberiaBuscarViajePorId() {
        // Arrange - Usar viaje existente de dataTest.sql (id=1)
        Long viajeId = 1L;

        // Act
        Optional<Viaje> resultadoOptional = repositorioViaje.findById(viajeId);

        // Assert
        assertTrue(resultadoOptional.isPresent(), "El Optional debería contener el viaje");
        Viaje resultado = resultadoOptional.get();

        assertEquals(viajeId, resultado.getId());
        assertEquals(15000.0, resultado.getPrecio());
        assertEquals(3, resultado.getAsientosDisponibles());
        assertEquals(1L, resultado.getConductor().getId());
        assertEquals(1L, resultado.getVehiculo().getId());
    }

    @Test
    void deberiaRetornarOptionalVacioSiNoExisteViaje() {
        Long idInexistente = 999L;
        // Act
        Optional<Viaje> resultadoOptional = repositorioViaje.findById(idInexistente);
        // Assert
        assertTrue(resultadoOptional.isEmpty(), "El Optional debería estar vacío para un ID inexistente");
    }

    @Test
    void deberiaModificarViaje() {
        // Arrange - Usar viaje existente de dataTest.sql (id=2)
        Viaje viaje = repositorioViaje.findById(2L).get();
        Double precioOriginal = viaje.getPrecio();
        Integer asientosOriginales = viaje.getAsientosDisponibles();

        // Act
        viaje.setPrecio(20000.0);
        viaje.setAsientosDisponibles(1);
        repositorioViaje.modificarViajer(viaje);

        // Assert
        Viaje modificado = repositorioViaje.findById(2L).get();
        assertEquals(20000.0, modificado.getPrecio());
        assertEquals(1, modificado.getAsientosDisponibles());
        assertNotEquals(precioOriginal, modificado.getPrecio());
        assertNotEquals(asientosOriginales, modificado.getAsientosDisponibles());
    }

    @Test
    void deberiaBorrarViaje() {
        // Arrange - Usar viaje existente de dataTest.sql (id=3)
        Long viajeId = 3L;
        assertTrue(repositorioViaje.findById(viajeId).isPresent(), "El viaje debería existir antes de borrarlo");
        // Act
        repositorioViaje.borrarViaje(viajeId);
        sessionFactory.getCurrentSession().flush();
        // Assert - Afirmamos que el Optional está vacío después de la operación
        assertTrue(repositorioViaje.findById(viajeId).isEmpty(), "El Optional debería estar vacío después de borrarlo");
    }

    @Test
    void deberiaIntentarBorrarViajeInexistenteSinExcepcion() {
        // Arrange
        Long idInexistente = 999L;
        // Aseguramos que el viaje no existe
        assertTrue(repositorioViaje.findById(idInexistente).isEmpty());

        assertDoesNotThrow(() -> {
            repositorioViaje.borrarViaje(idInexistente);
            sessionFactory.getCurrentSession().flush();
        }, "Borrar un viaje inexistente no debería lanzar excepción");
    }

    @Test
    void deberiaEncontrarViajesEnEstadosDisponibleYCompleto() {
        // Arrange - Usar datos de dataTest.sql
        // Viaje 1: Buenos Aires -> Cordoba, conductor 1, estado DISPONIBLE (0)
        Conductor conductor = sessionFactory.getCurrentSession().get(Conductor.class, 1L);
        Ciudad buenosAires = sessionFactory.getCurrentSession().get(Ciudad.class, 1L);
        Ciudad cordoba = sessionFactory.getCurrentSession().get(Ciudad.class, 2L);

        List<EstadoDeViaje> estadosProhibidos = Arrays.asList(EstadoDeViaje.DISPONIBLE, EstadoDeViaje.COMPLETO);

        // Act
        List<Viaje> viajesEncontrados = repositorioViaje.findByOrigenYDestinoYConductorYEstadoIn(
            buenosAires,
            cordoba,
            conductor,
            estadosProhibidos
        );

        // Assert
        assertNotNull(viajesEncontrados);
        assertEquals(1, viajesEncontrados.size(), "Debería encontrar el viaje existente en estado DISPONIBLE");
        assertEquals(1L, viajesEncontrados.get(0).getId());
        assertEquals(EstadoDeViaje.DISPONIBLE, viajesEncontrados.get(0).getEstado());
    }



    @Test
    void noDeberiaEncontrarViajesEnEstadosFinalizadoOCancelado() {
        // Arrange - Crear viaje en estado FINALIZADO
        Conductor conductor = sessionFactory.getCurrentSession().get(Conductor.class, 1L);
        Vehiculo vehiculo = sessionFactory.getCurrentSession().get(Vehiculo.class, 1L);
        Ciudad buenosAires = sessionFactory.getCurrentSession().get(Ciudad.class, 1L);
        Ciudad rosario = sessionFactory.getCurrentSession().get(Ciudad.class, 3L);

        Viaje viajeFinalizadoForTest = new Viaje();
        viajeFinalizadoForTest.setOrigen(buenosAires);
        viajeFinalizadoForTest.setDestino(rosario);
        viajeFinalizadoForTest.setConductor(conductor);
        viajeFinalizadoForTest.setVehiculo(vehiculo);
        viajeFinalizadoForTest.setEstado(EstadoDeViaje.FINALIZADO);
        viajeFinalizadoForTest.setFechaHoraDeSalida(LocalDateTime.now().plusDays(1));
        viajeFinalizadoForTest.setPrecio(10000.0);
        viajeFinalizadoForTest.setAsientosDisponibles(2);
        viajeFinalizadoForTest.setFechaDeCreacion(LocalDateTime.now());
        viajeFinalizadoForTest.setViajeros(new ArrayList<>());
        viajeFinalizadoForTest.setParadas(new ArrayList<>());

        repositorioViaje.guardarViaje(viajeFinalizadoForTest);
        sessionFactory.getCurrentSession().flush();

        List<EstadoDeViaje> estadosProhibidos = Arrays.asList(EstadoDeViaje.DISPONIBLE, EstadoDeViaje.COMPLETO);

        // Act
        List<Viaje> viajesEncontrados = repositorioViaje.findByOrigenYDestinoYConductorYEstadoIn(
            buenosAires,
            rosario,
            conductor,
            estadosProhibidos
        );

        // Assert
        assertNotNull(viajesEncontrados);
        assertEquals(0, viajesEncontrados.size(), "No debería encontrar viajes en estado FINALIZADO");
    }

    @Test
    void deberiaRetornarListaVaciaCuandoNoHayViajesConEsosEstados() {
        // Arrange
        Conductor conductor = sessionFactory.getCurrentSession().get(Conductor.class, 2L);
        Ciudad buenosAires = sessionFactory.getCurrentSession().get(Ciudad.class, 1L);
        Ciudad rosario = sessionFactory.getCurrentSession().get(Ciudad.class, 3L);

        List<EstadoDeViaje> estadosProhibidos = Arrays.asList(EstadoDeViaje.DISPONIBLE, EstadoDeViaje.COMPLETO);

        // Act - Buscar viaje que no existe (conductor 2 nunca viajó de Buenos Aires a Rosario)
        List<Viaje> viajesEncontrados = repositorioViaje.findByOrigenYDestinoYConductorYEstadoIn(
            buenosAires,
            rosario,
            conductor,
            estadosProhibidos
        );

        // Assert
        assertNotNull(viajesEncontrados);
        assertEquals(0, viajesEncontrados.size(), "Debería retornar lista vacía");
    }
    @Test
    void deberiaEncontrarViajesPorOrigenDestinoYConductor() {
        Conductor conductor = sessionFactory.getCurrentSession().get(Conductor.class, 1L);
        Ciudad buenosAires = sessionFactory.getCurrentSession().get(Ciudad.class, 1L);
        Ciudad cordoba = sessionFactory.getCurrentSession().get(Ciudad.class, 2L);

        List<Viaje> viajesEncontrados = repositorioViaje.findByOrigenYDestinoYConductor(
                buenosAires,
                cordoba,
                conductor
        );

        assertNotNull(viajesEncontrados);
        assertEquals(1, viajesEncontrados.size(), "Debería encontrar el viaje 1L");
        assertEquals(1L, viajesEncontrados.get(0).getId());
    }

    @Test
    void deberiaRetornarListaVaciaSiNoHayViajesPorOrigenDestinoYConductor() {
        Conductor conductor = sessionFactory.getCurrentSession().get(Conductor.class, 2L);
        Ciudad buenosAires = sessionFactory.getCurrentSession().get(Ciudad.class, 1L);
        Ciudad rosario = sessionFactory.getCurrentSession().get(Ciudad.class, 3L);

        // Act
        List<Viaje> viajesEncontrados = repositorioViaje.findByOrigenYDestinoYConductor(
                buenosAires,
                rosario,
                conductor
        );

        assertNotNull(viajesEncontrados);
        assertTrue(viajesEncontrados.isEmpty(), "Debería retornar una lista vacía");
    }
}
