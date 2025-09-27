package com.tallerwebi.presentacion;
import com.tallerwebi.config.ManualModelMapper;
import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import com.tallerwebi.dominio.excepcion.CredencialesInvalidas;
import com.tallerwebi.dominio.excepcion.FechaDeVencimientoDeLicenciaInvalida;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.presentacion.Controller.ControladorConductor;
import com.tallerwebi.presentacion.DTO.InputsDTO.ConductorRegistroInputDTO;
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
    private ManualModelMapper manualModelMapperMock;
    private ConductorLoginDTO loginDTO;
    private HttpSession sessionMock;
    private Conductor conductorMock;
    private ConductorRegistroInputDTO conductorRegistroInputDTOMock;

    @BeforeEach
    public void init() {
        servicioConductorMock = mock(ServicioConductor.class);
        manualModelMapperMock = new ManualModelMapper();
        controladorConductor = new ControladorConductor(servicioConductorMock, manualModelMapperMock);
        loginDTO = new ConductorLoginDTO("conductor@mail.com", "1234");
        sessionMock = mock(HttpSession.class);
        conductorMock = mock(Conductor.class);
        conductorRegistroInputDTOMock = mock(ConductorRegistroInputDTO.class);

        when(conductorMock.getId()).thenReturn(1L);
        when(conductorRegistroInputDTOMock.getId()).thenReturn(1L);
        when(conductorRegistroInputDTOMock.getNombre()).thenReturn("Pepe");
    }

    @Test
    public void loginConCredencialesCorrectasDeberiaRedirigirAHomeYSetearSesion() throws CredencialesInvalidas {
        // preparación
        when(servicioConductorMock.login(loginDTO.getEmail(), loginDTO.getContrasenia()))
                .thenReturn(conductorRegistroInputDTOMock);

        // ejecución
        ModelAndView modelAndView = controladorConductor.validarLogin(loginDTO, sessionMock);

        // validación
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/conductor/home"));
        verify(sessionMock, times(1)).setAttribute("usuarioId", conductorRegistroInputDTOMock.getId());
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
        assertThat(mav.getViewName(), equalTo("redirect:/conductor/home"));
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
        ConductorRegistroInputDTO nuevoConductorRegistroInputDTO = manualModelMapperMock.toConductorDTO(nuevoConductor);
        when(servicioConductorMock.registrar(nuevoConductor)).thenReturn(nuevoConductorRegistroInputDTO);

        // ejecución
        ModelAndView mav = controladorConductor.registrar(nuevoConductorRegistroInputDTO, sessionMock);

        // validación
        assertThat(mav.getViewName(), equalTo("redirect:/conductor/home"));
        verify(sessionMock, times(1)).setAttribute("usuarioId", nuevoConductor.getId());
        verify(sessionMock, times(1)).setAttribute("rol", "CONDUCTOR");
    }

    @Test
    public void registroConEmailExistenteDeberiaVolverAFormularioConError() throws UsuarioExistente, FechaDeVencimientoDeLicenciaInvalida {
        // preparación
        Conductor nuevoConductor = new Conductor(null, null, "Ana", "ana@mail.com", "123", LocalDate.now());
        ConductorRegistroInputDTO nuevoConductorRegistroInputDTO = manualModelMapperMock.toConductorDTO(nuevoConductor);
        doThrow(new UsuarioExistente("Ya existe un usuario con ese email"))
                .when(servicioConductorMock).registrar(nuevoConductor);

        // ejecución
        ModelAndView mav = controladorConductor.registrar(nuevoConductorRegistroInputDTO, sessionMock);

        // validación
        assertThat(mav.getViewName(), equalTo("registroConductor"));
        assertThat(mav.getModel().get("error").toString(),
                equalToIgnoringCase("Ya existe un usuario con ese email"));
        verify(sessionMock, times(0)).setAttribute(eq("usuarioId"), any());
    }

    @Test
    void siUsuarioNoEstaEnSesionEnHomeDeberiaRedirigirALogin() {
        // given
        when(sessionMock.getAttribute("usuarioId")).thenReturn(null);

        // when
        ModelAndView mav = controladorConductor.irAHome(sessionMock);

        // then
        assertThat(mav.getViewName(), equalTo("redirect:/conductor/login"));
        verify(sessionMock, times(1)).getAttribute("usuarioId");
    }

    @Test
    void siUsuarioEstaEnSesionEnHomeDeberiaMostrarHomeConNombre() throws UsuarioInexistente {
        // given
        when(sessionMock.getAttribute("usuarioId")).thenReturn(1L);
        when(servicioConductorMock.obtenerConductor(conductorRegistroInputDTOMock.getId())).thenReturn(conductorRegistroInputDTOMock);

        // when
        ModelAndView mav = controladorConductor.irAHome(sessionMock);

        // then
        assertThat(mav.getViewName(), equalTo("homeConductor"));
        assertThat(mav.getModel().get("nombreConductor").toString(), equalTo(conductorRegistroInputDTOMock.getNombre()));
    }

    @Test
    void logoutDeberiaInvalidarSesionYRedirigirALogin() {
        // when
        ModelAndView mav = controladorConductor.logout(sessionMock);

        // then
        assertThat(mav.getViewName(), equalTo("redirect:/conductor/login"));
        verify(sessionMock, times(1)).invalidate();
    }

}