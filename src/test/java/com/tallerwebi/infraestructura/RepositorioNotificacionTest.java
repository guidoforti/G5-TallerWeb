package com.tallerwebi.infraestructura;

import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.config.SpringWebConfig;
import com.tallerwebi.dominio.Entity.Notificacion;
import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.Entity.Viajero; // Usamos Viajero, una clase concreta de Usuario
import com.tallerwebi.dominio.Enums.TipoNotificacion;
import com.tallerwebi.dominio.IRepository.RepositorioNotificacion;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test de Integración para RepositorioNotificacionImpl.
 * Verifica la persistencia y las consultas HQL/Criteria.
 * Se garantiza la existencia del destinatario (Viajero) directamente con SessionFactory.
 */
@ExtendWith(SpringExtension.class)
// Se asumen estas configuraciones para la carga del contexto y la base de datos de prueba
@ContextConfiguration(classes = {SpringWebConfig.class, HibernateTestConfig.class /*, DataBaseTestInitilizationConfig.class */})
@Transactional
@WebAppConfiguration
public class RepositorioNotificacionTest {

    @Autowired
    private SessionFactory sessionFactory;

    private RepositorioNotificacion repositorioNotificacion;

    // Este repositorio no es necesario inyectarlo, pero lo mantenemos para claridad si se necesita en otros tests.
    // private RepositorioUsuario repositorioUsuario;

    private Viajero usuarioDestinatario;

    @BeforeEach
    public void setUp() {
        this.repositorioNotificacion = new RepositorioNotificacionImpl(this.sessionFactory);

        // ARRANGE: Aseguramos que el usuario destinatario exista en la DB directamente con la sesión.
        usuarioDestinatario = new Viajero(); // Usamos Viajero, una subclase de Usuario
        usuarioDestinatario.setEmail("notificacion_test_a@viajero.com");
        usuarioDestinatario.setContrasenia("pass123");
        usuarioDestinatario.setNombre("Test User A");
        // Persistimos directamente con la sesión para garantizar que tiene un ID y la FK es válida.
        sessionFactory.getCurrentSession().save(usuarioDestinatario);

        sessionFactory.getCurrentSession().flush();
    }

    // --------------------------------------------------------------------------------
    // Tests: Guardar, Buscar por ID, Actualizar
    // --------------------------------------------------------------------------------

    @Test
    public void guardarYBuscarPorId_deberiaGuardarYEncontrarNotificacion() {
        // GIVEN: Una nueva notificación
        Notificacion notificacion = crearNotificacion("Tu asiento fue reservado.", usuarioDestinatario, false, LocalDateTime.now());

        // WHEN: Guardo la notificación
        repositorioNotificacion.guardar(notificacion);
        sessionFactory.getCurrentSession().flush();

        // THEN: Debería tener un ID y ser recuperable
        assertThat(notificacion.getId(), is(notNullValue()));
        Optional<Notificacion> resultado = repositorioNotificacion.buscarPorId(notificacion.getId());

        assertTrue(resultado.isPresent(), "Debería encontrar la notificación por ID");
        assertThat(resultado.get().getMensaje(), is("Tu asiento fue reservado."));
        assertThat(resultado.get().getLeida(), is(false));
    }

    @Test
    public void buscarPorId_deberiaRetornarOptionalVacioSiNoExiste() {
        // WHEN: Busco por un ID que no existe
        Optional<Notificacion> resultado = repositorioNotificacion.buscarPorId(9999L);

        // THEN: Debería retornar vacío
        assertFalse(resultado.isPresent(), "No debería encontrar la notificación con ID inexistente");
    }

    @Test
    public void actualizar_deberiaCambiarElEstadoDeLeida() {
        // GIVEN: Una notificación no leída guardada
        Notificacion notificacion = crearNotificacion("Mensaje a actualizar", usuarioDestinatario, false, LocalDateTime.now());
        repositorioNotificacion.guardar(notificacion);
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear(); // Limpiamos para asegurar que se lee de la DB

        // WHEN: La busco, cambio su estado a leída y la actualizo
        Optional<Notificacion> notiOpt = repositorioNotificacion.buscarPorId(notificacion.getId());
        assertTrue(notiOpt.isPresent());
        Notificacion notiEncontrada = notiOpt.get();

        assertFalse(notiEncontrada.getLeida());

        notiEncontrada.setLeida(true);
        repositorioNotificacion.actualizar(notiEncontrada);
        sessionFactory.getCurrentSession().flush();

        // THEN: La busco de nuevo y verifico que esté marcada como leída
        Optional<Notificacion> notiActualizadaOpt = repositorioNotificacion.buscarPorId(notificacion.getId());
        assertTrue(notiActualizadaOpt.isPresent());
        assertTrue(notiActualizadaOpt.get().getLeida(), "El estado 'leida' debería haber cambiado a true");
    }

    // --------------------------------------------------------------------------------
    // Tests: Consultas HQL/Criteria (buscarPorUsuario)
    // --------------------------------------------------------------------------------

    @Test
    public void buscarPorUsuario_deberiaDevolverNotificacionesOrdenadasYLimitadas() {
        // GIVEN: Tres notificaciones para el mismo usuario con distintas fechas
        Notificacion n1 = crearNotificacion("Mensaje 1 (Antiguo)", usuarioDestinatario, true, LocalDateTime.now().minusHours(3));
        Notificacion n2 = crearNotificacion("Mensaje 2 (Reciente)", usuarioDestinatario, false, LocalDateTime.now());
        Notificacion n3 = crearNotificacion("Mensaje 3 (Intermedio)", usuarioDestinatario, false, LocalDateTime.now().minusHours(1));

        repositorioNotificacion.guardar(n1);
        repositorioNotificacion.guardar(n2);
        repositorioNotificacion.guardar(n3);
        sessionFactory.getCurrentSession().flush();

        // WHEN: Busco las notificaciones con límite 2
        List<Notificacion> lista = repositorioNotificacion.buscarPorUsuario(usuarioDestinatario, 2);

        // THEN: La lista debería tener 2 elementos y estar ordenada por fechaCreacion DESC
        assertThat(lista, hasSize(2));
        // El más reciente primero
        assertThat(lista.get(0).getMensaje(), is("Mensaje 2 (Reciente)"));
        // El siguiente más reciente
        assertThat(lista.get(1).getMensaje(), is("Mensaje 3 (Intermedio)"));
    }

    @Test
    public void buscarPorUsuario_deberiaDevolverListaVaciaSiUsuarioNoTieneNotificaciones() {
        // GIVEN: Un segundo usuario sin notificaciones
        Viajero otroUsuario = new Viajero();
        otroUsuario.setEmail("otro_sin_notis@mail.com");
        otroUsuario.setContrasenia("otro");
        sessionFactory.getCurrentSession().save(otroUsuario);
        sessionFactory.getCurrentSession().flush();

        // WHEN: Busco las notificaciones para el otro usuario
        List<Notificacion> lista = repositorioNotificacion.buscarPorUsuario(otroUsuario, 5);

        // THEN: La lista debe ser vacía
        assertThat(lista, hasSize(0));
    }


    // --------------------------------------------------------------------------------
    // Tests: Consultas HQL/Criteria (contarNoLeidasPorUsuario)
    // --------------------------------------------------------------------------------

    @Test
    public void contarNoLeidasPorUsuario_deberiaContarSoloLasNotificacionesNoLeidasDelDestinatario() {
        // GIVEN: Varias notificaciones (leídas, no leídas y de otro usuario)
        crearYGuardarNotificacion("No Leída 1", usuarioDestinatario, false);
        crearYGuardarNotificacion("Leída 1", usuarioDestinatario, true);
        crearYGuardarNotificacion("No Leída 2", usuarioDestinatario, false);

        // Notificación de otro usuario (no debe contar)
        Viajero otroUsuario = new Viajero();
        otroUsuario.setEmail("otro_b@mail.com");
        otroUsuario.setContrasenia("otro");
        sessionFactory.getCurrentSession().save(otroUsuario); // Guardamos directamente
        crearYGuardarNotificacion("De otro usuario (No Leída)", otroUsuario, false);
        sessionFactory.getCurrentSession().flush();

        // WHEN: Cuento las no leídas para el usuario destinatario
        Long cantidadNoLeidas = repositorioNotificacion.contarNoLeidasPorUsuario(usuarioDestinatario);

        // THEN: La cuenta debe ser 2
        assertThat(cantidadNoLeidas, is(2L));
    }

    @Test
    public void contarNoLeidasPorUsuario_deberiaRetornarCeroSiNoHayNoLeidas() {
        // GIVEN: Solo notificaciones leídas
        crearYGuardarNotificacion("Leída 1", usuarioDestinatario, true);
        crearYGuardarNotificacion("Leída 2", usuarioDestinatario, true);
        sessionFactory.getCurrentSession().flush();

        // WHEN: Cuento las no leídas
        Long cantidadNoLeidas = repositorioNotificacion.contarNoLeidasPorUsuario(usuarioDestinatario);

        // THEN: La cuenta debe ser 0
        assertThat(cantidadNoLeidas, is(0L));
    }


    // --------------------------------------------------------------------------------
    // Métodos Auxiliares para el test
    // --------------------------------------------------------------------------------

    /**
     * Crea una instancia de Notificacion con valores básicos.
     */
    private Notificacion crearNotificacion(String mensaje, Usuario destinatario, Boolean leida, LocalDateTime fecha) {
        Notificacion notificacion = new Notificacion();
        notificacion.setMensaje(mensaje);
        notificacion.setFechaCreacion(fecha);
        notificacion.setTipo(TipoNotificacion.VALORACION_PENDIENTE);
        notificacion.setDestinatario(destinatario);
        notificacion.setLeida(leida);
        notificacion.setUrlDestino("/test/url/default");
        return notificacion;
    }

    /**
     * Crea y guarda una Notificacion usando el repositorio.
     */
    private void crearYGuardarNotificacion(String mensaje, Usuario destinatario, Boolean leida) {
        Notificacion notificacion = crearNotificacion(mensaje, destinatario, leida, LocalDateTime.now());
        repositorioNotificacion.guardar(notificacion);
    }
}