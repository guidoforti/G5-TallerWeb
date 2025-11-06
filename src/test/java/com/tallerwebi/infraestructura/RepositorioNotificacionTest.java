package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Notificacion;
import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.IRepository.RepositorioNotificacion;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

// NOTA: Asumo que esta clase está marcada con las anotaciones de Spring Test para la inyección (ej., @ExtendWith, @ContextConfiguration)
@Transactional @Rollback
public class RepositorioNotificacionTest {

    @Autowired
    private SessionFactory sessionFactory;

    private RepositorioNotificacion repositorioNotificacion;
    private Conductor usuarioDestino;

    @BeforeEach
    public void setUp() {
        // Inicializar el repositorio con la sessionFactory inyectada
        repositorioNotificacion = new RepositorioNotificacionImpl(sessionFactory);

        Session session = sessionFactory.getCurrentSession();

        // 1. Crear el usuario concreto (Conductor)
        usuarioDestino = new Conductor();
        usuarioDestino.setNombre("Conductor Test");
        usuarioDestino.setEmail("test@conductor.com");
        usuarioDestino.setContrasenia("hashed");
        usuarioDestino.setRol("CONDUCTOR");

        // 2. Persistir la entidad base para obtener el ID
        session.save(usuarioDestino);

        // [🟢 CORRECCIÓN CLAVE] Forzar la sincronización. Esto garantiza que el ID
        // (que es autogenerado) se obtenga antes de ser usado en las FKs de las Notificaciones.
        session.flush();
    }

    /**
     * Helper para crear y guardar una notificación.
     * Llama al save del repositorio que usa el sessionFactory.
     */
    private Notificacion crearYPersistirNotificacion(Usuario usuario, String mensaje, boolean vista) {
        Notificacion n = new Notificacion();
        n.setUsuario(usuario);
        n.setMensaje(mensaje);
        n.setFechaCreacion(LocalDateTime.now());
        n.setVista(vista);
        // Persistir la notificación (el ID del usuario ya está disponible)
        repositorioNotificacion.save(n);
        return n;
    }

    @Test
    public void findByUsuarioIdAndVistaFalse_deberiaDevolverSoloNotificacionesNoVistasParaElUsuario() {
        // GIVEN: Creamos notificaciones
        // Las notificaciones usan usuarioDestino, que ahora tiene un ID generado.
        crearYPersistirNotificacion(usuarioDestino, "Notificacion Pendiente 1", false);
        crearYPersistirNotificacion(usuarioDestino, "Notificacion Pendiente 2", false);
        crearYPersistirNotificacion(usuarioDestino, "Ya Vista", true);

        // WHEN
        // Llamamos al repositorio usando el ID generado y válido.
        List<Notificacion> resultado = repositorioNotificacion.findByUsuarioIdAndVistaFalse(usuarioDestino.getId());

        // THEN
        // Debe haber exactamente dos resultados (las no vistas)
        assertThat(resultado, hasSize(2));
        // El resultado debe contener el mensaje esperado
        assertThat(resultado.get(0).getMensaje(), containsString("Pendiente"));
    }

    @Test
    public void findByUsuarioIdAndVistaFalse_sinNotificacionesPendientes_deberiaDevolverListaVacia() {
        // GIVEN: Creamos una notificación vista (no debe ser devuelta)
        crearYPersistirNotificacion(usuarioDestino, "Ya fue vista", true);

        // WHEN
        List<Notificacion> resultado = repositorioNotificacion.findByUsuarioIdAndVistaFalse(usuarioDestino.getId());

        // THEN
        // El resultado debe ser una lista vacía
        assertThat(resultado, is(empty()));
    }
}