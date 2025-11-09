package com.tallerwebi.infraestructura;


import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.Entity.Valoracion;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Enums.EstadoDeViaje;
import com.tallerwebi.dominio.IRepository.RepositorioValoracion;
import com.tallerwebi.integracion.config.DataBaseTestInitilizationConfig;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HibernateTestConfig.class, DataBaseTestInitilizationConfig.class})
@Transactional
public class RepositorioValoracionTest {

    @Autowired
    private SessionFactory sessionFactory;

    private RepositorioValoracion repositorioValoracion;
    private Viaje viajeDummy;

    // --- HELPERS ---

    private Viaje crearViajeFinalizado(Long id) {
        Viaje viaje = new Viaje();
        viaje.setId(id);
        viaje.setEstado(EstadoDeViaje.FINALIZADO);
        sessionFactory.getCurrentSession().save(viaje); // Persistir el viaje dummy
        return viaje;
    }

    private Usuario obtenerUsuario(Long id) {
        return sessionFactory.getCurrentSession().get(Usuario.class, id);
    }

    @BeforeEach
    void setUp() {
        this.repositorioValoracion = new RepositorioValoracionImpl(this.sessionFactory);

        // Crear un viaje base para usar en todos los constructores de Valoracion
        this.viajeDummy = crearViajeFinalizado(99L);
    }

    // --- TEST PARA: save(Valoracion valoracion) ---

    @Test
    public void deberiaGuardarValoracionCorrectamente() {
        // given
        Usuario emisor = obtenerUsuario(1L);
        Usuario receptor = obtenerUsuario(2L);

        // CLAVE: Añadir el viajeDummy al constructor
        Valoracion valoracion = new Valoracion(emisor, receptor, 5, "Excelente experiencia, muy puntual.", viajeDummy);

        // when
        repositorioValoracion.save(valoracion);
        sessionFactory.getCurrentSession().flush();

        // then
        assertNotNull(valoracion.getId(), "La valoración guardada debería tener un ID asignado");

        Valoracion valoracionRecuperada = sessionFactory.getCurrentSession().get(Valoracion.class, valoracion.getId());
        assertNotNull(valoracionRecuperada, "La valoración debe ser recuperable");
        assertThat(valoracionRecuperada.getPuntuacion(), is(5));
        assertThat(valoracionRecuperada.getReceptor().getId(), is(receptor.getId()));
        assertThat(valoracionRecuperada.getViaje().getId(), is(viajeDummy.getId()));
    }

    // --- TEST PARA: findByReceptorId(Long receptorId) ---

    @Test
    public void deberiaObtenerValoracionesDeUsuarioOrdenadasPorFechaDesc() {
        // given
        Usuario emisor1 = obtenerUsuario(1L);
        Usuario receptor = obtenerUsuario(2L);
        Usuario emisor2 = obtenerUsuario(3L);

        Viaje viaje1 = crearViajeFinalizado(10L);
        Viaje viaje2 = crearViajeFinalizado(20L);

        // 1. Valoración Antigua (Fecha -2 días)
        Valoracion valoracionAntigua = new Valoracion(emisor1, receptor, 3, "Regular", viajeDummy);
        valoracionAntigua.setFecha(LocalDate.now().minusDays(2));

        // 2. Valoración Reciente (Fecha -1 día)
        Valoracion valoracionReciente = new Valoracion(emisor2, receptor, 5, "Genial!", viaje1);
        valoracionReciente.setFecha(LocalDate.now().minusDays(1));

        // 3. Valoración Hoy (Más Reciente)
        Valoracion valoracionHoy = new Valoracion(emisor1, receptor, 4, "Buena", viaje2);
        valoracionHoy.setFecha(LocalDate.now());

        // Guardar para probar el ORDER BY
        repositorioValoracion.save(valoracionAntigua);
        repositorioValoracion.save(valoracionHoy);
        repositorioValoracion.save(valoracionReciente);
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        // when
        List<Valoracion> valoraciones = repositorioValoracion.findByReceptorId(receptor.getId());

        // then
        assertThat(valoraciones, hasSize(3));

        // Verificar el orden (DESC por fecha: Hoy > Reciente > Antigua)
        assertThat(valoraciones.get(0).getComentario(), is("Buena"));
        assertThat(valoraciones.get(1).getComentario(), is("Genial!"));
        assertThat(valoraciones.get(2).getComentario(), is("Regular"));
    }

    @Test
    public void deberiaRetornarListaVaciaCuandoUsuarioNoTieneValoraciones() {
        // given
        Long idUsuarioSinValoraciones = 99L;

        // when
        List<Valoracion> valoraciones = repositorioValoracion.findByReceptorId(idUsuarioSinValoraciones);

        // then
        assertNotNull(valoraciones);
        assertTrue(valoraciones.isEmpty());
        assertThat(valoraciones, hasSize(0));
    }

    // --- NUEVO TEST PARA: yaExisteValoracionParaViaje(emisorId, receptorId, viajeId) ---

    @Test
    public void deberiaRetornarTrueSiYaExisteValoracionParaElViajeEspecifico() {
        // given
        Long emisorId = 1L;
        Long receptorId = 2L;
        Long viajeObjetivoId = 50L;

        Usuario emisor = obtenerUsuario(emisorId);
        Usuario receptor = obtenerUsuario(receptorId);
        Viaje viajeObjetivo = crearViajeFinalizado(viajeObjetivoId);

        // Guardar la valoración que estamos buscando
        Valoracion valoracionExistente = new Valoracion(emisor, receptor, 5, "Ya calificado.", viajeObjetivo);
        repositorioValoracion.save(valoracionExistente);

        // Guardar otra valoración para otro viaje (para asegurar que la consulta es precisa)
        Viaje otroViaje = crearViajeFinalizado(51L);
        Valoracion otraValoracion = new Valoracion(emisor, receptor, 3, "Otro viaje.", otroViaje);
        repositorioValoracion.save(otraValoracion);

        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        // when
        boolean existe = repositorioValoracion.yaExisteValoracionParaViaje(emisorId, receptorId, viajeObjetivoId);

        // then
        assertTrue(existe, "Debería retornar TRUE porque la valoración para este Viaje 50 ya existe.");
    }

    @Test
    public void deberiaRetornarFalseSiNoExisteValoracionParaElViajeEspecifico() {
        // given
        Long emisorId = 1L;
        Long receptorId = 2L;
        Long viajeObjetivoId = 60L;

        Usuario emisor = obtenerUsuario(emisorId);
        Usuario receptor = obtenerUsuario(receptorId);

        // Solo guardamos una valoración para otro viaje, no para el objetivo (60)
        Viaje otroViaje = crearViajeFinalizado(61L);
        Valoracion otraValoracion = new Valoracion(emisor, receptor, 3, "Para viaje 61.", otroViaje);
        repositorioValoracion.save(otraValoracion);

        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        // when
        boolean existe = repositorioValoracion.yaExisteValoracionParaViaje(emisorId, receptorId, viajeObjetivoId);

        // then
        assertTrue(!existe, "Debería retornar FALSE porque la valoración para el Viaje 60 no existe.");
    }
}