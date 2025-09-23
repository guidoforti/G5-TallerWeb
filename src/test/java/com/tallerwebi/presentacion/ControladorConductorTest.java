package com.tallerwebi.presentacion;
import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import com.tallerwebi.dominio.excepcion.CredencialesInvalidas;
import com.tallerwebi.dominio.excepcion.FechaDeVencimientoDeLicenciaInvalida;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.presentacion.Controller.ControladorConductor;
import com.tallerwebi.presentacion.DTO.ConductorLoginDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.mockito.Mockito.*;

public class ControladorConductorTest {

    private ControladorConductor controladorConductor;
    private ServicioConductor servicioConductorMock;
    private ConductorLoginDTO loginDTO;
    private HttpSession sessionMock;
    private Conductor conductorMock;

    @BeforeEach
    public void init() {
        servicioConductorMock = mock(ServicioConductor.class);
        controladorConductor = new ControladorConductor(servicioConductorMock);
        loginDTO = new ConductorLoginDTO("conductor@mail.com", "1234");
        sessionMock = mock(HttpSession.class);
        conductorMock = mock(Conductor.class);
        when(conductorMock.getId()).thenReturn(1L);
    }

    @Test
    public void loginConCredencialesCorrectasDeberiaRedirigirAHomeYSetearSesion() throws CredencialesInvalidas {
        // preparación
        when(servicioConductorMock.login(loginDTO.getEmail(), loginDTO.getContrasenia()))
                .thenReturn(conductorMock);

        // ejecución
        ModelAndView modelAndView = controladorConductor.validarLogin(loginDTO, sessionMock);

        // validación
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
        verify(sessionMock, times(1)).setAttribute("usuarioId", conductorMock.getId());
        verify(sessionMock, times(1)).setAttribute("rol", "CONDUCTOR");
    }

    @Test
    public void loginConCredencialesInvalidasDeberiaVolverALoginConError() throws CredencialesInvalidas {
        // preparación
        when(servicioConductorMock.login(loginDTO.getEmail(), loginDTO.getContrasenia()))
                .thenThrow(new CredencialesInvalidas("Email o contraseña inválidos"));

        // ejecución
        ModelAndView modelAndView = controladorConductor.validarLogin(loginDTO, sessionMock);

        // validación
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("loginConductor"));
        assertThat(modelAndView.getModel().get("error").toString(), equalToIgnoringCase("Email o contraseña inválidos"));
        verify(sessionMock, times(0)).setAttribute(eq("usuarioId"), any());
    }


    @Test
    void siUsuarioYaEstaLogueadoDeberiaRedirigirAHome() {
        // given
        when(sessionMock.getAttribute("usuarioId")).thenReturn(1L);

        // when
        ModelAndView mav = controladorConductor.irALogin(sessionMock);

        // then
        assertThat(mav.getViewName(), equalTo("redirect:/home"));
        verify(sessionMock, times(1)).getAttribute("usuarioId");
    }

    @Test
    void siUsuarioNoEstaLogueadoDeberiaMostrarLogin() {
        // given
        when(sessionMock.getAttribute("usuarioId")).thenReturn(null);

        // when
        ModelAndView mav = controladorConductor.irALogin(sessionMock);

        // then
        assertThat(mav.getViewName(), equalTo("loginConductor"));
        assertThat(mav.getModel().containsKey("datosLogin"), equalTo(true));
    }

    @Test
    public void irARegistroDeberiaMostrarFormulario() {
        // when
        ModelAndView mav = controladorConductor.irARegistro();

        // then
        assertThat(mav.getViewName(), equalTo("registroConductor"));
        assertThat(mav.getModel().containsKey("datosConductor"), equalTo(true));
    }

    @Test
    public void registroCorrectoDeberiaRedirigirAHomeYSetearSesion() throws UsuarioExistente, FechaDeVencimientoDeLicenciaInvalida {
        // preparación
        Conductor nuevoConductor = new Conductor(null, null, "Ana", "ana@mail.com", "123", LocalDate.now());
        when(servicioConductorMock.registrar(nuevoConductor)).thenReturn(nuevoConductor);

        // ejecución
        ModelAndView mav = controladorConductor.registrar(nuevoConductor, sessionMock);

        // validación
        assertThat(mav.getViewName(), equalTo("redirect:/home"));
        verify(sessionMock, times(1)).setAttribute("usuarioId", nuevoConductor.getId());
        verify(sessionMock, times(1)).setAttribute("rol", "CONDUCTOR");
    }

    @Test
    public void registroConEmailExistenteDeberiaVolverAFormularioConError() throws UsuarioExistente, FechaDeVencimientoDeLicenciaInvalida {
        // preparación
        Conductor nuevoConductor = new Conductor(null, null, "Ana", "ana@mail.com", "123", LocalDate.now());
        doThrow(new UsuarioExistente("Ya existe un usuario con ese email"))
                .when(servicioConductorMock).registrar(nuevoConductor);

        // ejecución
        ModelAndView mav = controladorConductor.registrar(nuevoConductor, sessionMock);

        // validación
        assertThat(mav.getViewName(), equalTo("registroConductor"));
        assertThat(mav.getModel().get("error").toString(),
                equalToIgnoringCase("Ya existe un usuario con ese email"));
        verify(sessionMock, times(0)).setAttribute(eq("usuarioId"), any());
    }

}