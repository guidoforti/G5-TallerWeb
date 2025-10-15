package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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



@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HibernateTestConfig.class, DataBaseTestInitilizationConfig.class})
@Transactional
public class RepositorioViajeTest{
    @Autowired
    SessionFactory sessionFactory;

    private ViajeRepository repositorioViaje;

    @BeforeEach
    void setUp() {
        this.repositorioViaje = new RepositorioViajeImpl(this.sessionFactory);
    }

@Test
void deberiaModificarViajeExistente() {
    // Arrange
    Conductor conductor = sessionFactory.getCurrentSession().get(Conductor.class, 1L);
    Vehiculo vehiculo = sessionFactory.getCurrentSession().get(Vehiculo.class, 1L);

    Viaje viaje = new Viaje();
    viaje.setFechaHoraDeSalida(LocalDateTime.now().plusDays(1));
    viaje.setPrecio(1000.0);
    viaje.setAsientosDisponibles(2);
    viaje.setEstado(EstadoDeViaje.DISPONIBLE);
    viaje.setConductor(conductor);
    viaje.setVehiculo(vehiculo);

    repositorioViaje.guardarViaje(viaje);
    Long idGenerado = viaje.getId();

    // Act
    viaje.setEstado(EstadoDeViaje.CANCELADO);
    repositorioViaje.modificarViaje(viaje);

    // Assert
    Viaje modificado = repositorioViaje.findById(idGenerado);
    assertThat(modificado.getEstado(), equalTo(EstadoDeViaje.CANCELADO));
}


    @Test
void deberiaBorrarViajePorId() {
    // Arrange
    Conductor conductor = sessionFactory.getCurrentSession().get(Conductor.class, 1L);
    Vehiculo vehiculo = sessionFactory.getCurrentSession().get(Vehiculo.class, 1L);

    Viaje viaje = new Viaje();
    viaje.setFechaHoraDeSalida(LocalDateTime.now().plusDays(1));
    viaje.setPrecio(1000.0);
    viaje.setAsientosDisponibles(2);
    viaje.setEstado(EstadoDeViaje.DISPONIBLE);
    viaje.setConductor(conductor);
    viaje.setVehiculo(vehiculo);

    repositorioViaje.guardarViaje(viaje);
    Long idGenerado = viaje.getId();

    // Act
    repositorioViaje.borrarViaje(idGenerado);
    sessionFactory.getCurrentSession().flush();

    // Assert
    Viaje borrado = repositorioViaje.findById(idGenerado);
    assertNull(borrado, "El viaje debería haber sido eliminado");
}




    @Test
void deberiaBuscarPorOrigenDestinoYConductor() {
    // 1. ARRANGE - CREACIÓN Y PERSISTENCIA DE CIUDADES CON COORDENADAS ÚNICAS
    // Usamos 'f' para el tipo float y valores diferentes.
    Ciudad origen = new Ciudad(null, "San Justo", 0.0f, 0.0f); 
    Ciudad destino = new Ciudad(null, "La Plata", 1.0f, 1.0f); // Valores únicos para no violar Constraint

    // Usamos saveOrUpdate y flush() para asegurar que las entidades se guarden 
    // y obtengan sus IDs antes de que Viaje las referencie.
    sessionFactory.getCurrentSession().saveOrUpdate(origen); 
    sessionFactory.getCurrentSession().saveOrUpdate(destino);
    sessionFactory.getCurrentSession().flush(); 

    // 2. ARRANGE - CREACIÓN Y PERSISTENCIA DEL CONDUCTOR
    // Creamos el conductor para asegurar que existe, en lugar de usar get(..., 1L).
    Conductor conductor = new Conductor();
    conductor.setNombre("Conductor Principal");
    conductor.setEmail("principal@correo.com");
    conductor.setContrasenia("123456");
    sessionFactory.getCurrentSession().saveOrUpdate(conductor);

    // 3. ARRANGE - CREACIÓN Y PERSISTENCIA DEL VIAJE
    Viaje viaje = new Viaje();
    viaje.setFechaHoraDeSalida(LocalDateTime.now().plusDays(1));
    viaje.setPrecio(1000.0);
    viaje.setAsientosDisponibles(2);
    viaje.setEstado(EstadoDeViaje.DISPONIBLE);
    
    // Asignamos las entidades persistidas
    viaje.setOrigen(origen);
    viaje.setDestino(destino);
    viaje.setConductor(conductor);

    repositorioViaje.guardarViaje(viaje);

    // Act
    List<Viaje> resultados = repositorioViaje.findByOrigenYDestinoYConductor(origen, destino, conductor);

    // Assert
    assertThat(resultados, hasSize(1));
    assertThat(resultados.get(0).getId(), equalTo(viaje.getId()));
}




@Test
void noDeberiaEncontrarViajeSiConductorNoCoincide() {
    // 1. ARRANGE - CREACIÓN Y PERSISTENCIA DE CIUDADES CON COORDENADAS ÚNICAS
    // Es crucial usar 0f y 1f (o cualquier valor distinto)
    Ciudad origen = new Ciudad(null, "San Justo", 0.0f, 0.0f); 
    Ciudad destino = new Ciudad(null, "La Plata", 1.0f, 1.0f); // Lat y Lon DIFERENTES
    
    // Usamos saveOrUpdate para ser más robustos
    sessionFactory.getCurrentSession().saveOrUpdate(origen);
    sessionFactory.getCurrentSession().saveOrUpdate(destino);
    
    // Forzamos el flush para que los IDs se generen antes de continuar,
    // previniendo otros errores de Constraint/FK.
    sessionFactory.getCurrentSession().flush(); 

    // 2. ARRANGE - CREACIÓN Y PERSISTENCIA DEL CONDUCTOR
    // Crear el conductor es más seguro que usar get(..., 1L)
    Conductor conductorCorrecto = new Conductor();
    conductorCorrecto.setNombre("Conductor OK");
    conductorCorrecto.setEmail("ok@correo.com");
    conductorCorrecto.setContrasenia("123456");
    sessionFactory.getCurrentSession().saveOrUpdate(conductorCorrecto);
    
    Conductor otroConductor = new Conductor();
    otroConductor.setNombre("Otro");
    otroConductor.setEmail("otro@correo.com");
    otroConductor.setContrasenia("123456");
    sessionFactory.getCurrentSession().saveOrUpdate(otroConductor);

    // 3. ARRANGE - CREACIÓN Y PERSISTENCIA DEL VIAJE
    Viaje viaje = new Viaje();
    viaje.setFechaHoraDeSalida(LocalDateTime.now().plusDays(1));
    viaje.setPrecio(1000.0);
    viaje.setAsientosDisponibles(2);
    viaje.setEstado(EstadoDeViaje.DISPONIBLE);
    viaje.setOrigen(origen);
    viaje.setDestino(destino);
    viaje.setConductor(otroConductor);

    repositorioViaje.guardarViaje(viaje);

    // Act
    List<Viaje> resultados = repositorioViaje.findByOrigenYDestinoYConductor(origen, destino, conductorCorrecto);

    // Assert
    assertThat(resultados, empty());
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
        repositorioViaje.modificarViaje(viaje);

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
    void deberiaEncontrarViajesPorConductor() {
    // 1. ARRANGE - CREACIÓN Y PERSISTENCIA DE CIUDADES CON COORDENADAS ÚNICAS
    Ciudad origen = new Ciudad(null, "Morón", 0.0f, 0.0f); // Coordenadas de Origen
    Ciudad destino = new Ciudad(null, "Lanús", 1.0f, 1.0f); // Coordenadas DIFERENTES para evitar ConstraintViolation

    // Persistir ciudades
    sessionFactory.getCurrentSession().persist(origen); 
    sessionFactory.getCurrentSession().persist(destino);
    
    // Forzar el flush para que los IDs se generen y se registren en la DB antes de que Viaje las use.
    sessionFactory.getCurrentSession().flush(); 

    // 2. ARRANGE - CREACIÓN Y PERSISTENCIA DEL CONDUCTOR
    Conductor conductor = new Conductor();
    conductor.setNombre("Test");
    conductor.setEmail("test@correo.com");
    conductor.setContrasenia("123456");
    sessionFactory.getCurrentSession().persist(conductor);

    // 3. ARRANGE - CREACIÓN Y PERSISTENCIA DE VIAJES
    
    // Viaje 1
    Viaje viaje1 = new Viaje();
    viaje1.setFechaHoraDeSalida(LocalDateTime.now().plusDays(2));
    viaje1.setPrecio(1200.0);
    viaje1.setAsientosDisponibles(4);
    viaje1.setEstado(EstadoDeViaje.DISPONIBLE);
    viaje1.setOrigen(origen);
    viaje1.setDestino(destino);
    viaje1.setConductor(conductor);
    sessionFactory.getCurrentSession().persist(viaje1);
    
    // Viaje 2
    Viaje viaje2 = new Viaje();
    viaje2.setFechaHoraDeSalida(LocalDateTime.now().plusDays(3));
    viaje2.setPrecio(1800.0);
    viaje2.setAsientosDisponibles(2);
    viaje2.setEstado(EstadoDeViaje.DISPONIBLE);
    viaje2.setOrigen(origen);
    viaje2.setDestino(destino);
    viaje2.setConductor(conductor);
    sessionFactory.getCurrentSession().persist(viaje2);

    // Act
    List<Viaje> resultados = repositorioViaje.findByConductorId(conductor.getId());

    // Assert
    assertNotNull(resultados);
    assertEquals(2, resultados.size(), "Debería encontrar 2 viajes para el conductor");
    assertTrue(resultados.stream().allMatch(v -> v.getConductor().getId().equals(conductor.getId())));
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
