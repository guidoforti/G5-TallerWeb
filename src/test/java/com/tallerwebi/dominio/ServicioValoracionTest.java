
package com.tallerwebi.dominio;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Reserva;
import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.Entity.Valoracion;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.IRepository.RepositorioUsuario;
import com.tallerwebi.dominio.IRepository.RepositorioValoracion;
import com.tallerwebi.dominio.IRepository.RepositorioViajero;
import com.tallerwebi.dominio.IRepository.ViajeRepository;
import com.tallerwebi.dominio.IServicio.ServicioValoracion;
import com.tallerwebi.dominio.ServiceImpl.ServicioValoracionImpl;
import com.tallerwebi.dominio.excepcion.DatoObligatorioException;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.dominio.excepcion.ViajeNoEncontradoException;
import com.tallerwebi.presentacion.DTO.InputsDTO.ValoracionNuevaInputDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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
    private Usuario receptorDummy;

    @BeforeEach
    void setUp() {
        repositorioValoracionMock = mock(RepositorioValoracion.class);
        repositorioUsuarioMock = mock(RepositorioUsuario.class);
        repositorioViajeroMock = mock(RepositorioViajero.class);
        viajeRepositoryMock = mock(ViajeRepository.class);
        receptorDummy = crearUsuario(99L);

        when(viajeRepositoryMock.existeViajeFinalizadoYNoValorado(anyLong(), anyLong())).thenReturn(true);
        when(repositorioUsuarioMock.buscarPorId(anyLong())).thenReturn(Optional.of(receptorDummy));

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

    // =================================================================
    // TESTS PARA: obtenerViajero(Long viajeroId)
    // =================================================================

    @Test
    void deberiaObtenerViajeroCorrectamente() throws Exception {
        // given
        Long viajeroId = 10L;
        Viajero viajero = crearViajero(viajeroId);
        when(repositorioViajeroMock.buscarPorId(viajeroId)).thenReturn(Optional.of(viajero));

        // when
        Viajero resultado = servicioValoracion.obtenerViajero(viajeroId);

        // then
        assertThat(resultado, equalTo(viajero));
        verify(repositorioViajeroMock).buscarPorId(viajeroId);
    }

    @Test
    void deberiaLanzarExcepcionSiViajeroNoExiste() {
        // given
        Long viajeroId = 999L;
        when(repositorioViajeroMock.buscarPorId(viajeroId)).thenReturn(Optional.empty());

        // when & then
        UsuarioInexistente exception = assertThrows(
            UsuarioInexistente.class,
            () -> servicioValoracion.obtenerViajero(viajeroId)
        );

        assertThat(exception.getMessage(), is("El viajero no existe."));
    }

    // =================================================================
    // TESTS PARA: obtenerUsuario(Long usuarioId)
    // =================================================================

    @Test
    void deberiaObtenerUsuarioCorrectamente() throws Exception {
        // given
        Long usuarioId = 15L;
        Usuario usuario = crearUsuario(usuarioId);
        when(repositorioUsuarioMock.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));

        // when
        Usuario resultado = servicioValoracion.obtenerUsuario(usuarioId);

        // then
        assertThat(resultado, equalTo(usuario));
        verify(repositorioUsuarioMock).buscarPorId(usuarioId);
    }

    @Test
    void deberiaLanzarExcepcionSiUsuarioNoExiste() {
        // given
        Long usuarioId = 888L;
        when(repositorioUsuarioMock.buscarPorId(usuarioId)).thenReturn(Optional.empty());

        // when & then
        UsuarioInexistente exception = assertThrows(
            UsuarioInexistente.class,
            () -> servicioValoracion.obtenerUsuario(usuarioId)
        );

        assertThat(exception.getMessage(), is("El usuario no existe."));
    }

    // =================================================================
    // TESTS PARA: valorarUsuario(Usuario emisor, ValoracionNuevaInputDTO dto)
    // =================================================================

    @Test
    void deberiaGuardarUnaValoracionCorrectamenteCuandoEmisorEsViajero() throws Exception {
        // given
        Viajero emisor = crearViajero(1L);
        Usuario receptor = crearUsuario(2L);
        ValoracionNuevaInputDTO dto = new ValoracionNuevaInputDTO();
        dto.setReceptorId(2L);
        dto.setPuntuacion(5);
        dto.setComentario("Excelente servicio y puntualidad.");

        when(repositorioUsuarioMock.buscarPorId(2L)).thenReturn(Optional.of(receptor));

        // when
        servicioValoracion.valorarUsuario(emisor, dto);

        // then
        ArgumentCaptor<Valoracion> valoracionCaptor = ArgumentCaptor.forClass(Valoracion.class);
        verify(repositorioValoracionMock).save(valoracionCaptor.capture());
        verify(viajeRepositoryMock).existeViajeFinalizadoYNoValorado(1L, 2L);

        Valoracion valoracionGuardada = valoracionCaptor.getValue();
        assertThat(valoracionGuardada.getEmisor(), equalTo(emisor));
        assertThat(valoracionGuardada.getReceptor(), equalTo(receptor));
        assertThat(valoracionGuardada.getPuntuacion(), is(5));
        assertThat(valoracionGuardada.getComentario(), is("Excelente servicio y puntualidad."));
    }

    @Test
    void deberiaGuardarUnaValoracionCorrectamenteCuandoEmisorEsConductor() throws Exception {
        // given
        Usuario emisor = crearUsuario(1L); // Conductor
        Viajero receptor = crearViajero(2L);
        ValoracionNuevaInputDTO dto = new ValoracionNuevaInputDTO();
        dto.setReceptorId(2L);
        dto.setPuntuacion(4);
        dto.setComentario("Buen viajero, muy respetuoso.");

        when(repositorioUsuarioMock.buscarPorId(2L)).thenReturn(Optional.of(receptor));

        // when
        servicioValoracion.valorarUsuario(emisor, dto);

        // then
        ArgumentCaptor<Valoracion> valoracionCaptor = ArgumentCaptor.forClass(Valoracion.class);
        verify(repositorioValoracionMock).save(valoracionCaptor.capture());
        verify(viajeRepositoryMock).existeViajeFinalizadoYNoValorado(1L, 2L);

        Valoracion valoracionGuardada = valoracionCaptor.getValue();
        assertThat(valoracionGuardada.getEmisor(), equalTo(emisor));
        assertThat(valoracionGuardada.getReceptor(), equalTo(receptor));
        assertThat(valoracionGuardada.getPuntuacion(), is(4));
        assertThat(valoracionGuardada.getComentario(), is("Buen viajero, muy respetuoso."));
    }

    @Test
    void noDeberiaPermitirAutoValoracion() {
        // given
        Viajero viajero = crearViajero(1L);
        ValoracionNuevaInputDTO dto = new ValoracionNuevaInputDTO();
        dto.setReceptorId(1L);
        dto.setPuntuacion(4);
        dto.setComentario("Me valoro a mi mismo.");

        // when & then
        DatoObligatorioException exception = assertThrows(
                DatoObligatorioException.class,
                () -> servicioValoracion.valorarUsuario(viajero, dto)
        );

        assertThat(exception.getMessage(), is("Error. No podes valorarte a vos mismo"));
        verify(viajeRepositoryMock, never()).existeViajeFinalizadoYNoValorado(anyLong(), anyLong());
        verify(repositorioUsuarioMock, never()).buscarPorId(anyLong());
        verify(repositorioValoracionMock, never()).save(any());
    }

    @Test
    void deberiaLanzarExcepcionSiPuntuacionEsNula() {
        // given
        Viajero emisor = crearViajero(1L);
        ValoracionNuevaInputDTO dto = new ValoracionNuevaInputDTO();
        dto.setReceptorId(2L);
        dto.setPuntuacion(null);
        dto.setComentario("Comentario valido.");

        // when & then
        DatoObligatorioException exception = assertThrows(
            DatoObligatorioException.class,
            () -> servicioValoracion.valorarUsuario(emisor, dto)
        );
        assertThat(exception.getMessage(), is("La valoracion debe estar entre 1 y 5"));
        verify(repositorioValoracionMock, never()).save(any());
    }

    @Test
    void deberiaLanzarExcepcionSiPuntuacionEstaFueraDeRangoBajo() {
        // given
        Viajero emisor = crearViajero(1L);
        ValoracionNuevaInputDTO dto = new ValoracionNuevaInputDTO();
        dto.setReceptorId(2L);
        dto.setPuntuacion(0);
        dto.setComentario("Comentario valido.");

        // when then
        DatoObligatorioException exception = assertThrows(
            DatoObligatorioException.class,
            () -> servicioValoracion.valorarUsuario(emisor, dto)
        );
        assertThat(exception.getMessage(), is("La valoracion debe estar entre 1 y 5"));
        verify(repositorioValoracionMock, never()).save(any());
    }

    @Test
    void deberiaLanzarExcepcionSiPuntuacionEstaFueraDeRangoAlto() {
        // given
        Viajero emisor = crearViajero(1L);
        ValoracionNuevaInputDTO dto = new ValoracionNuevaInputDTO();
        dto.setReceptorId(2L);
        dto.setPuntuacion(6);
        dto.setComentario("Comentario valido.");

        // when then
        DatoObligatorioException exception = assertThrows(
            DatoObligatorioException.class,
            () -> servicioValoracion.valorarUsuario(emisor, dto)
        );
        assertThat(exception.getMessage(), is("La valoracion debe estar entre 1 y 5"));
        verify(repositorioValoracionMock, never()).save(any());
    }

    @Test
    void deberiaLanzarExcepcionSiComentarioEsNulo() {
        // given
        Viajero emisor = crearViajero(1L);
        ValoracionNuevaInputDTO dto = new ValoracionNuevaInputDTO();
        dto.setReceptorId(2L);
        dto.setPuntuacion(4);
        dto.setComentario(null);

        // when then
        DatoObligatorioException exception = assertThrows(
                DatoObligatorioException.class,
                () -> servicioValoracion.valorarUsuario(emisor, dto)
        );
        assertThat(exception.getMessage(), is("El comentario es obligatorio"));
        verify(repositorioValoracionMock, never()).save(any());
    }

    @Test
    void deberiaLanzarExcepcionSiComentarioEsVacio() {
        // given
        Viajero emisor = crearViajero(1L);
        ValoracionNuevaInputDTO dto = new ValoracionNuevaInputDTO();
        dto.setReceptorId(2L);
        dto.setPuntuacion(4);
        dto.setComentario("  ");

        // whenthen
        DatoObligatorioException exception = assertThrows(
                DatoObligatorioException.class,
                () -> servicioValoracion.valorarUsuario(emisor, dto)
        );
        assertThat(exception.getMessage(), is("El comentario es obligatorio"));
        verify(repositorioValoracionMock, never()).save(any());
    }

    @Test
    void deberiaLanzarExcepcionSiNoExisteViajeFinalizadoParaValorar() {
        // given
        Viajero emisor = crearViajero(1L);
        ValoracionNuevaInputDTO dto = new ValoracionNuevaInputDTO();
        dto.setReceptorId(2L);
        dto.setPuntuacion(5);
        dto.setComentario("Comentario OK.");

        when(viajeRepositoryMock.existeViajeFinalizadoYNoValorado(1L, 2L)).thenReturn(false);

        // when & then
        DatoObligatorioException exception = assertThrows(
                DatoObligatorioException.class,
                () -> servicioValoracion.valorarUsuario(emisor, dto)
        );

        assertThat(exception.getMessage(), is("No hay un viaje concluido y pendiente de valoración entre usted y el usuario receptor."));
        verify(repositorioUsuarioMock, never()).buscarPorId(anyLong());
        verify(repositorioValoracionMock, never()).save(any());
    }

    @Test
    void deberiaLanzarExcepcionSiUsuarioReceptorNoExiste() {
        // given
        Viajero emisor = crearViajero(1L);
        ValoracionNuevaInputDTO dto = new ValoracionNuevaInputDTO();
        dto.setReceptorId(99L);
        dto.setPuntuacion(5);
        dto.setComentario("Comentario OK.");

        when(repositorioUsuarioMock.buscarPorId(99L)).thenReturn(Optional.empty());

        // whenthen
        UsuarioInexistente exception = assertThrows(
                UsuarioInexistente.class,
                () -> servicioValoracion.valorarUsuario(emisor, dto)
        );

        assertThat(exception.getMessage(), is("No se encontró el usuario receptor"));
        verify(repositorioValoracionMock, never()).save(any());
    }

    // =================================================================
    // TESTS PARA: obtenerValoracionesDeUsuario(Long usuarioId)
    // =================================================================

    @Test
    void deberiaObtenerValoracionesDeUsuario() {
        // given
        Usuario receptor = crearUsuario(2L);
        Viajero emisor1 = crearViajero(1L);
        Viajero emisor3 = crearViajero(3L);

        Valoracion v1 = new Valoracion(emisor1, receptor, 5, "Muy bueno");
        Valoracion v2 = new Valoracion(emisor3, receptor, 3, "Regular");

        when(repositorioValoracionMock.findByReceptorId(2L)).thenReturn(Arrays.asList(v1, v2));

        // when
        List<Valoracion> valoraciones = servicioValoracion.obtenerValoracionesDeUsuario(2L);

        // then
        assertThat(valoraciones, hasSize(2));
        assertThat(valoraciones.get(0).getPuntuacion(), is(5));
        assertThat(valoraciones.get(1).getComentario(), is("Regular"));
    }

    @Test
    void deberiaRetornarListaVaciaSiNoHayValoraciones() {
        // given
        when(repositorioValoracionMock.findByReceptorId(anyLong())).thenReturn(Collections.emptyList());

        // when
        List<Valoracion> valoraciones = servicioValoracion.obtenerValoracionesDeUsuario(50L);

        // then
        assertThat(valoraciones, notNullValue());
        assertThat(valoraciones, empty());
    }

    // =================================================================
    // TESTS PARA: calcularPromedioValoraciones(Long usuarioId)
    // =================================================================

    @Test
    void deberiaCalcularElPromedioCorrectamente() {
        // given
        Usuario receptor = crearUsuario(2L);
        Viajero emisor = crearViajero(1L);

        Valoracion v1 = new Valoracion(emisor, receptor, 5, "A");
        Valoracion v2 = new Valoracion(emisor, receptor, 4, "B");
        Valoracion v3 = new Valoracion(emisor, receptor, 3, "C");

        when(repositorioValoracionMock.findByReceptorId(2L)).thenReturn(Arrays.asList(v1, v2, v3));

        // when
        Double promedio = servicioValoracion.calcularPromedioValoraciones(2L);

        // then
        assertThat(promedio, is(4.0));
    }

    @Test
    void deberiaRetornarCeroCeroSiNoHayValoraciones() {
        // given
        when(repositorioValoracionMock.findByReceptorId(anyLong())).thenReturn(Collections.emptyList());

        // when
        Double promedio = servicioValoracion.calcularPromedioValoraciones(50L);

        // then
        assertThat(promedio, notNullValue());
        assertThat(promedio, is(0.0));
    }

    @Test
    void obtenerViajeros_conViajerosExistentes_debeRetornarListaDeViajeros() throws ViajeNoEncontradoException {
        // 1. Configuración de Mocks (Arrange)
        // Datos de Viajeros
        Viajero viajero1 = new Viajero(1, false, "Ninguna", "url1", new ArrayList<>(), new ArrayList<>());
        Viajero viajero2 = new Viajero(2, true, "Movilidad Reducida", "url2", new ArrayList<>(), new ArrayList<>());

        // Datos de Reservas con sus Viajeros (debe coincidir con la entidad)
        Reserva reserva1 = new Reserva();
        reserva1.setViajero(viajero1);
        Reserva reserva2 = new Reserva();
        reserva2.setViajero(viajero2);

        // Objeto Viaje
        Viaje viajeMock = new Viaje(); // Asume que Viaje tiene un constructor vacío o setters
        // Simula la lista de reservas
        viajeMock.setReservas(Arrays.asList(reserva1, reserva2)); 

        // Comportamiento del Mock del Repositorio
        when(viajeRepositoryMock.findById(viajeMock.getId())).thenReturn(Optional.of(viajeMock));

        // 2. Ejecución (Act)
        List<Viajero> viajeros = servicioValoracion.obtenerViajeros(viajeMock.getId());

        // 3. Verificación y Aserciones (Assert) - Usando Hamcrest
        assertThat("La lista no debe ser nula", viajeros, is(notNullValue()));
        assertThat("La lista debe contener 2 viajeros", viajeros, hasSize(2));
        assertThat("La lista debe contener a viajero1", viajeros, hasItem(viajero1));
        assertThat("La lista debe contener a viajero2", viajeros, hasItem(viajero2));

        // Verifica que se llamó al repositorio
        verify(viajeRepositoryMock, times(1)).findById(viajeMock.getId());
    }

    @Test
    void obtenerViajeros_sinReservas_debeRetornarListaVacia() throws ViajeNoEncontradoException {
        // 1. Configuración de Mocks (Arrange)
        Viaje viajeMock = new Viaje();
        viajeMock.setReservas(new ArrayList<>()); // Viaje sin reservas

        when(viajeRepositoryMock.findById(viajeMock.getId())).thenReturn(Optional.of(viajeMock));

        // 2. Ejecución (Act)
        List<Viajero> viajeros = servicioValoracion.obtenerViajeros(viajeMock.getId());

        // 3. Verificación y Aserciones (Assert) - Usando Hamcrest
        assertThat("La lista no debe ser nula", viajeros, is(notNullValue()));
        assertThat("La lista debe estar vacía", viajeros, is(empty()));
        assertThat("La lista debe estar vacía (alternativa)", viajeros, hasSize(0));

        verify(viajeRepositoryMock, times(1)).findById(viajeMock.getId());
    }
    
}
