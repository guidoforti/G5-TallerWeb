package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Entity.Ciudad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class RepositorioCiudadTest {

    private RepositorioCiudadImpl repositorio;

    @BeforeEach
    void setUp() {
        repositorio = new RepositorioCiudadImpl();
    }

    @Test
    void deberiaDevolverListaDeUbicacionesNoNula() {
        List<Ciudad> ubicaciones = repositorio.findAll();

        assertThat(ubicaciones, is(notNullValue()));
    }

    @Test
    void deberiaContenerAlMenosUnaUbicacion() {
        List<Ciudad> ubicaciones = repositorio.findAll();

        assertThat(ubicaciones.size(), greaterThan(0));
    }

    @Test
    void ubicacionesDeberianTenerLatitudYLongitud() {
        List<Ciudad> ubicaciones = repositorio.findAll();

        Ciudad ciudad = ubicaciones.get(0);

        assertThat(ciudad.getLatitud(), is(notNullValue()));
        assertThat(ciudad.getLongitud(), is(notNullValue()));
    }
}