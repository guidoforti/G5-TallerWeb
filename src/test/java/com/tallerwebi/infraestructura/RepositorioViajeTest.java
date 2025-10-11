package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.Matchers.*;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.tallerwebi.dominio.Entity.Ciudad;
import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Enums.EstadoDeViaje;
import com.tallerwebi.dominio.IRepository.ViajeRepository;
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

/*@Test
    void deberiaModificarViajeExistente() {
        Viaje viaje = new Viaje();
        viaje.setId(300L);
        viaje.setEstado(EstadoDeViaje.DISPONIBLE);
        repositorioViaje.guardarViaje(viaje);

        viaje.setEstado(EstadoDeViaje.CANCELADO);
        repositorioViaje.modificarViaje(viaje);

        Viaje modificado = repositorioViaje.findById(300L);
        assertThat(modificado.getEstado(), equalTo(EstadoDeViaje.CANCELADO));
    }

    @Test
    void deberiaBorrarViajePorId() {
        Viaje viaje = new Viaje();
        viaje.setId(400L);
        repositorioViaje.guardarViaje(viaje);

        repositorioViaje.borrarViaje(400L);

        assertThrows(NoSuchElementException.class, () -> repositorioViaje.findById(400L));
    }



    @Test
    void deberiaBuscarPorOrigenDestinoYConductor() {
        Ciudad origen = new Ciudad(null, "San Justo", 0, 0);
        Ciudad destino = new Ciudad(null, "La Plata", 0, 0);
        Conductor conductor = new Conductor();
        conductor.setId(1L);

        Viaje viaje = new Viaje();
        viaje.setId(500L);
        viaje.setOrigen(origen);
        viaje.setDestino(destino);
        viaje.setConductor(conductor);

        repositorioViaje.guardarViaje(viaje);

        List<Viaje> resultados = repositorioViaje.findByOrigenYDestinoYConductor(origen, destino, conductor);

        assertThat(resultados, hasSize(1));
        assertThat(resultados.get(0).getId(), equalTo(500L));
    }

    @Test
    void noDeberiaEncontrarViajeSiConductorNoCoincide() {
        Ciudad origen = new Ciudad(null, "San Justo", 0, 0);
        Ciudad destino = new Ciudad(null, "La Plata", 0, 0);
        Conductor conductor = new Conductor();
        conductor.setId(1L);

        Conductor otro = new Conductor();
        otro.setId(99L);

        Viaje viaje = new Viaje();
        viaje.setId(600L);
        viaje.setOrigen(origen);
        viaje.setDestino(destino);
        viaje.setConductor(otro);

        repositorioViaje.guardarViaje(viaje);

        List<Viaje> resultados = repositorioViaje.findByOrigenYDestinoYConductor(origen, destino, conductor);

        assertThat(resultados, empty());
    }*/

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
        Viaje resultado = repositorioViaje.findById(viajeId);

        // Assert
        assertNotNull(resultado);
        assertEquals(viajeId, resultado.getId());
        assertEquals(15000.0, resultado.getPrecio());
        assertEquals(3, resultado.getAsientosDisponibles());
        assertEquals(1L, resultado.getConductor().getId());
        assertEquals(1L, resultado.getVehiculo().getId());
    }

    @Test
    void deberiaModificarViaje() {
        // Arrange - Usar viaje existente de dataTest.sql (id=2)
        Viaje viaje = repositorioViaje.findById(2L);
        Double precioOriginal = viaje.getPrecio();
        Integer asientosOriginales = viaje.getAsientosDisponibles();

        // Act
        viaje.setPrecio(20000.0);
        viaje.setAsientosDisponibles(1);
        repositorioViaje.modificarViaje(viaje);

        // Assert
        Viaje modificado = repositorioViaje.findById(2L);
        assertEquals(20000.0, modificado.getPrecio());
        assertEquals(1, modificado.getAsientosDisponibles());
        assertNotEquals(precioOriginal, modificado.getPrecio());
        assertNotEquals(asientosOriginales, modificado.getAsientosDisponibles());
    }

    @Test
    void deberiaBorrarViaje() {
        // Arrange - Usar viaje existente de dataTest.sql (id=3)
        Long viajeId = 3L;
        Viaje viajeExistente = repositorioViaje.findById(viajeId);
        assertNotNull(viajeExistente, "El viaje debería existir antes de borrarlo");

        // Act
        repositorioViaje.borrarViaje(viajeId);
        sessionFactory.getCurrentSession().flush();

        // Assert
        Viaje borrado = repositorioViaje.findById(viajeId);
        assertNull(borrado, "El viaje debería haber sido eliminado");
    }

}
