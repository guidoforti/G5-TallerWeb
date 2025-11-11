package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.IServicio.ServicioAlmacenamientoFoto;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import com.tallerwebi.dominio.IServicio.ServicioViajero;
import com.tallerwebi.dominio.excepcion.*;
import com.tallerwebi.presentacion.Controller.ControladorRegistro;
import com.tallerwebi.presentacion.DTO.InputsDTO.RegistroInputDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.mockito.Mockito.*;

public class ControladorRegistroTest {

    private ControladorRegistro controladorRegistro;
    private ServicioConductor servicioConductorMock;
    private ServicioViajero servicioViajeroMock;
    private ServicioAlmacenamientoFoto servicioAlmacenamientoFotoMock;
    private HttpSession sessionMock;
    private Conductor conductorMock;
    private Viajero viajeroMock;

    @BeforeEach
    public void init() {
        servicioConductorMock = mock(ServicioConductor.class);
        servicioViajeroMock = mock(ServicioViajero.class);
        servicioAlmacenamientoFotoMock = mock(ServicioAlmacenamientoFoto.class);
        controladorRegistro = new ControladorRegistro(servicioConductorMock, servicioViajeroMock, servicioAlmacenamientoFotoMock);
        sessionMock = mock(HttpSession.class);

        // Mocks de entidades para simular el registro exitoso
        conductorMock = mock(Conductor.class);
        when(conductorMock.getId()).thenReturn(10L);

        viajeroMock = mock(Viajero.class);
        when(viajeroMock.getId()).thenReturn(20L);
    }

    // --- irARegistroUnificado (GET /registrarme) ---

    @Test
    void irARegistroUnificadoDeberiaRetornarVistaRegistro() {
        // Act
        ModelAndView mav = controladorRegistro.irARegistroUnificado();

        // Assert
        assertThat(mav.getViewName(), equalTo("registro"));
        assertThat(mav.getModel().get("datosRegistro"), instanceOf(RegistroInputDTO.class));
    }

    // --- registrar (POST /validar-registro) - Flujos de Éxito ---

    @Test
    void registroConductorExitosoDeberiaRedirigirAHomeConductorYSesion() throws Exception {
        // Arrange
        RegistroInputDTO dto = new RegistroInputDTO();
        dto.setRolSeleccionado("CONDUCTOR");
        // Mockear el DTO para que devuelva una entidad con ID
        when(servicioConductorMock.registrar(any(Conductor.class))).thenReturn(conductorMock);

        // Act
        ModelAndView mav = controladorRegistro.registrar(dto, sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("redirect:/conductor/home"));
        verify(servicioConductorMock, times(1)).registrar(any(Conductor.class));
        verify(servicioViajeroMock, never()).registrar(any());
        verify(sessionMock, times(1)).setAttribute("idUsuario", 10L);
        verify(sessionMock, times(1)).setAttribute("ROL", "CONDUCTOR");
    }

    @Test
    void registroViajeroExitosoDeberiaRedirigirAHomeViajeroYSesion() throws Exception {
        // Arrange
        RegistroInputDTO dto = new RegistroInputDTO();
        dto.setRolSeleccionado("VIAJERO");
        // Mockear el DTO para que devuelva una entidad con ID
        when(servicioViajeroMock.registrar(any(Viajero.class))).thenReturn(viajeroMock);

        // Act
        ModelAndView mav = controladorRegistro.registrar(dto, sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("redirect:/viajero/home"));
        verify(servicioViajeroMock, times(1)).registrar(any(Viajero.class));
        verify(servicioConductorMock, never()).registrar(any());
        verify(sessionMock, times(1)).setAttribute("idUsuario", 20L);
        verify(sessionMock, times(1)).setAttribute("ROL", "VIAJERO");
    }

    // --- registrar (POST /validar-registro) - Flujos de Falla y Errores de Negocio ---

    // Cobertura: if (registroDTO.getRolSeleccionado() == null || ...)
    @Test
    void registroSinRolSeleccionadoDeberiaVolverARegistroYMostrarError() throws UsuarioExistente, FechaDeVencimientoDeLicenciaInvalida, EdadInvalidaException, DatoObligatorioException{
        // Arrange
        RegistroInputDTO dto = new RegistroInputDTO();
        dto.setRolSeleccionado(null);

        // Act
        ModelAndView mav = controladorRegistro.registrar(dto, sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("registro"));
        assertThat(mav.getModel().get("error").toString(), equalTo("Debes seleccionar un rol para registrarte."));
        verify(servicioConductorMock, never()).registrar(any());
        verify(servicioViajeroMock, never()).registrar(any());
    }

    // Cobertura: else { Rol seleccionado no válido }
    @Test
    void registroConRolNoValidoDeberiaVolverARegistroYMostrarError() throws UsuarioExistente, FechaDeVencimientoDeLicenciaInvalida, EdadInvalidaException, DatoObligatorioException {
        // Arrange
        RegistroInputDTO dto = new RegistroInputDTO();
        dto.setRolSeleccionado("ADMIN"); // Rol no esperado

        // Act
        ModelAndView mav = controladorRegistro.registrar(dto, sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("registro"));
        assertThat(mav.getModel().get("error").toString(), equalTo("Rol seleccionado no válido."));
        verify(servicioConductorMock, never()).registrar(any());
        verify(servicioViajeroMock, never()).registrar(any());
    }

    // Cobertura: catch (UsuarioExistente e)
    @Test
    void registroConductorFallaPorUsuarioExistenteDeberiaVolverARegistro() throws Exception {
        // Arrange
        RegistroInputDTO dto = new RegistroInputDTO();
        dto.setRolSeleccionado("CONDUCTOR");
        doThrow(new UsuarioExistente("Email ya registrado")).when(servicioConductorMock).registrar(any(Conductor.class));

        // Act
        ModelAndView mav = controladorRegistro.registrar(dto, sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("registro"));
        assertThat(mav.getModel().get("error").toString(), equalTo("Error de registro: Ya existe un usuario con ese email."));
        verify(sessionMock, never()).setAttribute(anyString(), any());
    }

    // Cobertura: catch (FechaDeVencimientoDeLicenciaInvalida e)
    @Test
    void registroConductorFallaPorLicenciaInvalidaDeberiaVolverARegistro() throws Exception {
        // Arrange
        RegistroInputDTO dto = new RegistroInputDTO();
        dto.setRolSeleccionado("CONDUCTOR");
        doThrow(new FechaDeVencimientoDeLicenciaInvalida("Licencia vencida")).when(servicioConductorMock).registrar(any(Conductor.class));

        // Act
        ModelAndView mav = controladorRegistro.registrar(dto, sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("registro"));
        assertThat(mav.getModel().get("error").toString(), equalTo("Licencia vencida"));
    }

    // Cobertura: catch (EdadInvalidaException | DatoObligatorioException e) - Viajero (Edad)
    @Test
    void registroViajeroFallaPorEdadInvalidaDeberiaVolverARegistro() throws Exception {
        // Arrange
        RegistroInputDTO dto = new RegistroInputDTO();
        dto.setRolSeleccionado("VIAJERO");
        doThrow(new EdadInvalidaException("Menor de 18")).when(servicioViajeroMock).registrar(any(Viajero.class));

        // Act
        ModelAndView mav = controladorRegistro.registrar(dto, sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("registro"));
        assertThat(mav.getModel().get("error").toString(), equalTo("Menor de 18"));
    }

    // Cobertura: catch (EdadInvalidaException | DatoObligatorioException e) - Viajero (Obligatorio)
    @Test
    void registroViajeroFallaPorDatoObligatorioDeberiaVolverARegistro() throws Exception {
        // Arrange
        RegistroInputDTO dto = new RegistroInputDTO();
        dto.setRolSeleccionado("VIAJERO");
        doThrow(new DatoObligatorioException("El nombre es requerido")).when(servicioViajeroMock).registrar(any(Viajero.class));

        // Act
        ModelAndView mav = controladorRegistro.registrar(dto, sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("registro"));
        assertThat(mav.getModel().get("error").toString(), equalTo("El nombre es requerido"));
    }

    // Cobertura: catch (Exception e) - Excepción genérica
    @Test
    void registroFallaPorExcepcionGenericaDeberiaVolverARegistro() throws Exception {
        // Arrange
        RegistroInputDTO dto = new RegistroInputDTO();
        dto.setRolSeleccionado("CONDUCTOR");
        // Forzamos una RuntimeException para entrar al catch(Exception e)
        doThrow(new RuntimeException("Error de base de datos")).when(servicioConductorMock).registrar(any(Conductor.class));

        // Act
        ModelAndView mav = controladorRegistro.registrar(dto, sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("registro"));
        assertThat(mav.getModel().get("error").toString(), equalTo("Error desconocido durante el registro. Inténtalo de nuevo."));
    }
}