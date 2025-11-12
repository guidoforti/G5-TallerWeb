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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

public class ControladorRegistroTest {

    private ControladorRegistro controladorRegistro;
    private ServicioConductor servicioConductorMock;
    private ServicioViajero servicioViajeroMock;
    private ServicioAlmacenamientoFoto servicioAlmacenamientoFotoMock;
    private HttpSession sessionMock;
    private Conductor conductorMock;
    private Viajero viajeroMock;
    private MultipartFile fotoMock; // Mock para el argumento de la foto

    @BeforeEach
    public void init() {
        servicioConductorMock = mock(ServicioConductor.class);
        servicioViajeroMock = mock(ServicioViajero.class);
        servicioAlmacenamientoFotoMock = mock(ServicioAlmacenamientoFoto.class);
        sessionMock = mock(HttpSession.class);

        // Mocks de entidades y foto
        conductorMock = mock(Conductor.class);
        when(conductorMock.getId()).thenReturn(10L);

        viajeroMock = mock(Viajero.class);
        when(viajeroMock.getId()).thenReturn(20L);

        // FotoMock por defecto: Vacío y no lanza excepción
        fotoMock = mock(MultipartFile.class);
        when(fotoMock.isEmpty()).thenReturn(true);

        controladorRegistro = new ControladorRegistro(servicioConductorMock, servicioViajeroMock, servicioAlmacenamientoFotoMock);
    }

    // Helper para crear un DTO funcional con fecha de nacimiento requerida
    private RegistroInputDTO crearDtoBase(String rol) {
        RegistroInputDTO dto = new RegistroInputDTO();
        dto.setRolSeleccionado(rol);
        dto.setNombre("Test User");
        dto.setEmail("test@email.com");
        dto.setContrasenia("1234");
        // Establecer Fecha de Nacimiento válida (mayor de 18 años)
        dto.setFechaNacimiento(LocalDate.now().minusYears(25));
        return dto;
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
        RegistroInputDTO dto = crearDtoBase("CONDUCTOR");
        when(servicioConductorMock.registrar(any(Conductor.class))).thenReturn(conductorMock);

        // Act
        ModelAndView mav = controladorRegistro.registrar(dto, fotoMock, sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("redirect:/conductor/home"));
        verify(servicioConductorMock, times(1)).registrar(any(Conductor.class));
        verify(sessionMock, times(1)).setAttribute("idUsuario", 10L);
    }

    @Test
    void registroViajeroExitosoDeberiaRedirigirAHomeViajeroYSesion() throws Exception {
        // Arrange
        RegistroInputDTO dto = crearDtoBase("VIAJERO");
        when(servicioViajeroMock.registrar(any(Viajero.class))).thenReturn(viajeroMock);

        // Act
        ModelAndView mav = controladorRegistro.registrar(dto, fotoMock, sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("redirect:/viajero/home"));
        verify(servicioViajeroMock, times(1)).registrar(any(Viajero.class));
        verify(sessionMock, times(1)).setAttribute("idUsuario", 20L);
    }

    @Test
    void registroConFotoExitosoDeberiaGuardarURLYRedirigir() throws Exception {
        // Arrange
        RegistroInputDTO dto = crearDtoBase("VIAJERO");
        String urlEsperada = "/images/profile_uploads/unique_id.jpg";

        when(fotoMock.isEmpty()).thenReturn(false);
        when(servicioAlmacenamientoFotoMock.guardarArchivo(fotoMock)).thenReturn(urlEsperada);
        when(servicioViajeroMock.registrar(any(Viajero.class))).thenAnswer(invocation -> {
            // Verificar que la URL se asignó a la entidad antes del registro
            Viajero v = invocation.getArgument(0);
            assertThat(v.getFotoPerfilUrl(), equalTo(urlEsperada));
            return viajeroMock;
        });

        // Act
        ModelAndView mav = controladorRegistro.registrar(dto, fotoMock, sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("redirect:/viajero/home"));
        verify(servicioAlmacenamientoFotoMock, times(1)).guardarArchivo(fotoMock);
    }

    // --- Flujos de Falla y Errores de Negocio ---

    @Test
    void registroSinRolSeleccionadoDeberiaVolverARegistroYMostrarError() throws Exception {
        // Arrange
        RegistroInputDTO dto = crearDtoBase(null);

        // Act
        ModelAndView mav = controladorRegistro.registrar(dto, fotoMock, sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("registro"));
        assertThat(mav.getModel().get("error").toString(), equalTo("Debes seleccionar un rol para registrarte."));
    }

    @Test
    void registroConRolNoValidoDeberiaVolverARegistroYMostrarError() throws Exception {
        // Arrange
        RegistroInputDTO dto = crearDtoBase("ADMIN");

        // Act
        ModelAndView mav = controladorRegistro.registrar(dto, fotoMock, sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("registro"));
        assertThat(mav.getModel().get("error").toString(), equalTo("Rol seleccionado no válido."));
    }

    @Test
    void registroSinFechaNacimientoDeberiaVolverARegistroYMostrarError() throws Exception {
        // Arrange
        RegistroInputDTO dto = crearDtoBase("VIAJERO");
        dto.setFechaNacimiento(null);

        // Act
        ModelAndView mav = controladorRegistro.registrar(dto, fotoMock, sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("registro"));
        assertThat(mav.getModel().get("error").toString(), equalTo("La fecha de nacimiento es obligatoria."));
    }

    @Test
    void registroFallaPorGuardadoDeFotoDeberiaVolverARegistro() throws Exception {
        // Arrange
        RegistroInputDTO dto = crearDtoBase("CONDUCTOR");

        when(fotoMock.isEmpty()).thenReturn(false);
        doThrow(new NotFoundException("Fallo de escritura en disco")).when(servicioAlmacenamientoFotoMock).guardarArchivo(fotoMock);

        // Act
        ModelAndView mav = controladorRegistro.registrar(dto, fotoMock, sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("registro"));
        assertThat(mav.getModel().get("error").toString(), containsString("Error desconocido durante el registro. Inténtalo de nuevo."));
    }

    @Test
    void registroConductorFallaPorLicenciaInvalidaDeberiaVolverARegistro() throws Exception {
        // Arrange
        RegistroInputDTO dto = crearDtoBase("CONDUCTOR");
        doThrow(new FechaDeVencimientoDeLicenciaInvalida("Licencia vencida")).when(servicioConductorMock).registrar(any(Conductor.class));

        // Act
        ModelAndView mav = controladorRegistro.registrar(dto, fotoMock, sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("registro"));
        assertThat(mav.getModel().get("error").toString(), equalTo("Licencia vencida"));
    }

    @Test
    void registroViajeroFallaPorDatoObligatorioDeberiaVolverARegistro() throws Exception {
        // Arrange
        RegistroInputDTO dto = crearDtoBase("VIAJERO");
        doThrow(new DatoObligatorioException("El nombre es requerido")).when(servicioViajeroMock).registrar(any(Viajero.class));

        // Act
        ModelAndView mav = controladorRegistro.registrar(dto, fotoMock, sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("registro"));
        assertThat(mav.getModel().get("error").toString(), equalTo("El nombre es requerido"));
    }

    @Test
    void registroFallaPorExcepcionGenericaDeberiaVolverARegistro() throws Exception {
        // Arrange
        RegistroInputDTO dto = crearDtoBase("CONDUCTOR");
        doThrow(new RuntimeException("Error de base de datos")).when(servicioConductorMock).registrar(any(Conductor.class));

        // Act
        ModelAndView mav = controladorRegistro.registrar(dto, fotoMock, sessionMock);

        // Assert
        assertThat(mav.getViewName(), equalTo("registro"));
        assertThat(mav.getModel().get("error").toString(), equalTo("Error desconocido durante el registro. Inténtalo de nuevo."));
    }
}