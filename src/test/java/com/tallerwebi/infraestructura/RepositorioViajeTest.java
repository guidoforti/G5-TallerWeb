package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Entity.*;
import com.tallerwebi.dominio.IRepository.RepositorioViaje;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class RepositorioViajeTest {

    private RepositorioViaje repositorio;
    private Conductor conductor;
    private Ubicacion origen;
    private Ubicacion destino;
    private LocalDateTime ahora;

    @BeforeEach
    void setUp() {
        repositorio = new RepositorioViajeImpl(true); // lista vacía
        conductor = new Conductor(1L, null, "Juan", "juan@mail.com", "1234", LocalDate.now());
        origen = new Ubicacion("Av. Rivadavia 1234", -34.65f, -58.62f);
        destino = new Ubicacion("Av. Eva Perón 456", -34.66f, -58.61f);
        ahora = LocalDateTime.now();
    }

    private Viaje crearViaje(Long id, Ubicacion origen, Ubicacion destino, Conductor conductor) {
        return new Viaje(
                id,
                conductor,
                null, // viajeros
                origen,
                destino,
                null, // paradas
                ahora,
                1000.0,
                4,
                4,
                ahora,
                null // vehiculo
        );
    }

    @Test
    void deberiaGuardarViajeNuevo() {
        Viaje viaje = crearViaje(1L, origen, destino, conductor);

        boolean guardado = repositorio.guardarViaje(viaje);

        assertThat(guardado, is(true));
        assertThat(repositorio.buscarPorId(1L).isPresent(), is(true));
    }

    @Test
    void noDeberiaGuardarViajeDuplicadoMismoOrigenDestinoConductor() {
        Viaje v1 = crearViaje(1L, origen, destino, conductor);
        Viaje v2 = crearViaje(2L, origen, destino, conductor);

        assertThat(repositorio.guardarViaje(v1), is(true));
        assertThat(repositorio.guardarViaje(v2), is(false));
    }

    @Test
    void deberiaBuscarPorId() {
        Viaje viaje = crearViaje(5L, origen, destino, conductor);
        repositorio.guardarViaje(viaje);

        Optional<Viaje> encontrado = repositorio.buscarPorId(5L);

        assertThat(encontrado.isPresent(), is(true));
        assertThat(encontrado.get().getOrigen(), equalTo(origen));
    }

    @Test
    void noDeberiaEncontrarSiIdNoExiste() {
        Optional<Viaje> encontrado = repositorio.buscarPorId(999L);

        assertThat(encontrado.isPresent(), is(false));
    }

    @Test
    void deberiaModificarViajeExistente() {
        Viaje viaje = crearViaje(10L, origen, destino, conductor);
        repositorio.guardarViaje(viaje);

        Ubicacion nuevoOrigen = new Ubicacion("Haedo 123", -34.64f, -58.60f);
        Viaje modificado = crearViaje(10L, nuevoOrigen, destino, conductor);

        boolean modificadoOk = repositorio.modificarViaje(modificado);

        assertThat(modificadoOk, is(true));
        assertThat(repositorio.buscarPorId(10L).get().getOrigen(), equalTo(nuevoOrigen));
    }

    @Test
    void noDeberiaModificarViajeInexistente() {
        Viaje viaje = crearViaje(20L, origen, destino, conductor);

        boolean modificadoOk = repositorio.modificarViaje(viaje);

        assertThat(modificadoOk, is(false));
    }

    @Test
    void deberiaBorrarViaje() {
        Viaje viaje = crearViaje(30L, origen, destino, conductor);
        repositorio.guardarViaje(viaje);

        boolean borrado = repositorio.borrarViaje(30L);

        assertThat(borrado, is(true));
        assertThat(repositorio.buscarPorId(30L).isPresent(), is(false));
    }

    @Test
    void deberiaEncontrarPorOrigenDestinoYConductor() {
        Viaje viaje = crearViaje(40L, origen, destino, conductor);
        repositorio.guardarViaje(viaje);

        // aunque la dirección cambie, mientras lat/lng sean iguales, equals() devuelve true
        Ubicacion origenIgual = new Ubicacion("Otra dirección", -34.65f, -58.62f);
        Ubicacion destinoIgual = new Ubicacion("Otra dirección", -34.66f, -58.61f);

        Optional<Viaje> encontrado = repositorio.encontrarPorOrigenDestinoYConductor(origenIgual, destinoIgual, conductor);

        assertThat(encontrado.isPresent(), is(true));
        assertThat(encontrado.get().getId(), equalTo(40L));
    }

    @Test
    void noDeberiaEncontrarSiNoCoincidenOrigenODestinoOConductor() {
        Viaje viaje = crearViaje(50L, origen, destino, conductor);
        repositorio.guardarViaje(viaje);

        Ubicacion otroOrigen = new Ubicacion("Ituzaingó", -34.70f, -58.65f);

        Optional<Viaje> encontrado = repositorio.encontrarPorOrigenDestinoYConductor(otroOrigen, destino, conductor);

        assertThat(encontrado.isPresent(), is(false));
    }
}
