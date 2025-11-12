package com.tallerwebi.infraestructura;

import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.DataBaseTestInitilizationConfig;
import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Valoracion;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.IRepository.RepositorioValoracion;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test de Integración para RepositorioValoracionImpl.
 * Depende de los datos pre-cargados en dataTest.sql (Usuarios, Viajes).
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HibernateTestConfig.class, DataBaseTestInitilizationConfig.class})
@Transactional
public class RepositorioValoracionTest {

    @Autowired
    private SessionFactory sessionFactory;

    private RepositorioValoracion repositorioValoracion;

    // Entidades de referencia de dataTest.sql
    private final Long EMISOR_ID = 4L; // Viajero Sofia Torres (ID 4 en dataTest.sql)
    private final Long RECEPTOR_ID = 1L; // Conductor Carlos Perez (ID 1 en dataTest.sql)
    private final Long VIAJE_ID_FINALIZADO = 3L; // Viaje 3 (FINALIZADO en dataTest.sql)
    private Viajero viajeroEmisor;
    private Conductor conductorReceptor;
    private Viaje viajeFinalizado;

    @BeforeEach
    public void setUp() {
        this.repositorioValoracion = new RepositorioValoracionImpl(this.sessionFactory);

        // ARRANGE: Cargar las entidades necesarias desde la sesión (que ya fueron insertadas por dataTest.sql)
        viajeroEmisor = sessionFactory.getCurrentSession().get(Viajero.class, EMISOR_ID);
        conductorReceptor = sessionFactory.getCurrentSession().get(Conductor.class, RECEPTOR_ID);
        viajeFinalizado = sessionFactory.getCurrentSession().get(Viaje.class, VIAJE_ID_FINALIZADO);
    }

    // --------------------------------------------------------------------------------
    // Tests: save(Valoracion)
    // --------------------------------------------------------------------------------

    @Test
    public void save_deberiaGuardarValoracionCorrectamente() {
        // GIVEN: Una nueva valoración
        Valoracion valoracion = new Valoracion();
        valoracion.setEmisor(viajeroEmisor);
        valoracion.setReceptor(conductorReceptor);
        valoracion.setViaje(viajeFinalizado);
        valoracion.setPuntuacion(5);
        valoracion.setComentario("Excelente viaje");
        valoracion.setFecha(LocalDate.now());

        // WHEN: Guardo la valoración
        repositorioValoracion.save(valoracion);
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        // THEN: La valoración debe tener un ID asignado y ser recuperable
        assertThat(valoracion.getId(), is(notNullValue()));

        // Verifico que se pueda recuperar directamente de la sesión
        Valoracion recuperada = sessionFactory.getCurrentSession().get(Valoracion.class, valoracion.getId());
        assertThat(recuperada, is(notNullValue()));
        assertThat(recuperada.getPuntuacion(), is(5));
        assertThat(recuperada.getEmisor().getId(), is(EMISOR_ID));
    }

    // --------------------------------------------------------------------------------
    // Tests: findByReceptorId(Long receptorId)
    // --------------------------------------------------------------------------------

    @Test
    public void findByReceptorId_deberiaRetornarValoracionesOrdenadasPorFechaDesc() {
        // GIVEN: Varias valoraciones para el mismo receptor con diferentes fechas
        // Nota: Usamos un Viajero temporal como emisor, no importa que no sea el mismo de dataTest
        Viajero emisor2 = new Viajero();
        emisor2.setId(90L);
        emisor2.setEmail("emisor2@test.com");
        sessionFactory.getCurrentSession().save(emisor2); // Persistimos para la FK

        // Valoración 1 (Antigua)
        Valoracion v1 = crearYGuardarValoracion(emisor2, conductorReceptor, viajeFinalizado, 4, "Viejo", LocalDate.now().minusDays(2));
        // Valoración 2 (Reciente)
        Valoracion v2 = crearYGuardarValoracion(viajeroEmisor, conductorReceptor, viajeFinalizado, 5, "Nuevo", LocalDate.now());
        // Valoración 3 (Intermedia)
        Valoracion v3 = crearYGuardarValoracion(emisor2, conductorReceptor, viajeFinalizado, 3, "Intermedio", LocalDate.now().minusDays(1));

        sessionFactory.getCurrentSession().flush();

        // WHEN: Busco las valoraciones por el ID del conductor receptor
        List<Valoracion> lista = repositorioValoracion.findByReceptorId(RECEPTOR_ID);

        // THEN: La lista debe tener 3 elementos y estar ordenada por fecha DESC (V2 -> V3 -> V1)
        assertThat(lista, hasSize(3));
        assertThat(lista.get(0).getComentario(), is("Nuevo")); // Más reciente (V2)
        assertThat(lista.get(1).getComentario(), is("Intermedio")); // Intermedio (V3)
        assertThat(lista.get(2).getComentario(), is("Viejo")); // Más antiguo (V1)
    }

    @Test
    public void findByReceptorId_deberiaRetornarListaVaciaSiNoHayValoraciones() {
        // GIVEN: Un nuevo conductor que no tiene valoraciones
        Conductor conductorNuevo = new Conductor();
        conductorNuevo.setId(99L);
        conductorNuevo.setEmail("nuevo@conductor.com");
        sessionFactory.getCurrentSession().save(conductorNuevo);
        sessionFactory.getCurrentSession().flush();

        // WHEN: Busco valoraciones para el nuevo conductor
        List<Valoracion> lista = repositorioValoracion.findByReceptorId(99L);

        // THEN: La lista debe estar vacía
        assertThat(lista, is(empty()));
    }

    // --------------------------------------------------------------------------------
    // Tests: yaExisteValoracionParaViaje(Long emisorId, Long receptorId, Long viajeId)
    // --------------------------------------------------------------------------------

    @Test
    public void yaExisteValoracionParaViaje_deberiaRetornarTrueCuandoLaValoracionExiste() {
        // GIVEN: Una valoración guardada con los 3 IDs clave
        crearYGuardarValoracion(viajeroEmisor, conductorReceptor, viajeFinalizado, 5, "Test Unicidad", LocalDate.now());
        sessionFactory.getCurrentSession().flush();

        // WHEN: Verifico si existe la valoración con la misma combinación
        boolean existe = repositorioValoracion.yaExisteValoracionParaViaje(EMISOR_ID, RECEPTOR_ID, VIAJE_ID_FINALIZADO);

        // THEN: Debe retornar verdadero
        assertTrue(existe, "Debería retornar TRUE porque la valoración ya fue guardada");
    }

    @Test
    public void yaExisteValoracionParaViaje_deberiaRetornarFalseCuandoLaValoracionNoExiste() {
        // GIVEN: Ninguna valoración guardada

        // WHEN: Verifico si existe una valoración que no ha sido creada
        boolean existe = repositorioValoracion.yaExisteValoracionParaViaje(EMISOR_ID, RECEPTOR_ID, VIAJE_ID_FINALIZADO);

        // THEN: Debe retornar falso
        assertFalse(existe, "Debería retornar FALSE porque no hay valoraciones para esa combinación de IDs");
    }

    @Test
    public void yaExisteValoracionParaViaje_deberiaRetornarFalseCuandoCambiaElViaje() {
        // GIVEN: Una valoración guardada para el Viaje 5
        crearYGuardarValoracion(viajeroEmisor, conductorReceptor, viajeFinalizado, 5, "Test Viaje 5", LocalDate.now());
        sessionFactory.getCurrentSession().flush();

        // WHEN: Verifico si existe la valoración para la misma combinación de Emisor/Receptor, pero con el Viaje 1
        boolean existe = repositorioValoracion.yaExisteValoracionParaViaje(EMISOR_ID, RECEPTOR_ID, 1L); // Viaje 1

        // THEN: Debe retornar falso
        assertFalse(existe, "Debería retornar FALSE porque el Viaje ID es diferente");
    }

    // --------------------------------------------------------------------------------
    // Métodos Auxiliares
    // --------------------------------------------------------------------------------

    private Valoracion crearYGuardarValoracion(Viajero emisor, Conductor receptor, Viaje viaje, Integer puntuacion, String comentario, LocalDate fecha) {
        Valoracion valoracion = new Valoracion();
        valoracion.setEmisor(emisor);
        valoracion.setReceptor(receptor);
        valoracion.setViaje(viaje);
        valoracion.setPuntuacion(puntuacion);
        valoracion.setComentario(comentario);
        valoracion.setFecha(fecha);
        repositorioValoracion.save(valoracion);
        return valoracion;
    }
}