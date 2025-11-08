/* 
package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.IServicio.ServicioValoracion;
import com.tallerwebi.dominio.excepcion.DatoObligatorioException;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.presentacion.Controller.ControladorValoracion;
import com.tallerwebi.presentacion.DTO.InputsDTO.ValoracionNuevaInputDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

public class ControladorValoracionTest {

    private ControladorValoracion controladorValoracion;
    private ServicioValoracion servicioValoracionMock;
    private HttpSession sessionMock;

    @BeforeEach
    public void init() {
        servicioValoracionMock = mock(ServicioValoracion.class);
        controladorValoracion = new ControladorValoracion(servicioValoracionMock);
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
        verifyNoInteractions(servicioValoracionMock);
    }

    @Test
    public void deberiaMostrarErrorSiViajeroInexistente() throws Exception {
        // given
        when(sessionMock.getAttribute("idUsuario")).thenReturn(10L);
        when(servicioValoracionMock.obtenerViajero(1L)).thenThrow(new UsuarioInexistente("El usuario no existe."));

        // when
        ModelAndView mav = controladorValoracion.verValoraciones(1L, sessionMock);

        // then
        assertThat(mav.getViewName(), equalTo("error"));
        assertThat(mav.getModel().get("error").toString(), containsString("no existe"));
    }

    @Test
    public void deberiaMostrarVistaConFormularioCorrectamente() throws Exception {
        // given
        Long receptorId = 5L;
        when(sessionMock.getAttribute("idUsuario")).thenReturn(10L);

        Viajero receptor = new Viajero();
        receptor.setId(receptorId);
        receptor.setNombre("Juan Perez");
        receptor.setEmail("juan@email.com");

        when(servicioValoracionMock.obtenerViajero(receptorId)).thenReturn(receptor);

        // when
        ModelAndView mav = controladorValoracion.verValoraciones(receptorId, sessionMock);

        // then
        assertThat(mav.getViewName(), equalTo("valorarViajero"));
        assertThat(mav.getModel().get("receptor"), equalTo(receptor));
        assertThat(mav.getModel().get("valoracionDto"), is(instanceOf(ValoracionNuevaInputDTO.class)));

        verify(servicioValoracionMock).obtenerViajero(receptorId);
    }

    @Test
    public void deberiaMostrarErrorGenericoSiServicioLanzaExcepcion() throws Exception {
        // given
        Long receptorId = 2L;
        when(sessionMock.getAttribute("idUsuario")).thenReturn(10L);

        when(servicioValoracionMock.obtenerViajero(receptorId)).thenThrow(new RuntimeException("Error DB"));

        // when
        ModelAndView mav = controladorValoracion.verValoraciones(receptorId, sessionMock);

        // then
        assertThat(mav.getViewName(), equalTo("error"));
        assertThat(mav.getModel().get("error").toString(), containsString("Error al cargar el perfil"));
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
    public void deberiaRedirigirAHomeTrasValoracionExitosa() throws Exception {
        // given
        Long emisorId = 1L;
        when(sessionMock.getAttribute("idUsuario")).thenReturn(emisorId);

        Viajero emisor = new Viajero();
        emisor.setId(emisorId);

        ValoracionNuevaInputDTO dto = new ValoracionNuevaInputDTO();
        dto.setReceptorId(5L);
        dto.setComentario("Muy buen viaje");
        dto.setPuntuacion(5);

        when(servicioValoracionMock.obtenerUsuario(emisorId)).thenReturn(emisor);
        doNothing().when(servicioValoracionMock).valorarUsuario(emisor, dto);

        // when
        ModelAndView mav = controladorValoracion.enviarValoracion(dto, sessionMock);

        // then
        assertThat(mav.getViewName(), equalTo("redirect:/conductor/home"));
        verify(servicioValoracionMock, times(1)).valorarUsuario(emisor, dto);
    }

    @Test
    public void deberiaMostrarVistaConErrorDatoObligatorio() throws Exception {
        // given
        Long emisorId = 1L;
        when(sessionMock.getAttribute("idUsuario")).thenReturn(emisorId);

        Viajero emisor = new Viajero();
        emisor.setId(emisorId);

        Viajero receptor = new Viajero();
        receptor.setId(3L);
        receptor.setNombre("Receptor");

        ValoracionNuevaInputDTO dto = new ValoracionNuevaInputDTO();
        dto.setReceptorId(3L);

        when(servicioValoracionMock.obtenerUsuario(emisorId)).thenReturn(emisor);
        when(servicioValoracionMock.obtenerViajero(3L)).thenReturn(receptor);
        doThrow(new DatoObligatorioException("El comentario es obligatorio"))
                .when(servicioValoracionMock).valorarUsuario(emisor, dto);

        // when
        ModelAndView mav = controladorValoracion.enviarValoracion(dto, sessionMock);

        // then
        assertThat(mav.getViewName(), equalTo("valorarViajero"));
        assertThat(mav.getModel().get("error").toString(), containsString("obligatorio"));
        assertThat(mav.getModel().get("valoracionDto"), equalTo(dto));
        assertThat(mav.getModel().get("receptor"), equalTo(receptor));
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

        when(servicioValoracionMock.obtenerUsuario(emisorId)).thenReturn(emisor);
        doThrow(new UsuarioInexistente("No se encontró el usuario receptor"))
                .when(servicioValoracionMock).valorarUsuario(emisor, dto);

        // when
        ModelAndView mav = controladorValoracion.enviarValoracion(dto, sessionMock);

        // then
        assertThat(mav.getViewName(), equalTo("valorarViajero"));
        assertThat(mav.getModel().get("error").toString(), containsString("receptor"));
    }

    @Test
    public void deberiaMostrarErrorGenericoSiFallaAlEnviarValoracion() throws Exception {
        // given
        Long emisorId = 1L;
        when(sessionMock.getAttribute("idUsuario")).thenReturn(emisorId);

        Viajero emisor = new Viajero();
        emisor.setId(emisorId);

        ValoracionNuevaInputDTO dto = new ValoracionNuevaInputDTO();
        dto.setReceptorId(4L);

        when(servicioValoracionMock.obtenerUsuario(emisorId)).thenReturn(emisor);
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
