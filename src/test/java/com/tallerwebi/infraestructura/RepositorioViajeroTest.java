package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.IRepository.RepositorioViajero;

public class RepositorioViajeroTest {

    private RepositorioViajero repositorioViajero;

    @BeforeEach
    public void init(){
        this.repositorioViajero = new RepositorioViajeroImpl();
    }

    @Test
    public void cuandoSeAgregaViajeroNuevoSeGuarda(){
        Viajero viajero = new Viajero(null, "Patricio", 20, "Patricio@gmail.com", "1234aa", new ArrayList<>());

        Boolean guardar = this.repositorioViajero.guardar(viajero);

        assertThat(guardar, is(true));
        assertThat(viajero.getId(), is(4L));
    }

    @Test
    public void cuandoSeAgregaViajeroConEmailDuplicadoNoSeGuarda(){
         Viajero viajero = new Viajero(null, "Patricio", 20, "Patricio@gmail.com", "1234aa", new ArrayList<>());
         Viajero viajeroDos = new Viajero(null, "Julia", 24, "Julia@gmail.com", "378dsf", new ArrayList<>());
         Viajero viajeroTres = new Viajero(null, "Rodrigo", 22, "Patricio@gmail.com", "84kdgf", new ArrayList<>());

         assertThat(repositorioViajero.guardar(viajero), is(true));
         assertThat(repositorioViajero.guardar(viajeroDos),is(true));
         assertThat(repositorioViajero.guardar(viajeroTres), is(false));
    }

    @Test
    public void buscoViajeroPorEmailYContraseniaYLoEncuentro(){
        Viajero viajero = new Viajero(null, "Juan", 21, "Juan@gmail.com", "asfsnak3", new ArrayList<>());
        repositorioViajero.guardar(viajero);

        Optional<Viajero> viajeroEncontrado = repositorioViajero.buscarPorEmailYContrasenia("Juan@gmail.com", "asfsnak3");

        assertThat(viajeroEncontrado.isPresent(), is(true));
        assertThat(viajeroEncontrado.get().getNombre(), is("Juan"));
    }

    @Test
    public void siLaContraseniaEsIncorrectaNoDevuelveAlViajero(){
        Viajero viajero = new Viajero(null, "Mario", 30, "Mario@gmail.com", "jgfs2", new ArrayList<>());
        repositorioViajero.guardar(viajero);

        Optional<Viajero> viajeroEncontrado = repositorioViajero.buscarPorEmailYContrasenia("Mario@gmail.com", "kfsvc1");

        assertThat(viajeroEncontrado.isPresent(), is(false));
    }

    @Test
    public void buscoViajeroPorEmailYLoEncuentro(){
        Viajero viajero = new Viajero(null, "Pepe", 27, "Pepe@gmail.com", "sfsa88", new ArrayList<>());
        repositorioViajero.guardar(viajero);

        Optional<Viajero> viajeroPorMailEncontrado = repositorioViajero.buscarPorEmail("Pepe@gmail.com");

        assertThat(viajeroPorMailEncontrado.isPresent(), is(true));
        assertThat(viajeroPorMailEncontrado.get().getEmail(), is("Pepe@gmail.com"));
    }

    @Test
    public void buscoViajeroPorEmailYNoLoEncuentro(){
        Viajero viajero = new Viajero(null, "Pepe", 27, "Pepe@gmail.com", "sfsa88", new ArrayList<>());
        repositorioViajero.guardar(viajero);

        Optional<Viajero> viajeroPorMailEncontrado = repositorioViajero.buscarPorEmail("Juan@gmail.com");

        assertFalse(viajeroPorMailEncontrado.isPresent());
    }

    @Test
    public void buscoViajeroPorIdYLoEncuentro(){
        Viajero viajero = new Viajero(null, "Ramon", 35, "Ramon@gmail.com", "SAJasfn2", new ArrayList<>());
        repositorioViajero.guardar(viajero);

        Optional<Viajero> viajeroPorIDEncontrado = repositorioViajero.buscarPorId(viajero.getId());

        assertThat(viajeroPorIDEncontrado.get().getId(), is(4L));
        assertThat(viajeroPorIDEncontrado.get().getEmail(), is("Ramon@gmail.com"));
    }
    
    @Test
    public void buscoViajeroPorIdYNoLoEncuentro(){
        Viajero viajero = new Viajero(null, "Ramon", 35, "Ramon@gmail.com", "SAJasfn2", new ArrayList<>());
        repositorioViajero.guardar(viajero);

         Optional<Viajero> viajeroPorIDEncontrado = repositorioViajero.buscarPorId(5L);

         assertThat(viajeroPorIDEncontrado.isPresent(), is(false));
    }
}
