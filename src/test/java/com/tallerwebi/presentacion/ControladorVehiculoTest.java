package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Vehiculo;
import com.tallerwebi.dominio.Enums.EstadoVerificacion;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import com.tallerwebi.dominio.IServicio.ServicioNotificacion;
import com.tallerwebi.dominio.IServicio.ServicioVehiculo;
import com.tallerwebi.dominio.excepcion.NotFoundException;
import com.tallerwebi.dominio.excepcion.PatenteDuplicadaException;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.dominio.excepcion.VehiculoConViajesActivosException;
import com.tallerwebi.presentacion.Controller.ControladorVehiculo;
import com.tallerwebi.presentacion.DTO.InputsDTO.VehiculoInputDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

public class ControladorVehiculoTest {

    private ControladorVehiculo controladorVehiculo;
    private ServicioVehiculo servicioVehiculoMock;
    private ServicioNotificacion servicioNotificacionMock;
    private ServicioConductor servicioConductorMock;
    private HttpSession sessionMock;
    private VehiculoInputDTO vehiculoInputDTO;
    private Conductor conductorMock;
    private Vehiculo vehiculoMock;
    private final Long CONDUCTOR_ID = 1L;
    private final Long VEHICULO_ID = 1L;

    @BeforeEach
    public void init() {
        servicioVehiculoMock = mock(ServicioVehiculo.class);
        servicioConductorMock = mock(ServicioConductor.class);
        servicioNotificacionMock = mock(ServicioNotificacion.class);
        controladorVehiculo = new ControladorVehiculo(servicioVehiculoMock, servicioConductorMock, servicioNotificacionMock);
        sessionMock = mock(HttpSession.class);
        vehiculoInputDTO = new VehiculoInputDTO("Toyota", "2020", "ABC123", 4, EstadoVerificacion.PENDIENTE);
        conductorMock = new Conductor();
        conductorMock.setId(1L);
        conductorMock.setRol("CONDUCTOR");
        vehiculoMock = new Vehiculo(1L, "ABC123", "Toyota", "2020", 4, EstadoVerificacion.PENDIENTE, conductorMock);


                // Sesión válida
        when(sessionMock.getAttribute("rol")).thenReturn("CONDUCTOR");
        when(sessionMock.getAttribute("usuarioId")).thenReturn(1L);
    }

    @Test
    public void registrarVehiculo_conDatosValidos_deberiaRedirigirAListarVehiculos() throws Exception {
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");
        when(sessionMock.getAttribute("idUsuario")).thenReturn(1L);

        when(servicioConductorMock.obtenerConductor(1L)).thenReturn(conductorMock);
        when(servicioVehiculoMock.guardarVehiculo(any())).thenReturn(vehiculoMock);

        ModelAndView modelAndView = controladorVehiculo.registrarVehiculo(vehiculoInputDTO, sessionMock);

        String expectedRedirect = "redirect:/vehiculos/listarVehiculos?mensaje=%C2%A1Veh%C3%ADculo+%27ABC123%27+registrado+con+%C3%A9xito%21";

        assertThat(modelAndView.getViewName(), equalToIgnoringCase(expectedRedirect));
        verify(servicioVehiculoMock, times(1)).guardarVehiculo(any());
    }


    @Test
    public void registrarVehiculo_conPatenteDuplicada_deberiaMostrarError() throws Exception {
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");
        when(sessionMock.getAttribute("idUsuario")).thenReturn(1L);
        when(servicioConductorMock.obtenerConductor(1L)).thenReturn(conductorMock);
        when(servicioVehiculoMock.guardarVehiculo(any()))
                .thenThrow(new PatenteDuplicadaException("La patente ya está registrada"));

        ModelAndView modelAndView = controladorVehiculo.registrarVehiculo(vehiculoInputDTO, sessionMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("registrarVehiculo"));
        assertThat(modelAndView.getModel().get("error"), equalTo("La patente ya está registrada"));
    }


    @Test
    public void registrarVehiculo_conUsuarioNoAutorizado_deberiaMostrarError() throws PatenteDuplicadaException, NotFoundException {
        when(sessionMock.getAttribute("rol")).thenReturn("USUARIO"); // no es CONDUCTOR

        ModelAndView modelAndView = controladorVehiculo.registrarVehiculo(vehiculoInputDTO, sessionMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("usuarioNoAutorizado"));
        assertThat(modelAndView.getModel().get("error"), equalTo("no tienes permisos para acceder a este recurso"));
        verify(servicioVehiculoMock, never()).guardarVehiculo(any());
    }

    @Test
    public void mostrarFormularioDeRegistroVehiculo_conUsuarioNoAutorizado_deberiaMostrarError() throws Exception {
        when(sessionMock.getAttribute("idUsuario")).thenReturn(1L);
        when(sessionMock.getAttribute("ROL")).thenReturn("USUARIO");

        ModelAndView modelAndView = controladorVehiculo.mostrarFormularioDeRegistroVehiculo(sessionMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("usuarioNoAutorizado"));
        assertThat(modelAndView.getModel().get("error"), equalTo("no tienes permisos para acceder a este recurso"));
    }

    @Test
    public void listarVehiculosRegistrados_deberiaMostrarListaConMensajes() throws Exception {
        // Arrange
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");
        when(sessionMock.getAttribute("idUsuario")).thenReturn(CONDUCTOR_ID);
        when(servicioNotificacionMock.contarNoLeidas(CONDUCTOR_ID)).thenReturn(5L);
        when(servicioVehiculoMock.obtenerTodosLosVehiculosDeConductor(CONDUCTOR_ID)).thenReturn(java.util.Collections.singletonList(vehiculoMock));

        // Act
        ModelAndView modelAndView = controladorVehiculo.listarVehiculosRegistrados(sessionMock, "Error desde URL", "Mensaje OK desde URL");

        // Assert
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("listarVehiculos"));
        assertThat(modelAndView.getModel().get("error"), equalTo("Error desde URL"));
        assertThat(modelAndView.getModel().get("mensaje"), equalTo("Mensaje OK desde URL"));
        assertThat(modelAndView.getModel().get("listaVehiculos"), is(not(java.util.Collections.emptyList())));
        assertThat(modelAndView.getModel().get("contadorNotificaciones"), is(5L));
        verify(servicioVehiculoMock, times(1)).obtenerTodosLosVehiculosDeConductor(CONDUCTOR_ID);
    }

    @Test
    public void listarVehiculosRegistrados_deberiaMostrarErrorSiUsuarioNoAutorizado() {
        // Arrange
        when(sessionMock.getAttribute("ROL")).thenReturn(null);
        when(sessionMock.getAttribute("idUsuario")).thenReturn(CONDUCTOR_ID); // Rol incorrecto

        // Act
        ModelAndView modelAndView = controladorVehiculo.listarVehiculosRegistrados(sessionMock, null, null);

        // Assert
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("usuarioNoAutorizado"));
        assertThat(modelAndView.getModel().get("error"), equalTo("no tienes permisos para acceder a este recurso"));
        verify(servicioVehiculoMock, never()).obtenerTodosLosVehiculosDeConductor(anyLong());
    }

    @Test
    public void listarVehiculosRegistrados_deberiaMostrarErrorSiUsuarioIdEsNulo() {
        // Arrange
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");
        when(sessionMock.getAttribute("idUsuario")).thenReturn(null); // ID nulo

        // Act
        ModelAndView modelAndView = controladorVehiculo.listarVehiculosRegistrados(sessionMock, null, null);

        // Assert
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("usuarioNoAutorizado"));
        assertThat(modelAndView.getModel().get("error"), equalTo("no tienes permisos para acceder a este recurso"));
        verify(servicioVehiculoMock, never()).obtenerTodosLosVehiculosDeConductor(anyLong());
    }

    @Test
    public void listarVehiculosRegistrados_deberiaMostrarErrorGeneralSiFallaElServicio() throws Exception {
        // Arrange
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");
        when(sessionMock.getAttribute("idUsuario")).thenReturn(CONDUCTOR_ID);
        when(servicioVehiculoMock.obtenerTodosLosVehiculosDeConductor(CONDUCTOR_ID))
                .thenThrow(new RuntimeException("Error DB"));

        // Act
        ModelAndView modelAndView = controladorVehiculo.listarVehiculosRegistrados(sessionMock, null, null);

        // Assert
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("errorGeneral"));
        assertThat(modelAndView.getModel().get("error"), equalTo("Error al cargar vehículos: Error DB"));
    }

    // --- TESTS DE REGISTRAR VEHICULO (GET /registrar) ---

    @Test
    public void mostrarFormularioDeRegistroVehiculo_conSesionValida_deberiaMostrarFormulario() throws Exception {
        // Arrange
        when(sessionMock.getAttribute("idUsuario")).thenReturn(CONDUCTOR_ID);
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");
        when(servicioNotificacionMock.contarNoLeidas(CONDUCTOR_ID)).thenReturn(2L);

        // Act
        ModelAndView modelAndView = controladorVehiculo.mostrarFormularioDeRegistroVehiculo(sessionMock);

        // Assert
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("registrarVehiculo"));
        assertThat(modelAndView.getModel().get("vehiculoInputDTO"), is(notNullValue()));
        assertThat(modelAndView.getModel().get("contadorNotificaciones"), is(2L));
    }

    @Test
    public void mostrarFormularioDeRegistroVehiculo_conSesionNula_deberiaMostrarError() throws Exception {
        // Arrange
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");
        when(sessionMock.getAttribute("idUsuario")).thenReturn(null);

        // Act
        ModelAndView modelAndView = controladorVehiculo.mostrarFormularioDeRegistroVehiculo(sessionMock);

        // Assert
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("usuarioNoAutorizado"));
        assertThat(modelAndView.getModel().get("error"), equalTo("no tienes permisos para acceder a este recurso"));
    }

    // --- TESTS DE REGISTRAR VEHICULO (POST /registrar) ---

    @Test
    public void registrarVehiculo_conSesionNula_deberiaMostrarError() throws Exception {
        // Arrange
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");
        when(sessionMock.getAttribute("idUsuario")).thenReturn(null);

        // Act
        ModelAndView modelAndView = controladorVehiculo.registrarVehiculo(vehiculoInputDTO, sessionMock);

        // Assert
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("usuarioNoAutorizado"));
        assertThat(modelAndView.getModel().get("error"), equalTo("no tienes permisos para acceder a este recurso"));
    }

    @Test
    public void registrarVehiculo_conUsuarioInexistente_deberiaMostrarError() throws Exception {
        // Arrange
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");
        when(sessionMock.getAttribute("idUsuario")).thenReturn(1L);

        when(servicioConductorMock.obtenerConductor(1L)).thenThrow(new UsuarioInexistente("Conductor no encontrado"));

        // Act
        ModelAndView modelAndView = controladorVehiculo.registrarVehiculo(vehiculoInputDTO, sessionMock);

        // Assert
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("registrarVehiculo"));
        assertThat(modelAndView.getModel().get("error"), equalTo("Conductor no encontrado"));
        verify(servicioConductorMock, times(1)).obtenerConductor(1L);
    }

    @Test
    public void registrarVehiculo_conNotFoundException_deberiaMostrarError() throws Exception {
        // Arrange
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");
        when(sessionMock.getAttribute("idUsuario")).thenReturn(1L);

        when(servicioConductorMock.obtenerConductor(1L)).thenReturn(conductorMock);
        when(servicioVehiculoMock.guardarVehiculo(any()))
                .thenThrow(new NotFoundException("Error en entidad relacionada"));

        // Act
        ModelAndView modelAndView = controladorVehiculo.registrarVehiculo(vehiculoInputDTO, sessionMock);

        // Assert
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("registrarVehiculo"));
        assertThat(modelAndView.getModel().get("error"), equalTo("Error en entidad relacionada"));
    }

    // --- TESTS DE MOSTRAR CONFIRMACION DESACTIVAR (GET /desactivar/{id}) ---

    @Test
    public void mostrarConfirmacionDesactivar_deberiaRedirigirALoginSiNoHaySesion() {
        // Arrange
        when(sessionMock.getAttribute("idUsuario")).thenReturn(null);

        // Act
        ModelAndView modelAndView = controladorVehiculo.mostrarConfirmacionDesactivar(VEHICULO_ID, sessionMock);

        // Assert
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
        verify(servicioVehiculoMock, never()).getById(anyLong());
    }

    @Test
    public void mostrarConfirmacionDesactivar_deberiaRedirigirConErrorSiVehiculoNoExiste() throws Exception {
        // Arrange
        when(sessionMock.getAttribute("idUsuario")).thenReturn(CONDUCTOR_ID);
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");
        when(servicioVehiculoMock.getById(VEHICULO_ID)).thenThrow(new NotFoundException("Vehículo no existe"));

        // Act
        ModelAndView modelAndView = controladorVehiculo.mostrarConfirmacionDesactivar(VEHICULO_ID, sessionMock);

        // Assert
        String expectedRedirect = "redirect:/vehiculos/listarVehiculos?error=" + encodeUrl("Vehículo no existe");
        assertThat(modelAndView.getViewName(), equalToIgnoringCase(expectedRedirect));
    }

    @Test
    public void mostrarConfirmacionDesactivar_deberiaRedirigirConErrorSiVehiculoTieneViajesActivos() throws Exception {
        // Arrange
        when(sessionMock.getAttribute("idUsuario")).thenReturn(CONDUCTOR_ID);
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");

        // El vehículo pertenece al conductor
        vehiculoMock.getConductor().setId(CONDUCTOR_ID);
        when(servicioVehiculoMock.getById(VEHICULO_ID)).thenReturn(vehiculoMock);

        // Simular que tiene viajes activos
        doThrow(new VehiculoConViajesActivosException("No se puede desactivar por viajes activos"))
                .when(servicioVehiculoMock).verificarViajesActivos(VEHICULO_ID);

        // Act
        ModelAndView modelAndView = controladorVehiculo.mostrarConfirmacionDesactivar(VEHICULO_ID, sessionMock);

        // Assert
        String expectedError = encodeUrl("No se puede desactivar por viajes activos");
        String expectedRedirect = "redirect:/vehiculos/listarVehiculos?error=" + expectedError;
        assertThat(modelAndView.getViewName(), equalToIgnoringCase(expectedRedirect));
    }

    @Test
    public void mostrarConfirmacionDesactivar_deberiaMostrarErrorSiVehiculoNoPerteneceAlConductor() throws Exception {
        // Arrange
        Long otroConductorId = 99L;
        when(sessionMock.getAttribute("idUsuario")).thenReturn(CONDUCTOR_ID);
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");

        // El vehículo mock pertenece a otro conductor
        vehiculoMock.getConductor().setId(otroConductorId);
        when(servicioVehiculoMock.getById(VEHICULO_ID)).thenReturn(vehiculoMock);

        // Act
        ModelAndView modelAndView = controladorVehiculo.mostrarConfirmacionDesactivar(VEHICULO_ID, sessionMock);

        // Assert
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("usuarioNoAutorizado"));
        assertThat(modelAndView.getModel().get("error"), equalTo("No tienes permiso para desactivar este vehículo."));
    }

    // --- TESTS DE DESACTIVAR VEHICULO (POST /desactivar/{id}) ---

    @Test
    public void desactivarVehiculo_deberiaRedirigirConMensajeDeExito() throws Exception {
        // Arrange
        when(sessionMock.getAttribute("idUsuario")).thenReturn(CONDUCTOR_ID);
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");
        doNothing().when(servicioVehiculoMock).desactivarVehiculo(VEHICULO_ID);

        // Act
        ModelAndView modelAndView = controladorVehiculo.desactivarVehiculo(VEHICULO_ID, sessionMock);

        // Assert
        String expectedMensaje = encodeUrl("Vehículo desactivado correctamente.");
        String expectedRedirect = "redirect:/vehiculos/listarVehiculos?mensaje=" + expectedMensaje;
        assertThat(modelAndView.getViewName(), equalToIgnoringCase(expectedRedirect));
        verify(servicioVehiculoMock, times(1)).desactivarVehiculo(VEHICULO_ID);
    }

    @Test
    public void desactivarVehiculo_deberiaRedirigirConErrorSiVehiculoNoExiste() throws Exception {
        // Arrange
        when(sessionMock.getAttribute("idUsuario")).thenReturn(CONDUCTOR_ID);
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");
        doThrow(new NotFoundException("Vehículo no encontrado")).when(servicioVehiculoMock).desactivarVehiculo(VEHICULO_ID);

        // Act
        ModelAndView modelAndView = controladorVehiculo.desactivarVehiculo(VEHICULO_ID, sessionMock);

        // Assert
        String expectedError = encodeUrl("Error: Vehículo no encontrado.");
        String expectedRedirect = "redirect:/vehiculos/listarVehiculos?error=" + expectedError;
        assertThat(modelAndView.getViewName(), equalToIgnoringCase(expectedRedirect));
    }

    @Test
    public void desactivarVehiculo_deberiaRedirigirConErrorSiVehiculoTieneViajesActivos() throws Exception {
        // Arrange
        when(sessionMock.getAttribute("idUsuario")).thenReturn(CONDUCTOR_ID);
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");
        String exceptionMessage = "No se puede desactivar: Viajes activos.";
        doThrow(new VehiculoConViajesActivosException(exceptionMessage)).when(servicioVehiculoMock).desactivarVehiculo(VEHICULO_ID);

        // Act
        ModelAndView modelAndView = controladorVehiculo.desactivarVehiculo(VEHICULO_ID, sessionMock);

        // Assert
        String expectedError = encodeUrl(exceptionMessage);
        String expectedRedirect = "redirect:/vehiculos/listarVehiculos?error=" + expectedError;
        assertThat(modelAndView.getViewName(), equalToIgnoringCase(expectedRedirect));
    }

    @Test
    public void desactivarVehiculo_deberiaRedirigirConErrorSiOcurreExcepcionGenerica() throws Exception {
        // Arrange
        when(sessionMock.getAttribute("idUsuario")).thenReturn(CONDUCTOR_ID);
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");
        doThrow(new RuntimeException("Error fatal")).when(servicioVehiculoMock).desactivarVehiculo(VEHICULO_ID);

        // Act
        ModelAndView modelAndView = controladorVehiculo.desactivarVehiculo(VEHICULO_ID, sessionMock);

        // Assert
        String expectedError = encodeUrl("Error inesperado al intentar desactivar el vehículo.");
        String expectedRedirect = "redirect:/vehiculos/listarVehiculos?error=" + expectedError;
        assertThat(modelAndView.getViewName(), equalToIgnoringCase(expectedRedirect));
    }

    @Test
    public void desactivarVehiculo_deberiaRedirigirALoginSiNoHaySesion() throws VehiculoConViajesActivosException {
        // Arrange
        when(sessionMock.getAttribute("idUsuario")).thenReturn(null);

        // Act
        ModelAndView modelAndView = controladorVehiculo.desactivarVehiculo(VEHICULO_ID, sessionMock);

        // Assert
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
        verify(servicioVehiculoMock, never()).desactivarVehiculo(anyLong());
    }

    private String encodeUrl(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }
}
