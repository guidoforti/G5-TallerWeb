/* 

package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.IRepository.RepositorioUsuario;
import com.tallerwebi.dominio.IServicio.ServicioValoracion;
import com.tallerwebi.dominio.excepcion.DatoObligatorioException;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.presentacion.Controller.ControladorValoracion;
import com.tallerwebi.presentacion.DTO.InputsDTO.ValoracionNuevaInputDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ValoracionOutputDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

public class ControladorValoracionTest {

    private ControladorValoracion controladorValoracion;
    private ServicioValoracion servicioValoracionMock;
    private RepositorioUsuario repositorioUsuarioMock;
    private HttpSession sessionMock;

    @BeforeEach
    public void init() {
        servicioValoracionMock = mock(ServicioValoracion.class);
        repositorioUsuarioMock = mock(RepositorioUsuario.class);
        controladorValoracion = new ControladorValoracion(servicioValoracionMock, repositorioUsuarioMock);
        sessionMock = mock(HttpSession.class);
    }

    @Test
    public void deberiaRedirigirALoginSiNoHaySesionAlVerValoraciones() {
        // given
        when(sessionMock.getAttribute("idUsuario")).thenReturn(null);

        // when
        ModelAndView mav = controladorValoracion.verValoraciones(1L, sessionMock);

        // then
        assertThat(mav.getViewName(), equalTo("redirect:/login"));
        verifyNoInteractions(repositorioUsuarioMock, servicioValoracionMock);
    }

    @Test
    public void deberiaMostrarErrorSiUsuarioInexistente() {
        // given
        when(sessionMock.getAttribute("idUsuario")).thenReturn(10L);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(Optional.empty());

        // when
        ModelAndView mav = controladorValoracion.verValoraciones(1L, sessionMock);

        // then
        assertThat(mav.getViewName(), equalTo("error"));
        assertThat(mav.getModel().get("error").toString(), containsString("no existe"));
    }

     @Test
    public void deberiaMostrarVistaConValoracionesCorrectamente() throws Exception {
        Long receptorId = 5L;
        when(sessionMock.getAttribute("idUsuario")).thenReturn(10L);

        Conductor receptor = new Conductor();
        receptor.setId(receptorId);
        receptor.setNombre("Carlos Perez");

        ValoracionOutputDTO v1 = new ValoracionOutputDTO(
                11L,
                "Juan",
                "Carlos Perez",
                5,
                "Excelente viaje",
                LocalDate.now().minusDays(2)
        );
        ValoracionOutputDTO v2 = new ValoracionOutputDTO(
                12L,
                "Ana",
                "Carlos Perez",
                4,
                "Muy puntual",
                LocalDate.now().minusDays(5)
        );
        List<ValoracionOutputDTO> valoraciones = Arrays.asList(v1, v2);

        when(repositorioUsuarioMock.buscarPorId(receptorId)).thenReturn(Optional.of(receptor));
        when(servicioValoracionMock.obtenerValoracionesDeUsuario(receptorId)).thenReturn(valoraciones);
        when(servicioValoracionMock.calcularPromedioValoraciones(receptorId)).thenReturn(4.5);

        ModelAndView mav = controladorValoracion.verValoraciones(receptorId, sessionMock);

        assertThat(mav.getViewName(), equalTo("verValoraciones"));
        assertThat(mav.getModel().get("receptor"), equalTo(receptor));
        assertThat((List<?>) mav.getModel().get("valoraciones"), hasSize(2));
        assertThat(mav.getModel().get("promedio"), equalTo(4.5));
        assertThat(mav.getModel().get("valoracionDto"), is(instanceOf(ValoracionNuevaInputDTO.class)));

        verify(servicioValoracionMock).obtenerValoracionesDeUsuario(receptorId);
        verify(servicioValoracionMock).calcularPromedioValoraciones(receptorId);
    }

    @Test
    public void deberiaMostrarErrorGenericoSiServicioLanzaExcepcion() throws Exception {
        // given
        Long receptorId = 2L;
        when(sessionMock.getAttribute("idUsuario")).thenReturn(10L);

        Conductor receptor = new Conductor();
        receptor.setId(receptorId);

        when(repositorioUsuarioMock.buscarPorId(receptorId)).thenReturn(Optional.of(receptor));
        when(servicioValoracionMock.obtenerValoracionesDeUsuario(receptorId)).thenThrow(new RuntimeException("Falla en DB"));

        // when
        ModelAndView mav = controladorValoracion.verValoraciones(receptorId, sessionMock);

        // then
        assertThat(mav.getViewName(), equalTo("error"));
        assertThat(mav.getModel().get("error").toString(), containsString("Error al cargar las valoraciones"));
    }

    @Test
    public void deberiaRedirigirALoginSiNoHaySesionAlEnviarValoracion() {
        // given
        when(sessionMock.getAttribute("idUsuario")).thenReturn(null);

        // when
        ModelAndView mav = controladorValoracion.enviarValoracion(new ValoracionNuevaInputDTO(), sessionMock);

        // then
        assertThat(mav.getViewName(), equalTo("redirect:/login"));
    }

    @Test
    public void deberiaRedirigirALoginSiEmisorNoExiste() {
        // given
        when(sessionMock.getAttribute("idUsuario")).thenReturn(99L);
        when(repositorioUsuarioMock.buscarPorId(99L)).thenReturn(Optional.empty());

        ValoracionNuevaInputDTO dto = new ValoracionNuevaInputDTO();

        // when
        ModelAndView mav = controladorValoracion.enviarValoracion(dto, sessionMock);

        // then
        verify(sessionMock).invalidate();
        assertThat(mav.getViewName(), equalTo("redirect:/login"));
    }

    @Test
    public void deberiaRedirigirAlPerfilReceptorTrasValoracionExitosa() throws Exception {
        // given
        Long emisorId = 1L;
        when(sessionMock.getAttribute("idUsuario")).thenReturn(emisorId);

        Viajero emisor = new Viajero();
        emisor.setId(emisorId);

        ValoracionNuevaInputDTO dto = new ValoracionNuevaInputDTO();
        dto.setReceptorId(5L);
        dto.setComentario("Muy buen viaje");
        dto.setPuntuacion(5);

        when(repositorioUsuarioMock.buscarPorId(emisorId)).thenReturn(Optional.of(emisor));
        doNothing().when(servicioValoracionMock).valorarUsuario(emisor, dto);

        // when
        ModelAndView mav = controladorValoracion.enviarValoracion(dto, sessionMock);

        // then
        assertThat(mav.getViewName(), equalTo("redirect:/valoraciones/" + dto.getReceptorId()));
        verify(servicioValoracionMock, times(1)).valorarUsuario(emisor, dto);
    }

    @Test
    public void deberiaMostrarVistaConErrorDatoObligatorio() throws Exception {
        // given
        Long emisorId = 1L;
        when(sessionMock.getAttribute("idUsuario")).thenReturn(emisorId);

        Viajero emisor = new Viajero();
        emisor.setId(emisorId);

        ValoracionNuevaInputDTO dto = new ValoracionNuevaInputDTO();
        dto.setReceptorId(3L);

        when(repositorioUsuarioMock.buscarPorId(emisorId)).thenReturn(Optional.of(emisor));
        doThrow(new DatoObligatorioException("El comentario es obligatorio"))
                .when(servicioValoracionMock).valorarUsuario(emisor, dto);

        // when
        ModelAndView mav = controladorValoracion.enviarValoracion(dto, sessionMock);

        // then
        assertThat(mav.getViewName(), equalTo("verValoraciones"));
        assertThat(mav.getModel().get("error").toString(), containsString("obligatorio"));
        assertThat(mav.getModel().get("valoracionDto"), equalTo(dto));
    }

    @Test
    public void deberiaMostrarVistaConErrorUsuarioInexistente() throws Exception {
        // given
        Long emisorId = 1L;
        when(sessionMock.getAttribute("idUsuario")).thenReturn(emisorId);

        Viajero emisor = new Viajero();
        emisor.setId(emisorId);

        ValoracionNuevaInputDTO dto = new ValoracionNuevaInputDTO();
        dto.setReceptorId(99L);

        when(repositorioUsuarioMock.buscarPorId(emisorId)).thenReturn(Optional.of(emisor));
        doThrow(new UsuarioInexistente("No se encontró el usuario receptor"))
                .when(servicioValoracionMock).valorarUsuario(emisor, dto);

        // when
        ModelAndView mav = controladorValoracion.enviarValoracion(dto, sessionMock);

        // then
        assertThat(mav.getViewName(), equalTo("verValoraciones"));
        assertThat(mav.getModel().get("error").toString(), containsString("receptor"));
    }

    @Test
    public void deberiaMostrarErrorGenericoSiFallaAlEnviarValoracion() throws Exception {
        // given
        Long emisorId = 1L;
        when(sessionMock.getAttribute("idUsuario")).thenReturn(emisorId);

        Conductor emisor = new Conductor();
        emisor.setId(emisorId);

        ValoracionNuevaInputDTO dto = new ValoracionNuevaInputDTO();
        dto.setReceptorId(4L);

        when(repositorioUsuarioMock.buscarPorId(emisorId)).thenReturn(Optional.of(emisor));
        doThrow(new RuntimeException("Error de base de datos"))
                .when(servicioValoracionMock).valorarUsuario(emisor, dto);

        // when
        ModelAndView mav = controladorValoracion.enviarValoracion(dto, sessionMock);

        // then
        assertThat(mav.getViewName(), equalTo("error"));
        assertThat(mav.getModel().get("error").toString(), containsString("Error al enviar la valoración"));
    }
}

*/
