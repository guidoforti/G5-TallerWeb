package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import com.tallerwebi.dominio.excepcion.CredencialesInvalidas;
import com.tallerwebi.dominio.excepcion.FechaDeVencimientoDeLicenciaInvalida;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.presentacion.Controller.ControladorConductor;
import com.tallerwebi.presentacion.DTO.ConductorLoginDTO;
import com.tallerwebi.presentacion.DTO.InputsDTO.ConductorRegistroInputDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.ArrayList;

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
        when(conductorMock.getNombre()).thenReturn("Pepe");
    }

    @Test
    public void loginConCredencialesCorrectasDeberiaRedirigirAHomeYSetearSesion() throws CredencialesInvalidas {
        // preparación
        when(servicioConductorMock.login(loginDTO.getEmail(), loginDTO.getContrasenia()))
                .thenReturn(conductorMock);

        // ejecución
        ModelAndView modelAndView = controladorConductor.validarLogin(loginDTO, sessionMock);

        // validación
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/conductor/home"));
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
        when(sessionMock.getAttribute("usuarioId")).thenReturn(1L);

        ModelAndView mav = controladorConductor.irALogin(sessionMock);

        assertThat(mav.getViewName(), equalTo("redirect:/conductor/home"));
        verify(sessionMock, times(1)).getAttribute("usuarioId");
    }

    @Test
    void siUsuarioNoEstaLogueadoDeberiaMostrarLogin() {
        when(sessionMock.getAttribute("usuarioId")).thenReturn(null);

        ModelAndView mav = controladorConductor.irALogin(sessionMock);

        assertThat(mav.getViewName(), equalTo("loginConductor"));
        assertThat(mav.getModel().containsKey("datosLogin"), equalTo(true));
    }

    @Test
    public void irARegistroDeberiaMostrarFormulario() {
        ModelAndView mav = controladorConductor.irARegistro();

        assertThat(mav.getViewName(), equalTo("registroConductor"));
        assertThat(mav.getModel().containsKey("datosConductor"), equalTo(true));
    }

    @Test
    public void registroCorrectoDeberiaRedirigirAHomeYSetearSesion() throws UsuarioExistente, FechaDeVencimientoDeLicenciaInvalida {
        Conductor nuevoConductor = new Conductor(null,  "Ana", "ana@mail.com", "123", LocalDate.now(), new ArrayList<>(),new ArrayList<>());
        ConductorRegistroInputDTO inputDTO = new ConductorRegistroInputDTO(
                null, "Ana", "ana@mail.com", "123", LocalDate.now(), null
        );

        when(servicioConductorMock.registrar(any(Conductor.class))).thenReturn(nuevoConductor);

        ModelAndView mav = controladorConductor.registrar(inputDTO, sessionMock);

        assertThat(mav.getViewName(), equalTo("redirect:/conductor/home"));
        verify(sessionMock, times(1)).setAttribute("usuarioId", nuevoConductor.getId());
        verify(sessionMock, times(1)).setAttribute("rol", "CONDUCTOR");
    }

    @Test
    public void registroConEmailExistenteDeberiaVolverAFormularioConError() throws UsuarioExistente, FechaDeVencimientoDeLicenciaInvalida {
        ConductorRegistroInputDTO inputDTO = new ConductorRegistroInputDTO(
                null, "Ana", "ana@mail.com", "123", LocalDate.now(), null
        );

        doThrow(new UsuarioExistente("Ya existe un usuario con ese email"))
                .when(servicioConductorMock).registrar(any(Conductor.class));

        ModelAndView mav = controladorConductor.registrar(inputDTO, sessionMock);

        assertThat(mav.getViewName(), equalTo("registroConductor"));
        assertThat(mav.getModel().get("error").toString(),
                equalToIgnoringCase("Ya existe un usuario con ese email"));
        verify(sessionMock, times(0)).setAttribute(eq("usuarioId"), any());
    }

    @Test
    void siUsuarioNoEstaEnSesionEnHomeDeberiaRedirigirALogin() {
        when(sessionMock.getAttribute("usuarioId")).thenReturn(null);

        ModelAndView mav = controladorConductor.irAHome(sessionMock);

        assertThat(mav.getViewName(), equalTo("redirect:/conductor/login"));
        verify(sessionMock, times(1)).getAttribute("usuarioId");
    }

    @Test
    void siUsuarioEstaEnSesionEnHomeDeberiaMostrarHomeConNombre() throws UsuarioInexistente {
        when(sessionMock.getAttribute("usuarioId")).thenReturn(1L);
        when(servicioConductorMock.obtenerConductor(1L)).thenReturn(conductorMock);

        ModelAndView mav = controladorConductor.irAHome(sessionMock);

        assertThat(mav.getViewName(), equalTo("homeConductor"));
        assertThat(mav.getModel().get("nombreConductor").toString(), equalTo(conductorMock.getNombre()));
    }

    @Test
    void logoutDeberiaInvalidarSesionYRedirigirALogin() {
        ModelAndView mav = controladorConductor.logout(sessionMock);

        assertThat(mav.getViewName(), equalTo("redirect:/conductor/login"));
        verify(sessionMock, times(1)).invalidate();
    }
}
