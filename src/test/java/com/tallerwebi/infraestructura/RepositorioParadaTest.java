package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Enums.EstadoDeViaje;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.config.SpringWebConfig;
import com.tallerwebi.dominio.Entity.Ciudad;
import com.tallerwebi.dominio.Entity.Vehiculo;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Parada;
import com.tallerwebi.dominio.IRepository.RepositorioParada;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test de Integración para RepositorioParadaImpl.
 * Verifica la funcionalidad básica del método findByid(Long id),
 * respetando las relaciones (Ciudad) de la entidad Parada.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SpringWebConfig.class, HibernateTestConfig.class})
@Transactional
@WebAppConfiguration
public class RepositorioParadaTest {

    @Autowired
    private SessionFactory sessionFactory;

    private RepositorioParada repositorioParada;
    private Ciudad ciudadBase;
    private Viaje viajeBase;


    @BeforeEach
    public void setUp() {
        // Inicialización manual del repositorio
        this.repositorioParada = new RepositorioParadaImpl(this.sessionFactory);

        ciudadBase = new Ciudad();
        ciudadBase.setNombre("Ciudad Test");
        ciudadBase.setLatitud(10);
        ciudadBase.setLongitud(20);
        sessionFactory.getCurrentSession().save(ciudadBase);

        // 2. ARRANGE: Crear Conductor y Vehiculo (Requeridos por Viaje)
        Conductor conductor = new Conductor();
        conductor.setEmail("test_conductor@mail.com");
        conductor.setContrasenia("pass");
        conductor.setNombre("Conductor Test");
        conductor.setFechaDeVencimientoLicencia(LocalDate.now().plusYears(1));
        sessionFactory.getCurrentSession().save(conductor);

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPatente("ABC999");
        vehiculo.setModelo("Test Car");
        vehiculo.setAnio("2020");
        vehiculo.setAsientosTotales(4);
        vehiculo.setConductor(conductor);
        sessionFactory.getCurrentSession().save(vehiculo);

        // 3. ARRANGE: Crear Viaje (Requerido por Parada)
        viajeBase = new Viaje();
        viajeBase.setConductor(conductor);
        viajeBase.setVehiculo(vehiculo);
        viajeBase.setOrigen(ciudadBase);
        viajeBase.setDestino(ciudadBase);
        viajeBase.setFechaHoraDeSalida(LocalDateTime.now().plusDays(1));
        viajeBase.setAsientosDisponibles(3);
        viajeBase.setPrecio(100.0);
        viajeBase.setEstado(EstadoDeViaje.DISPONIBLE);
        viajeBase.setFechaDeCreacion(LocalDateTime.now());
        sessionFactory.getCurrentSession().save(viajeBase);

        sessionFactory.getCurrentSession().flush();
    }

    /**
     * Test para verificar que se puede guardar una parada y luego encontrarla por ID.
     */
    @Test
    public void findById_deberiaEncontrarParadaExistente() {

        Parada parada = new Parada();
        parada.setCiudad(ciudadBase);
        parada.setViaje(viajeBase);
        parada.setOrden(1);

        // WHEN: Persisto la parada directamente en la sesión
        Long idGuardado = (Long) sessionFactory.getCurrentSession().save(parada);
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        // WHEN: Busco la parada usando el repositorio
        Optional<Parada> resultado = repositorioParada.findByid(idGuardado);

        // THEN: El resultado debe estar presente y los datos deben coincidir
        assertTrue(resultado.isPresent(), "Debería encontrar la parada por el ID guardado.");
        assertThat(resultado.get().getId(), is(idGuardado));
        assertThat(resultado.get().getOrden(), is(1));
        // Verificamos que las relaciones se recuperaron correctamente
        assertThat(resultado.get().getCiudad().getNombre(), is("Ciudad Test"));
        assertThat(resultado.get().getViaje().getId(), is(viajeBase.getId()));
    }

    /**
     * Test para verificar que retorna Optional.empty() cuando la parada no existe.
     */
    @Test
    public void findById_deberiaRetornarOptionalVacioSiParadaNoExiste() {
        // GIVEN: Un ID que sabemos que no existe en la DB de prueba
        Long idInexistente = 9999L;

        // WHEN: Busco la parada con ese ID
        Optional<Parada> resultado = repositorioParada.findByid(idInexistente);

        // THEN: El resultado debe ser Optional.empty()
        assertFalse(resultado.isPresent(), "No debería encontrar la parada con ID inexistente.");
        assertThat(resultado.isEmpty(), is(true));
    }
}