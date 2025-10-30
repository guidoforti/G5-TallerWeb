package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.IRepository.RepositorioViajero;
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

import java.util.ArrayList;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateTestConfig.class, DataBaseTestInitilizationConfig.class })
@Transactional
public class RepositorioViajeroTest {

    @Autowired
    private SessionFactory sessionFactory;

    private RepositorioViajero repositorioViajero;
    private RepositorioUsuarioImpl repositorioUsuario;

    private Viajero viajeroBase;

    @BeforeEach
    public void setUp() {
        this.repositorioViajero = new RepositorioViajeroImpl(this.sessionFactory);
        this.repositorioUsuario = new RepositorioUsuarioImpl(this.sessionFactory);

        viajeroBase = new Viajero();
        viajeroBase.setNombre("ViajeroBaseTest");
        viajeroBase.setEdad(30);
        viajeroBase.setEmail("viajero.base.test@unlam.com");
        viajeroBase.setContrasenia("securePass1");
        viajeroBase.setRol("VIAJERO");
        viajeroBase.setActivo(true);
        viajeroBase.setReservas(new ArrayList<>());

        repositorioUsuario.guardar(viajeroBase);
    }


    @Test
    public void buscoViajeroPorIdYLoEncuentro(){
        Long idExistente = viajeroBase.getId();

        Optional<Viajero> viajeroPorIDEncontrado = repositorioViajero.buscarPorId(idExistente);

        assertTrue(viajeroPorIDEncontrado.isPresent());
        assertThat(viajeroPorIDEncontrado.get().getId(), is(idExistente));
        assertThat(viajeroPorIDEncontrado.get().getEmail(), is("viajero.base.test@unlam.com"));
    }

    @Test
    public void buscoViajeroPorIdYNoLoEncuentro(){
        Optional<Viajero> viajeroPorIDEncontrado = repositorioViajero.buscarPorId(999L);

        assertThat(viajeroPorIDEncontrado.isPresent(), is(false));
    }
}