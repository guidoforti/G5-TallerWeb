package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Entity.ViolacionConductor;
import com.tallerwebi.dominio.Enums.TipoViolacion;
import com.tallerwebi.dominio.IRepository.RepositorioViolacion;
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

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HibernateTestConfig.class, DataBaseTestInitilizationConfig.class})
@Transactional
public class RepositorioViolacionTest {

    @Autowired
    private SessionFactory sessionFactory;

    private RepositorioViolacion repositorioViolacion;

    @BeforeEach
    void setUp() {
        this.repositorioViolacion = new RepositorioViolacionImpl(this.sessionFactory);
    }

    @Test
    public void deberiaGuardarViolacionCorrectamente() {
        // given
        Conductor conductor = sessionFactory.getCurrentSession().get(Conductor.class, 1L);
        Viaje viaje = sessionFactory.getCurrentSession().get(Viaje.class, 1L);

        ViolacionConductor violacion = new ViolacionConductor();
        violacion.setConductor(conductor);
        violacion.setViaje(viaje);
        violacion.setTipo(TipoViolacion.RETRASO_LEVE);
        violacion.setFechaViolacion(LocalDateTime.now());
        violacion.setMinutosRetraso(12);
        violacion.setActiva(true);
        violacion.setFechaExpiracion(LocalDateTime.now().plusDays(30));
        violacion.setDescripcion("Inicio con 12 minutos de retraso");

        // when
        ViolacionConductor violacionGuardada = repositorioViolacion.guardar(violacion);

        // then
        assertThat(violacionGuardada.getId(), is(notNullValue()));
        assertThat(violacionGuardada.getTipo(), is(TipoViolacion.RETRASO_LEVE));
        assertThat(violacionGuardada.getActiva(), is(true));
    }

    @Test
    public void deberiaBuscarViolacionPorId() {
        // given
        Conductor conductor = sessionFactory.getCurrentSession().get(Conductor.class, 1L);
        ViolacionConductor violacion = crearViolacion(conductor, TipoViolacion.RETRASO_GRAVE);
        ViolacionConductor guardada = repositorioViolacion.guardar(violacion);

        // when
        Optional<ViolacionConductor> encontrada = repositorioViolacion.buscarPorId(guardada.getId());

        // then
        assertThat(encontrada.isPresent(), is(true));
        assertThat(encontrada.get().getTipo(), is(TipoViolacion.RETRASO_GRAVE));
    }

    @Test
    public void deberiaBuscarViolacionesActivasPorConductor() {
        // given
        Conductor conductor = sessionFactory.getCurrentSession().get(Conductor.class, 1L);

        // Crear 2 violaciones activas
        ViolacionConductor violacion1 = crearViolacion(conductor, TipoViolacion.RETRASO_LEVE);
        violacion1.setActiva(true);
        repositorioViolacion.guardar(violacion1);

        ViolacionConductor violacion2 = crearViolacion(conductor, TipoViolacion.OLVIDO_CIERRE);
        violacion2.setActiva(true);
        repositorioViolacion.guardar(violacion2);

        // Crear 1 violación inactiva
        ViolacionConductor violacion3 = crearViolacion(conductor, TipoViolacion.NO_SHOW);
        violacion3.setActiva(false);
        repositorioViolacion.guardar(violacion3);

        // when
        List<ViolacionConductor> violacionesActivas =
            repositorioViolacion.buscarPorConductorYActivaTrue(conductor);

        // then
        assertThat(violacionesActivas, hasSize(2));
        assertThat(violacionesActivas.get(0).getActiva(), is(true));
        assertThat(violacionesActivas.get(1).getActiva(), is(true));
    }

    @Test
    public void deberiaBuscarViolacionesExpiradasPorFecha() {
        // given
        Conductor conductor = sessionFactory.getCurrentSession().get(Conductor.class, 1L);
        LocalDateTime fechaLimite = LocalDateTime.now();

        // Violación expirada (fecha expiración en el pasado)
        ViolacionConductor violacionExpirada = crearViolacion(conductor, TipoViolacion.RETRASO_LEVE);
        violacionExpirada.setActiva(true);
        violacionExpirada.setFechaExpiracion(fechaLimite.minusDays(1));
        repositorioViolacion.guardar(violacionExpirada);

        // Violación aún vigente
        ViolacionConductor violacionVigente = crearViolacion(conductor, TipoViolacion.OLVIDO_CIERRE);
        violacionVigente.setActiva(true);
        violacionVigente.setFechaExpiracion(fechaLimite.plusDays(10));
        repositorioViolacion.guardar(violacionVigente);

        // when
        List<ViolacionConductor> expiradas =
            repositorioViolacion.buscarPorActivaTrueYFechaExpiracionAnteriorA(fechaLimite);

        // then
        assertThat(expiradas, hasSize(1));
        assertThat(expiradas.get(0).getFechaExpiracion(), is(lessThan(fechaLimite)));
    }

    @Test
    public void deberiaContarViolacionesPorTipo() {
        // given
        Conductor conductor = sessionFactory.getCurrentSession().get(Conductor.class, 1L);

        // Crear 3 violaciones de RETRASO_LEVE activas
        for (int i = 0; i < 3; i++) {
            ViolacionConductor violacion = crearViolacion(conductor, TipoViolacion.RETRASO_LEVE);
            violacion.setActiva(true);
            repositorioViolacion.guardar(violacion);
        }

        // Crear 1 violación de RETRASO_LEVE inactiva
        ViolacionConductor violacionInactiva = crearViolacion(conductor, TipoViolacion.RETRASO_LEVE);
        violacionInactiva.setActiva(false);
        repositorioViolacion.guardar(violacionInactiva);

        // when
        int count = repositorioViolacion.contarPorConductorYTipoYActivaTrue(
            conductor,
            TipoViolacion.RETRASO_LEVE
        );

        // then
        assertThat(count, is(3));
    }

    @Test
    public void deberiaBuscarViolacionesOrdenadasPorFecha() {
        // given
        Conductor conductor = sessionFactory.getCurrentSession().get(Conductor.class, 1L);

        ViolacionConductor violacion1 = crearViolacion(conductor, TipoViolacion.RETRASO_LEVE);
        violacion1.setFechaViolacion(LocalDateTime.now().minusDays(3));
        repositorioViolacion.guardar(violacion1);

        ViolacionConductor violacion2 = crearViolacion(conductor, TipoViolacion.RETRASO_GRAVE);
        violacion2.setFechaViolacion(LocalDateTime.now().minusDays(1));
        repositorioViolacion.guardar(violacion2);

        ViolacionConductor violacion3 = crearViolacion(conductor, TipoViolacion.OLVIDO_CIERRE);
        violacion3.setFechaViolacion(LocalDateTime.now().minusDays(5));
        repositorioViolacion.guardar(violacion3);

        // when
        List<ViolacionConductor> violaciones =
            repositorioViolacion.buscarPorConductorOrderByFechaViolacionDesc(conductor);

        // then
        assertThat(violaciones, hasSize(3));
        // Debe estar ordenado de más reciente a más antigua
        assertThat(violaciones.get(0).getTipo(), is(TipoViolacion.RETRASO_GRAVE)); // -1 day
        assertThat(violaciones.get(1).getTipo(), is(TipoViolacion.RETRASO_LEVE));  // -3 days
        assertThat(violaciones.get(2).getTipo(), is(TipoViolacion.OLVIDO_CIERRE)); // -5 days
    }

    @Test
    public void deberiaActualizarViolacion() {
        // given
        Conductor conductor = sessionFactory.getCurrentSession().get(Conductor.class, 1L);
        ViolacionConductor violacion = crearViolacion(conductor, TipoViolacion.RETRASO_LEVE);
        violacion.setActiva(true);
        ViolacionConductor guardada = repositorioViolacion.guardar(violacion);

        // when
        guardada.setActiva(false);
        repositorioViolacion.actualizar(guardada);
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();

        // then
        Optional<ViolacionConductor> actualizada = repositorioViolacion.buscarPorId(guardada.getId());
        assertThat(actualizada.isPresent(), is(true));
        assertThat(actualizada.get().getActiva(), is(false));
    }

    // Helper method
    private ViolacionConductor crearViolacion(Conductor conductor, TipoViolacion tipo) {
        ViolacionConductor violacion = new ViolacionConductor();
        violacion.setConductor(conductor);
        violacion.setTipo(tipo);
        violacion.setFechaViolacion(LocalDateTime.now());
        violacion.setActiva(true);
        violacion.setFechaExpiracion(LocalDateTime.now().plusDays(30));
        violacion.setDescripcion("Violación de prueba");
        return violacion;
    }
}
