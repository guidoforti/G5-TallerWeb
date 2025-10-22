package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Entity.Viajero;
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

    private RepositorioViajeroImpl repositorioViajero;
    private Viajero viajeroBase; // Necesaria para guardar el ID

    @BeforeEach
    public void setUp() {
        this.repositorioViajero = new RepositorioViajeroImpl(this.sessionFactory);

        // Creación del Viajero base refactorizada
        viajeroBase = new Viajero();
        viajeroBase.setNombre("ViajeroBaseTest");
        viajeroBase.setEdad(30);
        viajeroBase.setEmail("viajero.base.test@unlam.com");
        viajeroBase.setContrasenia("securePass1");
        viajeroBase.setRol("VIAJERO");
        viajeroBase.setActivo(true);
        viajeroBase.setViajes(new ArrayList<>());
        repositorioViajero.guardarViajero(viajeroBase);
    }

    @Test
    public void cuandoSeAgregaViajeroNuevoSeGuardaYRetornaId(){
        // Arrange
        // Creación del nuevo Viajero refactorizada
        Viajero viajero = new Viajero();
        viajero.setNombre("Patricio Nuevo");
        viajero.setEdad(20);
        viajero.setEmail("patricio.nuevo@test.com");
        viajero.setContrasenia("1234aa");
        viajero.setRol("VIAJERO");
        viajero.setActivo(true);
        viajero.setViajes(new ArrayList<>());

        // Act
        Viajero viajeroGuardado = this.repositorioViajero.guardarViajero(viajero);

        // Assert
        assertThat(viajeroGuardado, is(notNullValue()));
        assertThat(viajeroGuardado.getId(), is(notNullValue()));

        Optional<Viajero> encontrado = repositorioViajero.buscarPorId(viajeroGuardado.getId());
        assertTrue(encontrado.isPresent());
    }

    @Test
    public void buscoViajeroPorEmailYContraseniaYLoEncuentro(){
        // Arrange: Credenciales insertadas en el setUp
        String email = "viajero.base.test@unlam.com";
        String pass = "securePass1";

        // Act
        Optional<Viajero> viajeroEncontrado = repositorioViajero.buscarPorEmailYContrasenia(email, pass);

        // Assert
        assertTrue(viajeroEncontrado.isPresent());
        assertThat(viajeroEncontrado.get().getEmail(), is(email));
    }

    @Test
    public void siLaContraseniaEsIncorrectaNoDevuelveAlViajero(){
        // Act
        Optional<Viajero> viajeroEncontrado = repositorioViajero.buscarPorEmailYContrasenia("viajero.base.test@unlam.com", "kfsvc1_incorrecta");

        // Assert
        assertThat(viajeroEncontrado.isPresent(), is(false));
    }

    @Test
    public void siElEmailNoExisteNoDevuelveAlViajero(){
        // Act
        Optional<Viajero> viajeroEncontrado = repositorioViajero.buscarPorEmailYContrasenia("otro@mail.com", "securePass1");

        // Assert
        assertThat(viajeroEncontrado.isPresent(), is(false));
    }

    @Test
    public void buscoViajeroPorEmailYLoEncuentro(){
        // Arrange: Email insertado en el setUp
        String email = "viajero.base.test@unlam.com";

        // Act
        Optional<Viajero> viajeroPorMailEncontrado = repositorioViajero.buscarPorEmail(email);

        // Assert
        assertTrue(viajeroPorMailEncontrado.isPresent());
        assertThat(viajeroPorMailEncontrado.get().getEmail(), is(email));
    }

    @Test
    public void buscoViajeroPorEmailYNoLoEncuentro(){
        // Act
        Optional<Viajero> viajeroPorMailEncontrado = repositorioViajero.buscarPorEmail("Juan@gmail.com");

        // Assert
        assertThat(viajeroPorMailEncontrado.isPresent(), is(false));
    }

    @Test
    public void buscoViajeroPorIdYLoEncuentro(){
        // Arrange: El ID se obtiene del objeto guardado en el setUp
        Long idExistente = viajeroBase.getId();

        // Act
        Optional<Viajero> viajeroPorIDEncontrado = repositorioViajero.buscarPorId(idExistente);

        // Assert
        assertTrue(viajeroPorIDEncontrado.isPresent());
        assertThat(viajeroPorIDEncontrado.get().getId(), is(idExistente));
        assertThat(viajeroPorIDEncontrado.get().getEmail(), is("viajero.base.test@unlam.com"));
    }

    @Test
    public void buscoViajeroPorIdYNoLoEncuentro(){
        // Act
        Optional<Viajero> viajeroPorIDEncontrado = repositorioViajero.buscarPorId(999L);

        // Assert
        assertThat(viajeroPorIDEncontrado.isPresent(), is(false));
    }
}