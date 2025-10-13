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
import org.springframework.transaction.annotation.Transactional; // Usar la de Spring

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

    private RepositorioViajeroImpl repositorioViajero;

    private Viajero viajeroBase;

    @BeforeEach
    public void setUp() {
        this.repositorioViajero = new RepositorioViajeroImpl(this.sessionFactory);

        // Persistir un viajero base para usar en las búsquedas
        viajeroBase = new Viajero(null, "ViajeroBase", 30, "base@mail.com", "securePass1", new ArrayList<>());
        repositorioViajero.guardarViajero(viajeroBase); // Hibernate asigna el ID aquí

        // Limpiar para asegurar que las búsquedas siguientes golpeen la DB
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();
    }

    @Test
    public void cuandoSeAgregaViajeroNuevoSeGuardaYRetornaId(){
        Viajero viajero = new Viajero(null, "Patricio", 20, "Patricio@gmail.com", "1234aa", new ArrayList<>());
        Viajero viajeroGuardado = this.repositorioViajero.guardarViajero(viajero);

        assertThat(viajeroGuardado, is(notNullValue()));
        assertThat(viajeroGuardado.getId(), is(notNullValue()));

        // Verificar persistencia: buscarlo por ID
        Optional<Viajero> encontrado = repositorioViajero.buscarPorId(viajeroGuardado.getId());
        assertTrue(encontrado.isPresent());
    }
    @Test
    public void buscoViajeroPorEmailYContraseniaYLoEncuentro(){
        Optional<Viajero> viajeroEncontrado = repositorioViajero.buscarPorEmailYContrasenia("base@mail.com", "securePass1");

        assertTrue(viajeroEncontrado.isPresent());
        assertThat(viajeroEncontrado.get().getNombre(), is("ViajeroBase"));
    }

    @Test
    public void siLaContraseniaEsIncorrectaNoDevuelveAlViajero(){
        Optional<Viajero> viajeroEncontrado = repositorioViajero.buscarPorEmailYContrasenia("base@mail.com", "kfsvc1_incorrecta");

        assertThat(viajeroEncontrado.isPresent(), is(false));
    }

    @Test
    public void buscoViajeroPorEmailYLoEncuentro(){

        Optional<Viajero> viajeroPorMailEncontrado = repositorioViajero.buscarPorEmail("base@mail.com");

        assertTrue(viajeroPorMailEncontrado.isPresent());
        assertThat(viajeroPorMailEncontrado.get().getEmail(), is("base@mail.com"));
    }

    @Test
    public void buscoViajeroPorEmailYNoLoEncuentro(){

        Optional<Viajero> viajeroPorMailEncontrado = repositorioViajero.buscarPorEmail("Juan@gmail.com");

        assertThat(viajeroPorMailEncontrado.isPresent(), is(false));
    }

    @Test
    public void buscoViajeroPorIdYLoEncuentro(){
        Long idExistente = viajeroBase.getId();


        Optional<Viajero> viajeroPorIDEncontrado = repositorioViajero.buscarPorId(idExistente);

        assertTrue(viajeroPorIDEncontrado.isPresent());
        assertThat(viajeroPorIDEncontrado.get().getId(), is(idExistente));
        assertThat(viajeroPorIDEncontrado.get().getEmail(), is("base@mail.com"));
    }

    @Test
    public void buscoViajeroPorIdYNoLoEncuentro(){

        Optional<Viajero> viajeroPorIDEncontrado = repositorioViajero.buscarPorId(999L);

        assertThat(viajeroPorIDEncontrado.isPresent(), is(false));
    }
}
