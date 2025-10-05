package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.Matchers.*;


import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tallerwebi.dominio.Entity.Ciudad;
import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Viaje;

import com.tallerwebi.dominio.Enums.EstadoDeViaje;
import com.tallerwebi.dominio.IRepository.ViajeRepository;


public class RepositorioViajeTest {

    private ViajeRepository repositorio;

    @BeforeEach
    void setUp() {
        repositorio = new ViajeRepositoryImpl();
    }

    @Test
    void deberiaGuardarViajeNuevo() {
        Viaje viaje = new Viaje();
        viaje.setId(200L); 
        viaje.setEstado(EstadoDeViaje.DISPONIBLE);

        repositorio.guardarViaje(viaje);

        Viaje encontrado = repositorio.findById(200L);
        assertThat(encontrado, notNullValue());
        assertThat(encontrado.getEstado(), equalTo(EstadoDeViaje.DISPONIBLE));
    }

    @Test
    void deberiaModificarViajeExistente() {
        Viaje viaje = new Viaje();
        viaje.setId(300L);
        viaje.setEstado(EstadoDeViaje.DISPONIBLE);
        repositorio.guardarViaje(viaje);

        viaje.setEstado(EstadoDeViaje.CANCELADO);
        repositorio.modificarViaje(viaje);

        Viaje modificado = repositorio.findById(300L);
        assertThat(modificado.getEstado(), equalTo(EstadoDeViaje.CANCELADO));
    }

    @Test
    void deberiaBorrarViajePorId() {
        Viaje viaje = new Viaje();
        viaje.setId(400L);
        repositorio.guardarViaje(viaje);

        repositorio.borrarViaje(400L);

        assertThrows(NoSuchElementException.class, () -> repositorio.findById(400L));
    }



    @Test
    void deberiaBuscarPorOrigenDestinoYConductor() {
        Ciudad origen = new Ciudad(null, "San Justo", 0, 0);
        Ciudad destino = new Ciudad(null, "La Plata", 0, 0);
        Conductor conductor = new Conductor();
        conductor.setId(1L);

        Viaje viaje = new Viaje();
        viaje.setId(500L);
        viaje.setOrigen(origen);
        viaje.setDestino(destino);
        viaje.setConductor(conductor);

        repositorio.guardarViaje(viaje);

        List<Viaje> resultados = repositorio.findByOrigenYDestinoYConductor(origen, destino, conductor);

        assertThat(resultados, hasSize(1));
        assertThat(resultados.get(0).getId(), equalTo(500L));
    }

    @Test
    void noDeberiaEncontrarViajeSiConductorNoCoincide() {
        Ciudad origen = new Ciudad(null, "San Justo", 0, 0);
        Ciudad destino = new Ciudad(null, "La Plata", 0, 0);
        Conductor conductor = new Conductor();
        conductor.setId(1L);

        Conductor otro = new Conductor();
        otro.setId(99L);

        Viaje viaje = new Viaje();
        viaje.setId(600L);
        viaje.setOrigen(origen);
        viaje.setDestino(destino);
        viaje.setConductor(otro);

        repositorio.guardarViaje(viaje);

        List<Viaje> resultados = repositorio.findByOrigenYDestinoYConductor(origen, destino, conductor);

        assertThat(resultados, empty());
    }


}
