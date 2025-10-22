package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.IServicio.ServicioViajero;
import com.tallerwebi.dominio.excepcion.CredencialesInvalidas;
import com.tallerwebi.dominio.excepcion.DatoObligatorioException;
import com.tallerwebi.dominio.excepcion.EdadInvalidaException;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.presentacion.Controller.ControladorViajero;
import com.tallerwebi.presentacion.DTO.InputsDTO.ViajeroLoginInputDTO;
import com.tallerwebi.presentacion.DTO.InputsDTO.ViajeroRegistroInputDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ControladorViajeroTest {

    private ControladorViajero controladorViajero;
    private ServicioViajero servicioViajeroMock;
    private HttpSession sessionMock;
    private Viajero viajeroMock;
    private ViajeroLoginInputDTO loginDTO;

    @BeforeEach
    public void init() {
        servicioViajeroMock = mock(ServicioViajero.class);
        controladorViajero = new ControladorViajero(servicioViajeroMock);
        sessionMock = mock(HttpSession.class);
        viajeroMock = mock(Viajero.class);
        loginDTO = new ViajeroLoginInputDTO("viajero@mail.com", "1234");

        when(viajeroMock.getId()).thenReturn(1L);
        when(viajeroMock.getNombre()).thenReturn("Juan");
    }


    @Test
    public void irARegistroDeberiaMostrarFormulario() {
        ModelAndView mav = controladorViajero.irARegistro();

        assertThat(mav.getViewName(), equalTo("registroViajero"));
        assertThat(mav.getModel().containsKey("datosViajero"), equalTo(true));
    }

    @Test
    public void registroCorrectoDeberiaRedirigirAHomeYSetearSesion() throws UsuarioExistente, EdadInvalidaException, DatoObligatorioException {
        ViajeroRegistroInputDTO inputDTO = mock(ViajeroRegistroInputDTO.class);
        Viajero viajeroNuevo = new Viajero();
        viajeroNuevo.setId(2L);

        when(inputDTO.toEntity()).thenReturn(viajeroNuevo);
        when(servicioViajeroMock.registrar(viajeroNuevo)).thenReturn(viajeroNuevo);

        ModelAndView mav = controladorViajero.registrar(inputDTO, sessionMock);

        assertThat(mav.getViewName(), equalTo("redirect:/viajero/home"));
        verify(sessionMock, times(1)).setAttribute("usuarioId", viajeroNuevo.getId());
        verify(sessionMock, times(1)).setAttribute("rol", "VIAJERO");
    }

    @Test
    public void registroConEmailExistenteDeberiaVolverAFormularioConError() throws UsuarioExistente, EdadInvalidaException, DatoObligatorioException {
        ViajeroRegistroInputDTO inputDTO = mock(ViajeroRegistroInputDTO.class);
        Viajero viajeroNuevo = new Viajero();
        viajeroNuevo.setId(3L);

        when(inputDTO.toEntity()).thenReturn(viajeroNuevo);
        doThrow(new UsuarioExistente("Ya existe un usuario con ese email"))
                .when(servicioViajeroMock).registrar(any(Viajero.class));

        ModelAndView mav = controladorViajero.registrar(inputDTO, sessionMock);

        assertThat(mav.getViewName(), equalTo("registroViajero"));
        assertThat(mav.getModel().get("error").toString(), equalToIgnoringCase("Ya existe un usuario con ese email"));
    }

    @Test
    void siUsuarioNoEstaEnSesionEnHomeDeberiaRedirigirALogin() {
        when(sessionMock.getAttribute("usuarioId")).thenReturn(null);

        ModelAndView mav = controladorViajero.irAHome(sessionMock);

        assertThat(mav.getViewName(), equalTo("redirect:/viajero/login"));
    }

    @Test
    void siUsuarioEstaEnSesionEnHomeDeberiaMostrarHomeConNombre() throws UsuarioInexistente {
        when(sessionMock.getAttribute("usuarioId")).thenReturn(1L);
        when(servicioViajeroMock.obtenerViajero(1L)).thenReturn(viajeroMock);

        ModelAndView mav = controladorViajero.irAHome(sessionMock);

        assertThat(mav.getViewName(), equalTo("homeViajero"));
        assertThat(mav.getModel().get("nombreViajero").toString(), equalTo(viajeroMock.getNombre()));
    }



    @Test
    public void registroConEdadInvalidaDeberiaVolverAFormularioConError() throws UsuarioExistente, EdadInvalidaException, DatoObligatorioException {
        ViajeroRegistroInputDTO inputDTO = mock(ViajeroRegistroInputDTO.class);
        Viajero viajeroNuevo = new Viajero();
        viajeroNuevo.setId(10L);

        when(inputDTO.toEntity()).thenReturn(viajeroNuevo);

    
        doThrow(new EdadInvalidaException("La edad mínima es 18 años")).when(servicioViajeroMock).registrar(any(Viajero.class));

    
        ModelAndView mav = controladorViajero.registrar(inputDTO, sessionMock);

    
        assertThat(mav.getViewName(), equalTo("registroViajero"));
        assertThat(mav.getModel().get("error").toString(), equalToIgnoringCase("La edad mínima es 18 años"));
        verify(sessionMock, times(0)).setAttribute(eq("usuarioId"), any());
}

}
