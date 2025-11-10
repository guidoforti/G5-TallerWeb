package com.tallerwebi.dominio;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Reserva;
import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.Entity.Valoracion;
import com.tallerwebi.dominio.Entity.Viaje;
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
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

        // Configuración base de Mocks
        when(repositorioUsuarioMock.buscarPorId(RECEPTOR_ID)).thenReturn(Optional.of(crearViajero(RECEPTOR_ID)));

        // Configuración de Valoración Exitosa por defecto
        when(viajeRepositoryMock.findById(VIAJE_ID)).thenReturn(Optional.of(crearViaje(VIAJE_ID, EstadoDeViaje.FINALIZADO)));
        when(repositorioValoracionMock.yaExisteValoracionParaViaje(anyLong(), anyLong(), anyLong())).thenReturn(false);

        servicioValoracion = new ServicioValoracionImpl(
                repositorioValoracionMock,
                repositorioUsuarioMock,
                repositorioViajeroMock,
                viajeRepositoryMock
        );
    }

    // --- HELPERS ---

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

    // --- TESTS PARA VALORAR USUARIO (Nueva Lógica con ViajeId) ---

    @Test
    void deberiaGuardarUnaValoracionCorrectamente() throws Exception {
        // given
        Usuario emisor = crearUsuario(EMISOR_ID);
        ValoracionIndividualInputDTO dto = new ValoracionIndividualInputDTO(RECEPTOR_ID, 5, "Excelente servicio");

        // when
        servicioValoracion.valorarUsuario(emisor, dto, VIAJE_ID);

        // then
        ArgumentCaptor<Valoracion> valoracionCaptor = ArgumentCaptor.forClass(Valoracion.class);
        verify(repositorioValoracionMock, times(1)).save(valoracionCaptor.capture());

        verify(viajeRepositoryMock, times(1)).findById(VIAJE_ID);
        verify(repositorioValoracionMock, times(1)).yaExisteValoracionParaViaje(EMISOR_ID, RECEPTOR_ID, VIAJE_ID);

        Valoracion valoracionGuardada = valoracionCaptor.getValue();
        assertThat(valoracionGuardada.getEmisor(), equalTo(emisor));
        assertThat(valoracionGuardada.getReceptor().getId(), is(RECEPTOR_ID));
        assertThat(valoracionGuardada.getViaje().getId(), is(VIAJE_ID));
        assertThat(valoracionGuardada.getPuntuacion(), is(5));
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
        verify(repositorioValoracionMock, never()).save(any());
    }

    @Test
    void noDeberiaPermitirLaDobleValoracionParaElMismoViaje() {
        // given
        Usuario emisor = crearUsuario(EMISOR_ID);
        ValoracionIndividualInputDTO dto = new ValoracionIndividualInputDTO(RECEPTOR_ID, 5, "Excelente");

        // CLAVE: Simular que la valoración YA EXISTE para este VIAJE
        when(repositorioValoracionMock.yaExisteValoracionParaViaje(EMISOR_ID, RECEPTOR_ID, VIAJE_ID)).thenReturn(true);

        // when & then
        DatoObligatorioException exception = assertThrows(
                DatoObligatorioException.class,
                () -> servicioValoracion.valorarUsuario(emisor, dto, VIAJE_ID)
        );

        assertThat(exception.getMessage(), is("Ya has valorado a este usuario para este viaje."));
        verify(repositorioValoracionMock, never()).save(any());
    }

    @Test
    void deberiaPermitirValorarElMismoReceptorEnViajesDiferentes() throws Exception {
        // given
        Long viaje2Id = 200L;
        Usuario emisor = crearUsuario(EMISOR_ID);
        ValoracionIndividualInputDTO dto = new ValoracionIndividualInputDTO(RECEPTOR_ID, 4, "Viaje 2 OK.");

        // Simular que el Viaje 2 está FINALIZADO
        when(viajeRepositoryMock.findById(viaje2Id)).thenReturn(Optional.of(crearViaje(viaje2Id, EstadoDeViaje.FINALIZADO)));

        // Simular que NO existe valoración para el Viaje 2 (CLAVE)
        when(repositorioValoracionMock.yaExisteValoracionParaViaje(EMISOR_ID, RECEPTOR_ID, viaje2Id)).thenReturn(false);

        // when
        servicioValoracion.valorarUsuario(emisor, dto, viaje2Id);

        // then
        // Se verifica la persistencia para el Viaje 2
        verify(repositorioValoracionMock, times(1)).save(any(Valoracion.class));
        // Se verifica la unicidad contra el Viaje 2
        verify(repositorioValoracionMock, times(1)).yaExisteValoracionParaViaje(EMISOR_ID, RECEPTOR_ID, viaje2Id);
    }

    // --- TESTS GENERALES (Adaptados a la nueva estructura) ---

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
        verify(repositorioValoracionMock, never()).save(any());
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
        verify(repositorioValoracionMock, never()).save(any());
    }

    // --- TESTS PARA OBTENER DATOS Y VIAJEROS (Métodos de Consulta) ---

    @Test
    void obtenerViajeros_conViajerosExistentes_debeRetornarListaDeViajeros() throws ViajeNoEncontradoException {
        // given
        Viajero viajero1 = crearViajero(10L);
        Viajero viajero2 = crearViajero(20L);
        Reserva reserva1 = new Reserva(); reserva1.setViajero(viajero1);
        Reserva reserva2 = new Reserva(); reserva2.setViajero(viajero2);

        Viaje viajeMock = new Viaje();
        viajeMock.setReservas(Arrays.asList(reserva1, reserva2));

        when(viajeRepositoryMock.findById(viajeMock.getId())).thenReturn(Optional.of(viajeMock));

        // when
        List<Viajero> viajeros = servicioValoracion.obtenerViajeros(viajeMock.getId());

        // then
        assertThat(viajeros, hasSize(2));
        assertThat(viajeros, hasItem(viajero1));
        assertThat(viajeros, hasItem(viajero2));
        verify(viajeRepositoryMock, times(1)).findById(viajeMock.getId());
    }

    @Test
    void deberiaCalcularElPromedioCorrectamente() {
        // given
        Usuario receptor = crearUsuario(RECEPTOR_ID);
        Viajero emisor = crearViajero(EMISOR_ID);

        Valoracion v1 = new Valoracion(emisor, receptor, 5, "A", crearViaje(1L, EstadoDeViaje.FINALIZADO));
        Valoracion v2 = new Valoracion(emisor, receptor, 4, "B", crearViaje(2L, EstadoDeViaje.FINALIZADO));
        Valoracion v3 = new Valoracion(emisor, receptor, 3, "C", crearViaje(3L, EstadoDeViaje.FINALIZADO));

        when(repositorioValoracionMock.findByReceptorId(RECEPTOR_ID)).thenReturn(Arrays.asList(v1, v2, v3));

        // when
        Double promedio = servicioValoracion.calcularPromedioValoraciones(RECEPTOR_ID);

        // then
        assertThat(promedio, is(4.0));
    }
}