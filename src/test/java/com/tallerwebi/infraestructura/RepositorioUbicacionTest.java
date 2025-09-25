package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Entity.Ubicacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class RepositorioUbicacionTest {

    private RepositorioUbicacionImpl repositorio;

    @BeforeEach
    void setUp() {
        repositorio = new RepositorioUbicacionImpl();
    }

    @Test
    void deberiaDevolverListaDeUbicacionesNoNula() {
        List<Ubicacion> ubicaciones = repositorio.findAll();

        assertThat(ubicaciones, is(notNullValue()));
    }

    @Test
    void deberiaContenerAlMenosUnaUbicacion() {
        List<Ubicacion> ubicaciones = repositorio.findAll();

        assertThat(ubicaciones.size(), greaterThan(0));
    }

    @Test
    void ubicacionesDeberianTenerLatitudYLongitud() {
        List<Ubicacion> ubicaciones = repositorio.findAll();

        Ubicacion ubicacion = ubicaciones.get(0);

        assertThat(ubicacion.getLatitud(), is(notNullValue()));
        assertThat(ubicacion.getLongitud(), is(notNullValue()));
    }
}