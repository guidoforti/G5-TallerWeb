package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;

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

}
