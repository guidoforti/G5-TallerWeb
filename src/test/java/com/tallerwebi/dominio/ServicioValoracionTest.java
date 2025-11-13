package com.tallerwebi.dominio;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.Entity.Valoracion;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.Enums.EstadoDeViaje;
import com.tallerwebi.dominio.IRepository.RepositorioUsuario;
import com.tallerwebi.dominio.IRepository.RepositorioValoracion;
import com.tallerwebi.dominio.IRepository.RepositorioViajero;
import com.tallerwebi.dominio.IRepository.ViajeRepository;
import com.tallerwebi.dominio.IServicio.ServicioValoracion;
import com.tallerwebi.dominio.ServiceImpl.ServicioValoracionImpl;
import com.tallerwebi.dominio.excepcion.DatoObligatorioException;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.dominio.excepcion.ViajeNoEncontradoException;
import com.tallerwebi.presentacion.DTO.InputsDTO.ValoracionIndividualInputDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.mockito.ArgumentMatchers;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class ServicioValoracionTest {

    private RepositorioValoracion repositorioValoracionMock;
    private RepositorioUsuario repositorioUsuarioMock;
    private RepositorioViajero repositorioViajeroMock;
    private ViajeRepository viajeRepositoryMock;
    private ServicioValoracion servicioValoracion;
    private final Long VIAJE_ID = 100L;
    private final Long EMISOR_ID = 1L;
    private final Long RECEPTOR_ID = 2L;

    @BeforeEach
    void setUp() {
        repositorioValoracionMock = mock(RepositorioValoracion.class);
        repositorioUsuarioMock = mock(RepositorioUsuario.class);
        repositorioViajeroMock = mock(RepositorioViajero.class);
        viajeRepositoryMock = mock(ViajeRepository.class);

        when(repositorioUsuarioMock.buscarPorId(RECEPTOR_ID)).thenReturn(Optional.of(crearViajero(RECEPTOR_ID)));
        when(viajeRepositoryMock.findById(VIAJE_ID)).thenReturn(Optional.of(crearViaje(VIAJE_ID, EstadoDeViaje.FINALIZADO)));
        when(repositorioValoracionMock.yaExisteValoracionParaViaje(anyLong(), anyLong(), anyLong())).thenReturn(false);

        servicioValoracion = new ServicioValoracionImpl(
                repositorioValoracionMock,
                repositorioUsuarioMock,
                repositorioViajeroMock,
                viajeRepositoryMock
        );
    }

    private Usuario crearUsuario(Long id) {
        Conductor usuario = new Conductor();
        usuario.setId(id);
        usuario.setNombre("Conductor" + id);
        usuario.setEmail("elconductor" + id + "@conductor.com");
        usuario.setRol("CONDUCTOR");
        usuario.activar();
        return usuario;
    }

    private Viajero crearViajero(Long id) {
        Viajero viajero = new Viajero();
        viajero.setId(id);
        viajero.setNombre("Viajero" + id);
        viajero.setEmail("viajero" + id + "@email.com");
        viajero.setRol("VIAJERO");
        viajero.activar();
        return viajero;
    }

    private Viaje crearViaje(Long id, EstadoDeViaje estado) {
        Viaje viaje = new Viaje();
        viaje.setId(id);
        viaje.setEstado(estado);
        viaje.setReservas(new ArrayList<>());
        return viaje;
    }

    @Test
    void deberiaGuardarUnaValoracionCorrectamente() throws Exception {
        // given
        Usuario emisor = crearUsuario(EMISOR_ID);
        ValoracionIndividualInputDTO dto = new ValoracionIndividualInputDTO(RECEPTOR_ID, 5, "Excelente servicio");

        // when
        servicioValoracion.valorarUsuario(emisor, dto, VIAJE_ID);

        // then
        verify(repositorioValoracionMock, times(1)).save(ArgumentMatchers.any(Valoracion.class));
    }

    @Test
    void deberiaLanzarExcepcionSiViajeNoExiste() {
        // given
        Usuario emisor = crearUsuario(EMISOR_ID);
        ValoracionIndividualInputDTO dto = new ValoracionIndividualInputDTO(RECEPTOR_ID, 5, "Excelente servicio");
        when(viajeRepositoryMock.findById(VIAJE_ID)).thenReturn(Optional.empty());

        // when & then
        DatoObligatorioException exception = assertThrows(DatoObligatorioException.class, () -> {
            servicioValoracion.valorarUsuario(emisor, dto, VIAJE_ID);
        });
        assertThat(exception.getMessage(), containsString("El Viaje no existe para registrar la valoración."));
    }

    @Test
    void noDeberiaPermitirValorarSiElViajeNoEstaFinalizado() {
        // given
        Usuario emisor = crearUsuario(EMISOR_ID);
        ValoracionIndividualInputDTO dto = new ValoracionIndividualInputDTO(RECEPTOR_ID, 5, "Excelente");
        Viaje viajeEnCurso = crearViaje(VIAJE_ID, EstadoDeViaje.EN_CURSO);

        when(viajeRepositoryMock.findById(VIAJE_ID)).thenReturn(Optional.of(viajeEnCurso));

        // when & then
        DatoObligatorioException exception = assertThrows(
                DatoObligatorioException.class,
                () -> servicioValoracion.valorarUsuario(emisor, dto, VIAJE_ID)
        );

        assertThat(exception.getMessage(), containsString("Solo puedes valorar viajes finalizados"));
        verify(repositorioValoracionMock, never()).save(ArgumentMatchers.any());
    }

    @Test
    void noDeberiaPermitirLaDobleValoracionParaElMismoViaje() {
        // given
        Usuario emisor = crearUsuario(EMISOR_ID);
        ValoracionIndividualInputDTO dto = new ValoracionIndividualInputDTO(RECEPTOR_ID, 5, "Excelente");

        when(repositorioValoracionMock.yaExisteValoracionParaViaje(EMISOR_ID, RECEPTOR_ID, VIAJE_ID)).thenReturn(true);

        // when & then
        DatoObligatorioException exception = assertThrows(
                DatoObligatorioException.class,
                () -> servicioValoracion.valorarUsuario(emisor, dto, VIAJE_ID)
        );

        assertThat(exception.getMessage(), is("Ya has valorado a este usuario para este viaje."));
        verify(repositorioValoracionMock, never()).save(ArgumentMatchers.any());
    }

    @Test
    void noDeberiaPermitirAutoValoracion() {
        // given
        Viajero emisor = crearViajero(EMISOR_ID);
        ValoracionIndividualInputDTO dto = new ValoracionIndividualInputDTO(EMISOR_ID, 4, "Me valoro a mi mismo.");

        // when & then
        DatoObligatorioException exception = assertThrows(
                DatoObligatorioException.class,
                () -> servicioValoracion.valorarUsuario(emisor, dto, VIAJE_ID)
        );

        assertThat(exception.getMessage(), is("Error. No podes valorarte a vos mismo"));
        verify(repositorioValoracionMock, never()).save(ArgumentMatchers.any());
    }

    @Test
    void deberiaLanzarExcepcionSiPuntuacionEsNula() {
        // given
        Viajero emisor = crearViajero(EMISOR_ID);
        ValoracionIndividualInputDTO dto = new ValoracionIndividualInputDTO(RECEPTOR_ID, null, "Comentario valido.");

        // when & then
        DatoObligatorioException exception = assertThrows(
                DatoObligatorioException.class,
                () -> servicioValoracion.valorarUsuario(emisor, dto, VIAJE_ID)
        );
        assertThat(exception.getMessage(), is("La valoracion debe estar entre 1 y 5"));
        verify(repositorioValoracionMock, never()).save(ArgumentMatchers.any());
    }

    @Test
    void deberiaLanzarExcepcionSiPuntuacionEsCero() {
        // given
        Viajero emisor = crearViajero(EMISOR_ID);
        ValoracionIndividualInputDTO dto = new ValoracionIndividualInputDTO(RECEPTOR_ID, 0, "Comentario valido.");

        // when & then
        DatoObligatorioException exception = assertThrows(
                DatoObligatorioException.class,
                () -> servicioValoracion.valorarUsuario(emisor, dto, VIAJE_ID)
        );
        assertThat(exception.getMessage(), is("La valoracion debe estar entre 1 y 5"));
        verify(repositorioValoracionMock, never()).save(ArgumentMatchers.any());
    }

    @Test
    void deberiaLanzarExcepcionSiPuntuacionEsSeis() {
        // given
        Viajero emisor = crearViajero(EMISOR_ID);
        ValoracionIndividualInputDTO dto = new ValoracionIndividualInputDTO(RECEPTOR_ID, 6, "Comentario valido.");

        // when & then
        DatoObligatorioException exception = assertThrows(
                DatoObligatorioException.class,
                () -> servicioValoracion.valorarUsuario(emisor, dto, VIAJE_ID)
        );
        assertThat(exception.getMessage(), is("La valoracion debe estar entre 1 y 5"));
        verify(repositorioValoracionMock, never()).save(ArgumentMatchers.any());
    }

    @Test
    void deberiaLanzarExcepcionSiUsuarioReceptorNoExiste() {
        // given
        Usuario emisor = crearUsuario(EMISOR_ID);
        ValoracionIndividualInputDTO dto = new ValoracionIndividualInputDTO(999L, 4, "Comentario valido.");
        when(repositorioUsuarioMock.buscarPorId(999L)).thenReturn(Optional.empty());

        // when & then
        UsuarioInexistente exception = assertThrows(
                UsuarioInexistente.class,
                () -> servicioValoracion.valorarUsuario(emisor, dto, VIAJE_ID)
        );
        assertThat(exception.getMessage(), containsString("No se encontró el usuario receptor"));
    }

    @Test
    void obtenerViajeros_debeLanzarExcepcionSiViajeNoExiste() {
        // given
        when(viajeRepositoryMock.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.empty());

        // when & then
        assertThrows(ViajeNoEncontradoException.class, () -> {
            servicioValoracion.obtenerViajeros(1L);
        });
    }

    @Test
    void obtenerViajeros_debeRetornarListaVaciaSiNoHayReservas() throws ViajeNoEncontradoException {
        // given
        Viaje viajeMock = crearViaje(1L, EstadoDeViaje.FINALIZADO);
        viajeMock.setReservas(Collections.emptyList());
        when(viajeRepositoryMock.findById(1L)).thenReturn(Optional.of(viajeMock));

        // when
        List<Viajero> viajeros = servicioValoracion.obtenerViajeros(1L);

        // then
        assertThat(viajeros, is(empty()));
    }
    @Test
    void deberiaCalcularElPromedioYRetornarCeroSiListaEstaVacia() {
        // given
        when(repositorioValoracionMock.findByReceptorId(RECEPTOR_ID)).thenReturn(Collections.emptyList());

        // when
        Double promedio = servicioValoracion.calcularPromedioValoraciones(RECEPTOR_ID);

        // then
        assertThat(promedio, is(0.0));
    }
}