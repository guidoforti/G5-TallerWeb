package com.tallerwebi.dominio; 

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.Entity.Valoracion;
import com.tallerwebi.dominio.IRepository.RepositorioUsuario;
import com.tallerwebi.dominio.IRepository.RepositorioValoracion;
import com.tallerwebi.dominio.IRepository.ViajeRepository;
import com.tallerwebi.dominio.IServicio.ServicioValoracion;
import com.tallerwebi.dominio.ServiceImpl.ServicioValoracionImpl;
import com.tallerwebi.dominio.excepcion.DatoObligatorioException;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.presentacion.DTO.InputsDTO.ValoracionNuevaInputDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ValoracionOutputDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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
    private ViajeRepository viajeRepositoryMock; 
    private ServicioValoracion servicioValoracion;
    private Usuario receptorDummy; // Usuario genérico para asegurar que la búsqueda siempre devuelva algo.

    @BeforeEach
    void setUp() {
        repositorioValoracionMock = mock(RepositorioValoracion.class);
        repositorioUsuarioMock = mock(RepositorioUsuario.class);
        viajeRepositoryMock = mock(ViajeRepository.class); 
        receptorDummy = crearUsuario(99L); 
        
        // 1. Configuración por defecto para ViajeRepository: asume que el viaje ES valorable.
        // Esto permite que los tests de Puntuación/Comentario lleguen a su propia validación
        when(viajeRepositoryMock.existeViajeFinalizadoYNoValorado(anyLong(), anyLong())).thenReturn(true);
        
        // 2. Configuración por defecto para RepositorioUsuario: asume que el usuario receptor existe.
        // Esto evita que los tests de DatoObligatorioException fallen por UsuarioInexistente.
        when(repositorioUsuarioMock.buscarPorId(anyLong())).thenReturn(Optional.of(receptorDummy));

        // Inicialización del servicio con los mocks
        servicioValoracion = new ServicioValoracionImpl(
            repositorioValoracionMock, 
            repositorioUsuarioMock, 
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

    // =================================================================
    // TESTS PARA: valorarUsuario(Usuario emisor, ValoracionNuevaInputDTO dto)
    // =================================================================

    @Test
    void deberiaGuardarUnaValoracionCorrectamente() throws Exception {
        // given
        Usuario emisor = crearUsuario(1L);
        Usuario receptor = crearUsuario(2L);
        ValoracionNuevaInputDTO dto = new ValoracionNuevaInputDTO();
        dto.setReceptorId(2L);
        dto.setPuntuacion(5);
        dto.setComentario("Excelente servicio y puntualidad.");

        // Anulamos el mock genérico del setUp solo para simular un receptor específico
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

    // --- TESTS DE EXCEPCIONES DE VALIDACIÓN DE DATOS ---

    @Test
    void noDeberiaPermitirAutoValoracion() {
        // given
        Usuario usuario = crearUsuario(1L);
        ValoracionNuevaInputDTO dto = new ValoracionNuevaInputDTO();
        dto.setReceptorId(1L); // El receptor es el mismo que el emisor
        dto.setPuntuacion(4);
        dto.setComentario("Me valoro a mi mismo.");

        // when & then
        DatoObligatorioException exception = assertThrows(
                DatoObligatorioException.class,
                () -> servicioValoracion.valorarUsuario(usuario, dto)
        );

        assertThat(exception.getMessage(), is("Error. No podes valorarte a vos mismo"));
        // Verifica que no se llamó a ninguna dependencia (falla antes)
        verify(viajeRepositoryMock, never()).existeViajeFinalizadoYNoValorado(anyLong(), anyLong());
        verify(repositorioUsuarioMock, never()).buscarPorId(anyLong()); 
        verify(repositorioValoracionMock, never()).save(any());
    }

    @Test
    void deberiaLanzarExcepcionSiPuntuacionEsNula() {
        // given
        Usuario emisor = crearUsuario(1L);
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
        Usuario emisor = crearUsuario(1L);
        ValoracionNuevaInputDTO dto = new ValoracionNuevaInputDTO();
        dto.setReceptorId(2L);
        dto.setPuntuacion(0); // rango 1-5
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
        Usuario emisor = crearUsuario(1L);
        ValoracionNuevaInputDTO dto = new ValoracionNuevaInputDTO();
        dto.setReceptorId(2L);
        dto.setPuntuacion(6); // Rango: 1-5
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
        Usuario emisor = crearUsuario(1L);
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
        Usuario emisor = crearUsuario(1L);
        ValoracionNuevaInputDTO dto = new ValoracionNuevaInputDTO();
        dto.setReceptorId(2L);
        dto.setPuntuacion(4);
        dto.setComentario("  "); // solo espacios

        // whenthen
        DatoObligatorioException exception = assertThrows(
                DatoObligatorioException.class,
                () -> servicioValoracion.valorarUsuario(emisor, dto)
        );
        assertThat(exception.getMessage(), is("El comentario es obligatorio"));
        verify(repositorioValoracionMock, never()).save(any());
    }

    // --- TEST DE EXCEPCIÓN DE VALIDACIÓN DE VIAJE ---

    @Test
    void deberiaLanzarExcepcionSiNoExisteViajeFinalizadoParaValorar() {
        // given
        Usuario emisor = crearUsuario(1L);
        ValoracionNuevaInputDTO dto = new ValoracionNuevaInputDTO();
        dto.setReceptorId(2L);
        dto.setPuntuacion(5);
        dto.setComentario("Comentario OK.");

        // ANULACIÓN: Forzamos el fallo de viaje
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

    // --- TEST DE EXCEPCIÓN DE DEPENDENCIA (USUARIO INEXISTENTE) ---

    @Test
    void deberiaLanzarExcepcionSiUsuarioReceptorNoExiste() {
        // given
        Usuario emisor = crearUsuario(1L);
        ValoracionNuevaInputDTO dto = new ValoracionNuevaInputDTO();
        dto.setReceptorId(99L);
        dto.setPuntuacion(5);
        dto.setComentario("Comentario OK.");

        // ANULACIÓN: Forzamos la devolución de Optional.empty() para este ID
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
    void deberiaObtenerYMapearValoracionesDeUsuario() {
        // given
        Usuario receptor = crearUsuario(2L);
        Usuario emisor1 = crearUsuario(1L);
        Usuario emisor3 = crearUsuario(3L);
        
        // crear entidades valoracion
        Valoracion v1 = new Valoracion(emisor1, receptor, 5, "Muy bueno");
        Valoracion v2 = new Valoracion(emisor3, receptor, 3, "Regular");

        when(repositorioValoracionMock.findByReceptorId(2L)).thenReturn(Arrays.asList(v1, v2));

        // when
        List<ValoracionOutputDTO> dtos = servicioValoracion.obtenerValoracionesDeUsuario(2L);

        // then
        assertThat(dtos, hasSize(2));
        assertThat(dtos.get(0).getPuntuacion(), is(5));
        assertThat(dtos.get(0).getNombreEmisor(), is("Conductor1"));
        assertThat(dtos.get(1).getComentario(), is("Regular"));
    }

    @Test
    void deberiaRetornarListaVaciaSiNoHayValoraciones() {
        // given
        when(repositorioValoracionMock.findByReceptorId(anyLong())).thenReturn(Collections.emptyList());

        // when
        List<ValoracionOutputDTO> dtos = servicioValoracion.obtenerValoracionesDeUsuario(50L);

        // then
        assertThat(dtos, notNullValue());
        assertThat(dtos, empty());
    }

    // =================================================================
    // TESTS PARA: calcularPromedioValoraciones(Long usuarioId)
    // =================================================================

    @Test
    void deberiaCalcularElPromedioCorrectamente() {
        // given
        Usuario receptor = crearUsuario(2L);
        Usuario emisor = crearUsuario(1L);
        
        // Valoraciones: 5, 4, 3 -> Promedio: 12 / 3 = 4.0
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
}