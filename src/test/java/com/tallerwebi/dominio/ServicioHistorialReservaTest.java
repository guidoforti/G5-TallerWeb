package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.HistorialReserva;
import com.tallerwebi.dominio.Entity.Reserva;
import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.Enums.EstadoReserva;
import com.tallerwebi.dominio.IRepository.RepositorioHistorialReserva;
import com.tallerwebi.dominio.IRepository.ViajeRepository;
import com.tallerwebi.dominio.IServicio.ServicioHistorialReserva;
import com.tallerwebi.dominio.ServiceImpl.ServicioHistorialReservaImpl;
import com.tallerwebi.dominio.excepcion.UsuarioNoAutorizadoException;
import com.tallerwebi.dominio.excepcion.ViajeNoEncontradoException;
import com.tallerwebi.presentacion.DTO.OutputsDTO.HistorialReservaDTO;

public class ServicioHistorialReservaTest {

   private RepositorioHistorialReserva repositorioHistorialReservaMock;
    private ViajeRepository viajeRepositoryMock;
    private ServicioHistorialReserva servicioHistorialReserva;

    @BeforeEach
    void setUp() {
        repositorioHistorialReservaMock = mock(RepositorioHistorialReserva.class);
        viajeRepositoryMock = mock(ViajeRepository.class);
        servicioHistorialReserva = new ServicioHistorialReservaImpl(repositorioHistorialReservaMock, viajeRepositoryMock);
    }

    private Viaje crearViajeDeTest(Conductor conductor) {
        Viaje viaje = new Viaje();
        viaje.setId(1L);
        viaje.setConductor(conductor);
        viaje.setReservas(Collections.emptyList());
        return viaje;
    }

    private HistorialReserva crearHistorialDeTest(Viaje viaje, Usuario conductor, Viajero viajero, Long id, EstadoReserva anterior, EstadoReserva nuevo) {
        Reserva reserva = new Reserva();
        reserva.setViaje(viaje);
        reserva.setViajero(viajero);
        reserva.setId(id);

        HistorialReserva historial = new HistorialReserva();
        historial.setId(id);
        historial.setViaje(viaje);
        historial.setReserva(reserva);
        historial.setConductor(conductor);
        historial.setViajero(viajero);
        historial.setFechaEvento(LocalDateTime.now());
        historial.setEstadoAnterior(anterior);
        historial.setEstadoNuevo(nuevo);
        return historial;
    }

    @Test
    void deberiaObtenerHistorialPorViajeCorrectamente() throws Exception {
        // given
        Conductor conductor = new Conductor();
        conductor.setId(1L);
        conductor.setNombre("Juan");

        Viajero viajero1 = new Viajero();
        viajero1.setNombre("Pedro");
        viajero1.setEmail("pedro@test.com");

        Viajero viajero2 = new Viajero();
        viajero2.setNombre("Maria");
        viajero2.setEmail("maria@test.com");

        Viaje viaje = crearViajeDeTest(conductor);

        HistorialReserva hr1 = crearHistorialDeTest(viaje, conductor, viajero1, 1L, EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA);
        HistorialReserva hr2 = crearHistorialDeTest(viaje, conductor, viajero2, 2L, EstadoReserva.CONFIRMADA, EstadoReserva.RECHAZADA);

        when(viajeRepositoryMock.findById(1L)).thenReturn(Optional.of(viaje));
        when(repositorioHistorialReservaMock.findByViaje(viaje)).thenReturn(Arrays.asList(hr1, hr2));

        // when
        List<HistorialReservaDTO> historial = servicioHistorialReserva.obtenerHistorialPorViaje(1L, conductor);

        // then
        assertThat(historial.size(), equalTo(2));

        assertThat(historial.get(0).getId(), equalTo(1L));
        assertThat(historial.get(0).getNombreViajero(), equalTo("Pedro"));
        assertThat(historial.get(0).getEmailViajero(), equalTo("pedro@test.com"));
        assertThat(historial.get(0).getNombreConductor(), equalTo("Juan"));
        assertThat(historial.get(0).getEstadoAnterior(), equalTo(EstadoReserva.PENDIENTE));
        assertThat(historial.get(0).getEstadoNuevo(), equalTo(EstadoReserva.CONFIRMADA));

        assertThat(historial.get(1).getId(), equalTo(2L));
        assertThat(historial.get(1).getNombreViajero(), equalTo("Maria"));
        assertThat(historial.get(1).getEmailViajero(), equalTo("maria@test.com"));
        assertThat(historial.get(1).getNombreConductor(), equalTo("Juan"));
        assertThat(historial.get(1).getEstadoAnterior(), equalTo(EstadoReserva.CONFIRMADA));
        assertThat(historial.get(1).getEstadoNuevo(), equalTo(EstadoReserva.RECHAZADA));
    }

    @Test
    void deberiaLanzarExcepcionSiViajeNoExiste() {
        // given
        Conductor conductor = new Conductor();
        conductor.setId(1L);

        when(viajeRepositoryMock.findById(999L)).thenReturn(Optional.empty());

        // when & then
        ViajeNoEncontradoException exception = assertThrows(
                ViajeNoEncontradoException.class,
                () -> servicioHistorialReserva.obtenerHistorialPorViaje(999L, conductor)
        );

        assertThat(exception.getMessage(), containsString("No se encontró el viaje con ID 999"));
    }

    @Test
    void deberiaLanzarExcepcionSiUsuarioNoEsConductorDelViaje() {
        // given
        Conductor conductorReal = new Conductor();
        conductorReal.setId(1L);

        Conductor usuarioEnSesion = new Conductor();
        usuarioEnSesion.setId(2L);

        Viaje viaje = crearViajeDeTest(conductorReal);

        when(viajeRepositoryMock.findById(1L)).thenReturn(Optional.of(viaje));

        // when & then
        UsuarioNoAutorizadoException exception = assertThrows(
                UsuarioNoAutorizadoException.class,
                () -> servicioHistorialReserva.obtenerHistorialPorViaje(1L, usuarioEnSesion)
        );

        assertThat(exception.getMessage(), equalTo("No tenés permisos para ver el historial de este viaje."));
    }

    @Test
    void deberiaDevolverListaVaciaSiNoHayHistorial() throws Exception {
        // given
        Conductor conductor = new Conductor();
        conductor.setId(1L);

        Viaje viaje = crearViajeDeTest(conductor);

        when(viajeRepositoryMock.findById(1L)).thenReturn(Optional.of(viaje));
        when(repositorioHistorialReservaMock.findByViaje(viaje)).thenReturn(Collections.emptyList());

        // when
        List<HistorialReservaDTO> historial = servicioHistorialReserva.obtenerHistorialPorViaje(1L, conductor);

        // then
        assertThat(historial, is(empty()));
    }
}
