package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.IServicio.ServicioValoracion;
import com.tallerwebi.dominio.IRepository.RepositorioUsuario;
import com.tallerwebi.dominio.excepcion.DatoObligatorioException;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.presentacion.Controller.ControladorValoracion;
import com.tallerwebi.presentacion.DTO.InputsDTO.ValoracionNuevaInputDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ValoracionOutputDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ControladorValoracionTest {

    private ServicioValoracion servicioValoracionMock;
    private RepositorioUsuario repositorioUsuarioMock;
    private ControladorValoracion controlador;
    private HttpSession sessionMock;
    private RedirectAttributes redirectAttributesMock;

    @BeforeEach
    void init() {
        servicioValoracionMock = mock(ServicioValoracion.class);
        repositorioUsuarioMock = mock(RepositorioUsuario.class);
        sessionMock = mock(HttpSession.class);
        redirectAttributesMock = mock(RedirectAttributes.class);

        controlador = new ControladorValoracion(servicioValoracionMock, repositorioUsuarioMock);
    }


    @Test
    void deberiaMostrarValoracionesDeUsuarioCorrectamente() {
        Long receptorId = 1L;
        Conductor receptor = new Conductor();
        receptor.setId(receptorId);

        List<ValoracionOutputDTO> valoraciones = Arrays.asList(
    new ValoracionOutputDTO(1L, "Juan", "Pedro", 5, "Muy buen viaje", LocalDate.now()),
    new ValoracionOutputDTO(2L, "Ana", "Pedro", 4, "Excelente conductor", LocalDate.now())
);


        when(repositorioUsuarioMock.buscarPorId(receptorId)).thenReturn(Optional.of(receptor));
        when(servicioValoracionMock.obtenerValoracionesDeUsuario(receptorId)).thenReturn(valoraciones);
        when(servicioValoracionMock.calcularPromedioValoraciones(receptorId)).thenReturn(4.5);

        ModelAndView mav = controlador.verValoraciones(receptorId, "ok", "");

        assertThat(mav.getViewName(), equalTo("verValoraciones"));
        assertThat(mav.getModel().get("receptorId"), equalTo(receptorId));
        assertThat(mav.getModel().get("valoraciones"), equalTo(valoraciones));
        assertThat(mav.getModel().get("promedio"), equalTo(4.5));
        assertThat(mav.getModel().get("success"), equalTo("ok"));
    }

    @Test
    void deberiaMostrarErrorSiUsuarioNoExiste() {
        Long receptorId = 99L;
        when(repositorioUsuarioMock.buscarPorId(receptorId)).thenReturn(Optional.empty());

        ModelAndView mav = controlador.verValoraciones(receptorId, "", "");

        assertThat(mav.getViewName(), equalTo("error"));
        assertThat((String) mav.getModel().get("error"), containsString("Usuario no encontrado"));
    }

    @Test
    void deberiaMostrarErrorSiServicioLanzaExcepcionInesperada() {
        Long receptorId = 5L;
        Conductor receptor = new Conductor();
        receptor.setId(receptorId);

        when(repositorioUsuarioMock.buscarPorId(receptorId)).thenReturn(Optional.of(receptor));
        when(servicioValoracionMock.obtenerValoracionesDeUsuario(receptorId))
                .thenThrow(new RuntimeException("fallo"));

        ModelAndView mav = controlador.verValoraciones(receptorId, "", "");

        assertThat(mav.getViewName(), equalTo("error"));
        assertThat((String) mav.getModel().get("error"), containsString("Error al cargar las valoraciones"));
    }

    @Test
    void deberiaRedirigirALoginSiNoHaySesion() {
        when(sessionMock.getAttribute("idUsuario")).thenReturn(null);

        ValoracionNuevaInputDTO dto = new ValoracionNuevaInputDTO();
        ModelAndView mav = controlador.enviarValoracion(dto, sessionMock, redirectAttributesMock);

        assertThat(mav.getViewName(), equalTo("redirect:/login"));
    }

    @Test
    void deberiaRedirigirALoginSiUsuarioNoExisteEnRepo() {
        Long emisorId = 1L;
        when(sessionMock.getAttribute("idUsuario")).thenReturn(emisorId);
        when(repositorioUsuarioMock.buscarPorId(emisorId)).thenReturn(Optional.empty());

        ValoracionNuevaInputDTO dto = new ValoracionNuevaInputDTO();
        ModelAndView mav = controlador.enviarValoracion(dto, sessionMock, redirectAttributesMock);

        verify(sessionMock, times(1)).invalidate();
        assertThat(mav.getViewName(), equalTo("redirect:/login"));
    }

    @Test
    void deberiaEnviarValoracionExitosamente() throws Exception {
        Long emisorId = 1L;
        Long receptorId = 2L;

        Conductor emisor = new Conductor();
        emisor.setId(emisorId);

        ValoracionNuevaInputDTO dto = new ValoracionNuevaInputDTO();
        dto.setReceptorId(receptorId);
        dto.setComentario("Buen viaje");
        dto.setPuntuacion(5);

        when(sessionMock.getAttribute("idUsuario")).thenReturn(emisorId);
        when(repositorioUsuarioMock.buscarPorId(emisorId)).thenReturn(Optional.of(emisor));
        doNothing().when(servicioValoracionMock).valorarUsuario(eq(emisor), eq(dto));

        ModelAndView mav = controlador.enviarValoracion(dto, sessionMock, redirectAttributesMock);

        verify(servicioValoracionMock, times(1)).valorarUsuario(emisor, dto);
        verify(redirectAttributesMock, times(1)).addFlashAttribute("success", "¡Valoración enviada con éxito!");
        assertThat(mav.getViewName(), equalTo("redirect:/valoraciones/" + receptorId));
    }

    @Test
    void deberiaCapturarDatoObligatorioExceptionYRedirigirConError() throws Exception {
        Long emisorId = 1L;
        Long receptorId = 2L;

        Conductor emisor = new Conductor();
        emisor.setId(emisorId);

        ValoracionNuevaInputDTO dto = new ValoracionNuevaInputDTO();
        dto.setReceptorId(receptorId);

        when(sessionMock.getAttribute("idUsuario")).thenReturn(emisorId);
        when(repositorioUsuarioMock.buscarPorId(emisorId)).thenReturn(Optional.of(emisor));
        doThrow(new DatoObligatorioException("Comentario obligatorio"))
                .when(servicioValoracionMock).valorarUsuario(any(), any());

        ModelAndView mav = controlador.enviarValoracion(dto, sessionMock, redirectAttributesMock);

        verify(redirectAttributesMock).addFlashAttribute("error", "Comentario obligatorio");
        assertThat(mav.getViewName(), equalTo("redirect:/valoraciones/" + receptorId));
    }

    @Test
    void deberiaCapturarUsuarioInexistenteYRedirigirConError() throws Exception {
        Long emisorId = 1L;
        Long receptorId = 2L;

        Conductor emisor = new Conductor();
        emisor.setId(emisorId);

        ValoracionNuevaInputDTO dto = new ValoracionNuevaInputDTO();
        dto.setReceptorId(receptorId);

        when(sessionMock.getAttribute("idUsuario")).thenReturn(emisorId);
        when(repositorioUsuarioMock.buscarPorId(emisorId)).thenReturn(Optional.of(emisor));
        doThrow(new UsuarioInexistente("Receptor no encontrado"))
                .when(servicioValoracionMock).valorarUsuario(any(), any());

        ModelAndView mav = controlador.enviarValoracion(dto, sessionMock, redirectAttributesMock);

        verify(redirectAttributesMock).addFlashAttribute("error", "Receptor no encontrado");
        assertThat(mav.getViewName(), equalTo("redirect:/valoraciones/" + receptorId));
    }

    @Test
    void deberiaCapturarExcepcionInesperadaYRedirigirConErrorGenerico() throws Exception {
        Long emisorId = 1L;
        Long receptorId = 2L;

        Conductor emisor = new Conductor();
        emisor.setId(emisorId);

        ValoracionNuevaInputDTO dto = new ValoracionNuevaInputDTO();
        dto.setReceptorId(receptorId);

        when(sessionMock.getAttribute("idUsuario")).thenReturn(emisorId);
        when(repositorioUsuarioMock.buscarPorId(emisorId)).thenReturn(Optional.of(emisor));
        doThrow(new RuntimeException("error inesperado"))
                .when(servicioValoracionMock).valorarUsuario(any(), any());

        ModelAndView mav = controlador.enviarValoracion(dto, sessionMock, redirectAttributesMock);

        verify(redirectAttributesMock).addFlashAttribute("error", "Error inesperado al enviar la valoración.");
        assertThat(mav.getViewName(), equalTo("redirect:/valoraciones/" + receptorId));
    }
}
