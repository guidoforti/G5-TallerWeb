package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.IRepository.RepositorioConductor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class RepositorioConductorTest {
    private RepositorioConductor repositorio;

    @BeforeEach
    void setUp() {
        repositorio = new RepositorioConductorImpl();
    }

    @Test
    void deberiaGuardarConductorNuevo() {

    }

    @Test
    void noDeberiaGuardarConductorConEmailRepetido() {
        Conductor c1 = new Conductor(null , "Ana", "ana@mail.com", "pass", LocalDate.now(), new ArrayList<>(), new ArrayList<>());
        Conductor c2 = new Conductor(null,  "Pedro", "ana@mail.com", "otra", LocalDate.now(), new ArrayList<>(), new ArrayList<>());

        assertThat(repositorio.guardar(c1), is(true));
        assertThat(repositorio.guardar(c2), is(false));
        // assertThat(repositorio.listarTodos(), hasSize(1));
    }

    @Test
    void deberiaBuscarPorEmailYContrasenia() {
        Conductor c = new Conductor(null,  "Ana", "ana@mail.com", "pass", LocalDate.now(), new ArrayList<>(), new ArrayList<>());
        repositorio.guardar(c);

        Optional<Conductor> encontrado = repositorio.buscarPorEmailYContrasenia("ana@mail.com", "pass");

        assertThat(encontrado.isPresent(), is(true));
        assertThat(encontrado.get().getNombre(), equalTo("Ana"));
    }

    @Test
    void noDeberiaEncontrarSiContraseniaIncorrecta() {
        Conductor c = new Conductor(null, "Ana", "ana@mail.com", "pass", LocalDate.now(), new ArrayList<>(), new ArrayList<>());
        repositorio.guardar(c);

        Optional<Conductor> encontrado = repositorio.buscarPorEmailYContrasenia("ana@mail.com", "mal");

        assertThat(encontrado.isPresent(), is(false));
    }

    @Test
    void deberiaBuscarPorId() {
        Conductor c = new Conductor(null,  "Juan", "juan@mail.com", "1234", LocalDate.now(), new ArrayList<>(), new ArrayList<>());
        repositorio.guardar(c);

        Optional<Conductor> encontrado = repositorio.buscarPorId(c.getId());

        assertThat(encontrado.isPresent(), is(true));
        assertThat(encontrado.get().getNombre(), equalTo("Juan"));
    }
}
