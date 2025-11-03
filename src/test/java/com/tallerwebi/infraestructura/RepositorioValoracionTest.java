package com.tallerwebi.infraestructura;


import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.Entity.Valoracion;
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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HibernateTestConfig.class, DataBaseTestInitilizationConfig.class})
@Transactional
public class RepositorioValoracionTest {

    @Autowired
    private SessionFactory sessionFactory;

    private RepositorioValoracion repositorioValoracion;

    @BeforeEach
    void setUp() {
        // Inicializa el repositorio con la SessionFactory inyectada
        this.repositorioValoracion = new RepositorioValoracionImpl(this.sessionFactory);
    }

    // --- TEST PARA: save(Valoracion valoracion) ---

    @Test
    public void deberiaGuardarValoracionCorrectamente() {
        // given
        // Asumo que el usuario con ID 1 existe en tu dataTest.sql
        Usuario emisor = sessionFactory.getCurrentSession().get(Usuario.class, 1L);
        Usuario receptor = sessionFactory.getCurrentSession().get(Usuario.class, 2L);

        Valoracion valoracion = new Valoracion(emisor, receptor, 5, "Excelente experiencia, muy puntual.");

        // when
        repositorioValoracion.save(valoracion);
        sessionFactory.getCurrentSession().flush(); // Forzar la escritura a la base de datos

        // then
        assertNotNull(valoracion.getId(), "La valoración guardada debería tener un ID asignado");
        
        // Verificar que se puede recuperar directamente
        Valoracion valoracionRecuperada = sessionFactory.getCurrentSession().get(Valoracion.class, valoracion.getId());
        assertNotNull(valoracionRecuperada, "La valoración debe ser recuperable");
        assertThat(valoracionRecuperada.getPuntuacion(), is(5));
        assertThat(valoracionRecuperada.getReceptor().getId(), is(receptor.getId()));
    }

    // --- TEST PARA: findByReceptorId(Long receptorId) ---

    @Test
    public void deberiaObtenerValoracionesDeUsuarioOrdenadasPorFechaDesc() {
        // given
        // IDs: 1 (Emisor), 2 (Receptor Objetivo), 3 (Otro Emisor)
        Usuario emisor1 = sessionFactory.getCurrentSession().get(Usuario.class, 1L);
        Usuario receptor = sessionFactory.getCurrentSession().get(Usuario.class, 2L);
        Usuario emisor2 = sessionFactory.getCurrentSession().get(Usuario.class, 3L);

        // 1. Valoración Antigua (Fecha -2 días)
        Valoracion valoracionAntigua = new Valoracion(emisor1, receptor, 3, "Regular");
        valoracionAntigua.setFecha(LocalDate.now().minusDays(2));

        // 2. Valoración Reciente (Fecha -1 día)
        Valoracion valoracionReciente = new Valoracion(emisor2, receptor, 5, "Genial!");
        valoracionReciente.setFecha(LocalDate.now().minusDays(1));

        // 3. Valoración Hoy (Más Reciente)
        Valoracion valoracionHoy = new Valoracion(emisor1, receptor, 4, "Buena");
        valoracionHoy.setFecha(LocalDate.now());

        // Guardar en orden desordenado para probar el ORDER BY del repositorio
        repositorioValoracion.save(valoracionAntigua);
        repositorioValoracion.save(valoracionHoy);
        repositorioValoracion.save(valoracionReciente);
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear(); // Limpiar caché para asegurar que se consulta la BD

        // when
        List<Valoracion> valoraciones = repositorioValoracion.findByReceptorId(receptor.getId());

        // then
        assertThat(valoraciones, hasSize(3));
        
        // Verificar el orden (DESC por fecha: Hoy > Reciente > Antigua)
        assertThat(valoraciones.get(0).getComentario(), is("Buena"));   // Hoy
        assertThat(valoraciones.get(1).getComentario(), is("Genial!")); // Reciente
        assertThat(valoraciones.get(2).getComentario(), is("Regular")); // Antigua
    }

    @Test
    public void deberiaRetornarListaVaciaCuandoUsuarioNoTieneValoraciones() {
        // given
        // Asumo que el usuario con ID 99 no existe o no tiene valoraciones
        Long idUsuarioSinValoraciones = 99L;

        // when
        List<Valoracion> valoraciones = repositorioValoracion.findByReceptorId(idUsuarioSinValoraciones);

        // then
        assertNotNull(valoraciones, "Debe retornar una lista, no nulo");
        assertTrue(valoraciones.isEmpty(), "La lista de valoraciones debería estar vacía");
        assertThat(valoraciones, hasSize(0));
    }
}