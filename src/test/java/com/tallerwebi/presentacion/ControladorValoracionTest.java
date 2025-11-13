package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.IServicio.ServicioValoracion;
import com.tallerwebi.dominio.IServicio.ServicioViaje;
import com.tallerwebi.dominio.excepcion.DatoObligatorioException;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.dominio.excepcion.UsuarioNoAutorizadoException;
import com.tallerwebi.dominio.excepcion.ViajeNoEncontradoException;
import com.tallerwebi.presentacion.Controller.ControladorValoracion;
import com.tallerwebi.presentacion.DTO.InputsDTO.ValoracionIndividualInputDTO;
import com.tallerwebi.presentacion.DTO.InputsDTO.ValoracionViajeInputDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ViajeroParaValorarOutputDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class ControladorValoracionTest {

    private ControladorValoracion controladorValoracion;
    private ServicioValoracion servicioValoracionMock;
    private ServicioViaje servicioViajeMock;
    private HttpSession sessionMock;

    private final Long CONDUCTOR_ID = 1L;
    private final Long VIAJE_ID = 10L;

    @BeforeEach
    public void init() {
        servicioValoracionMock = mock(ServicioValoracion.class);
        servicioViajeMock = mock(ServicioViaje.class);
        controladorValoracion = new ControladorValoracion(servicioValoracionMock, servicioViajeMock);
        sessionMock = mock(HttpSession.class);
    }

    // =================================================================================
    // Tests: verViajerosParaValorar (@GetMapping("/viaje/{viajeId}"))
    // =================================================================================

    @Test
    void verViajerosParaValorar_deberiaMostrarFormularioConViajeros() throws Exception {
        // Arrange
        Viajero viajero1 = new Viajero();
        viajero1.setId(2L);
        viajero1.setNombre("Viajero Uno");

        when(sessionMock.getAttribute("idUsuario")).thenReturn(CONDUCTOR_ID);
        when(servicioValoracionMock.obtenerViajeros(VIAJE_ID)).thenReturn(Arrays.asList(viajero1));

        // Act
        ModelAndView mav = controladorValoracion.verViajerosParaValorar(VIAJE_ID, sessionMock);

        // Assert
        assertThat(mav.getViewName(), is("valorarViajero"));
        assertThat(mav.getModel().get("viajeros"), instanceOf(List.class));
        assertThat(((List<ViajeroParaValorarOutputDTO>) mav.getModel().get("viajeros")), hasSize(1));
        assertThat(mav.getModel().get("formularioValoracion"), instanceOf(ValoracionViajeInputDTO.class));
        assertThat(mav.getModel().get("viajeId"), is(VIAJE_ID));
        verify(servicioValoracionMock, times(1)).obtenerViajeros(VIAJE_ID);
    }

    @Test
    void verViajerosParaValorar_deberiaRedirigirALoginSiNoHaySesion() throws ViajeNoEncontradoException {
        // Arrange
        when(sessionMock.getAttribute("idUsuario")).thenReturn(null);

        // Act
        ModelAndView mav = controladorValoracion.verViajerosParaValorar(VIAJE_ID, sessionMock);

        // Assert
        assertThat(mav.getViewName(), is("redirect:/login"));
        verify(servicioValoracionMock, never()).obtenerViajeros(anyLong());
    }

    @Test
    void verViajerosParaValorar_deberiaMostrarErrorSiNoHayViajerosParaValorar() throws Exception {
        // Arrange
        when(sessionMock.getAttribute("idUsuario")).thenReturn(CONDUCTOR_ID);
        when(servicioValoracionMock.obtenerViajeros(VIAJE_ID)).thenReturn(Collections.emptyList());

        // Act
        ModelAndView mav = controladorValoracion.verViajerosParaValorar(VIAJE_ID, sessionMock);

        // Assert
        assertThat(mav.getViewName(), is("error"));
        assertThat(mav.getModel().get("error").toString(), is("No hay viajeros para valorar en este viaje."));
    }

    @Test
    void verViajerosParaValorar_deberiaMostrarErrorSiViajeNoExiste() throws Exception {
        // Arrange
        when(sessionMock.getAttribute("idUsuario")).thenReturn(CONDUCTOR_ID);
        doThrow(new ViajeNoEncontradoException("Viaje no encontrado"))
                .when(servicioValoracionMock).obtenerViajeros(VIAJE_ID);

        // Act
        ModelAndView mav = controladorValoracion.verViajerosParaValorar(VIAJE_ID, sessionMock);

        // Assert
        assertThat(mav.getViewName(), is("error"));
        assertThat(mav.getModel().get("error").toString(), is("Viaje no encontrado"));
    }

    @Test
    void verViajerosParaValorar_deberiaMostrarErrorSiOcurreExcepcionGenerica() throws Exception {
        // Arrange
        when(sessionMock.getAttribute("idUsuario")).thenReturn(CONDUCTOR_ID);
        doThrow(new RuntimeException("Error inesperado"))
                .when(servicioValoracionMock).obtenerViajeros(VIAJE_ID);

        // Act
        ModelAndView mav = controladorValoracion.verViajerosParaValorar(VIAJE_ID, sessionMock);

        // Assert
        assertThat(mav.getViewName(), is("error"));
        assertThat(mav.getModel().get("error").toString(), containsString("Error inesperado"));
    }

    // =================================================================================
    // Tests: enviarTodasLasValoraciones (@PostMapping("/enviar"))
    // =================================================================================

    @Test
    void enviarTodasLasValoraciones_deberiaEnviarConExitoYRedirigirAHomeConductor() throws Exception {
        // Arrange
        Long emisorId = CONDUCTOR_ID;
        ValoracionIndividualInputDTO dto1 = new ValoracionIndividualInputDTO(2L, 5, "Genial");
        ValoracionIndividualInputDTO dto2 = new ValoracionIndividualInputDTO(3L, 4, "Bien");
        ValoracionViajeInputDTO formulario = new ValoracionViajeInputDTO(Arrays.asList(dto1, dto2));
        Usuario emisorMock = mock(Usuario.class);

        when(sessionMock.getAttribute("idUsuario")).thenReturn(emisorId);
        when(servicioValoracionMock.obtenerUsuario(emisorId)).thenReturn(emisorMock);
        // El servicio.valorarUsuario es void, por defecto no hace nada (DoNothing)

        // Act
        ModelAndView mav = controladorValoracion.enviarTodasLasValoraciones(formulario, VIAJE_ID, sessionMock);

        // Assert
        assertThat(mav.getViewName(), is("redirect:/conductor/home"));
        assertThat(mav.getModel().get("mensaje").toString(), is("¡Valoraciones enviadas con éxito!"));
        verify(servicioValoracionMock, times(2)).valorarUsuario(eq(emisorMock), any(ValoracionIndividualInputDTO.class), eq(VIAJE_ID));
        verify(servicioValoracionMock, times(1)).valorarUsuario(emisorMock, dto1, VIAJE_ID);
        verify(servicioValoracionMock, times(1)).valorarUsuario(emisorMock, dto2, VIAJE_ID);
    }

    @Test
    void enviarTodasLasValoraciones_deberiaRedirigirALoginSiNoHaySesion() throws Exception {
        // Arrange
        when(sessionMock.getAttribute("idUsuario")).thenReturn(null);

        // Act
        ModelAndView mav = controladorValoracion.enviarTodasLasValoraciones(new ValoracionViajeInputDTO(), VIAJE_ID, sessionMock);

        // Assert
        assertThat(mav.getViewName(), is("redirect:/login"));
        verify(servicioValoracionMock, never()).obtenerUsuario(anyLong());
    }

    @Test
    void enviarTodasLasValoraciones_deberiaMostrarErrorSiExcepcionDeNegocio() throws Exception {
        // Arrange
        Long emisorId = CONDUCTOR_ID;
        ValoracionIndividualInputDTO dto1 = new ValoracionIndividualInputDTO(2L, null, "Sin puntuación");
        ValoracionViajeInputDTO formulario = new ValoracionViajeInputDTO(Arrays.asList(dto1));
        Usuario emisorMock = mock(Usuario.class);

        when(sessionMock.getAttribute("idUsuario")).thenReturn(emisorId);
        when(servicioValoracionMock.obtenerUsuario(emisorId)).thenReturn(emisorMock);
        // Simular excepción de dato obligatorio en el bucle
        doThrow(new DatoObligatorioException("Puntuación requerida"))
                .when(servicioValoracionMock).valorarUsuario(eq(emisorMock), any(ValoracionIndividualInputDTO.class), eq(VIAJE_ID));

        // Act
        ModelAndView mav = controladorValoracion.enviarTodasLasValoraciones(formulario, VIAJE_ID, sessionMock);

        // Assert
        assertThat(mav.getViewName(), is("error"));
        assertThat(mav.getModel().get("error").toString(), containsString("Puntuación requerida"));
    }

    @Test
    void enviarTodasLasValoraciones_deberiaMostrarErrorSiExcepcionGenerica() throws Exception {
        // Arrange
        Long emisorId = CONDUCTOR_ID;
        ValoracionIndividualInputDTO dto1 = new ValoracionIndividualInputDTO(2L, 5, "Genial");
        ValoracionViajeInputDTO formulario = new ValoracionViajeInputDTO(Arrays.asList(dto1));
        Usuario emisorMock = mock(Usuario.class);

        when(sessionMock.getAttribute("idUsuario")).thenReturn(emisorId);
        when(servicioValoracionMock.obtenerUsuario(emisorId)).thenReturn(emisorMock);
        // Simular excepción inesperada
        doThrow(new RuntimeException("Error de base de datos"))
                .when(servicioValoracionMock).valorarUsuario(eq(emisorMock), any(ValoracionIndividualInputDTO.class), eq(VIAJE_ID));

        // Act
        ModelAndView mav = controladorValoracion.enviarTodasLasValoraciones(formulario, VIAJE_ID, sessionMock);

        // Assert
        assertThat(mav.getViewName(), is("error"));
        assertThat(mav.getModel().get("error").toString(), is("Ocurrió un error inesperado al enviar las valoraciones."));
    }

    // =================================================================================
    // Tests: mostrarFormularioValoracionConductor (@GetMapping("/conductor/form"))
    // =================================================================================

    @Test
    void mostrarFormularioValoracionConductor_deberiaMostrarFormularioConDatosDelConductor() throws Exception {
        // Arrange
        Long viajeroId = 5L;
        Viaje viajeMock = crearViajeMockConConductor(2L, "Juan Perez");

        when(sessionMock.getAttribute("idUsuario")).thenReturn(viajeroId);
        when(servicioViajeMock.obtenerViajePorId(VIAJE_ID)).thenReturn(viajeMock);

        // Act
        ModelAndView mav = controladorValoracion.mostrarFormularioValoracionConductor(VIAJE_ID, sessionMock);

        // Assert
        assertThat(mav.getViewName(), is("valorarConductor"));
        assertThat(mav.getModel().get("conductorNombre"), is("Juan Perez"));
        assertThat(mav.getModel().get("valoracionDto"), instanceOf(ValoracionIndividualInputDTO.class));
        assertThat(((ValoracionIndividualInputDTO) mav.getModel().get("valoracionDto")).getReceptorId(), is(2L));
        verify(servicioViajeMock, times(1)).obtenerViajePorId(VIAJE_ID);
    }

    @Test
    void mostrarFormularioValoracionConductor_deberiaRedirigirALoginSiNoHaySesion() throws UsuarioNoAutorizadoException, ViajeNoEncontradoException {
        // Arrange
        when(sessionMock.getAttribute("idUsuario")).thenReturn(null);

        // Act
        ModelAndView mav = controladorValoracion.mostrarFormularioValoracionConductor(VIAJE_ID, sessionMock);

        // Assert
        assertThat(mav.getViewName(), is("redirect:/login"));
        verify(servicioViajeMock, never()).obtenerViajePorId(anyLong());
    }

    @Test
    void mostrarFormularioValoracionConductor_deberiaMostrarErrorSiOcurreExcepcion() throws Exception {
        // Arrange
        Long viajeroId = 5L;
        when(sessionMock.getAttribute("idUsuario")).thenReturn(viajeroId);
        doThrow(new ViajeNoEncontradoException("Error al cargar el viaje"))
                .when(servicioViajeMock).obtenerViajePorId(VIAJE_ID);

        // Act
        ModelAndView mav = controladorValoracion.mostrarFormularioValoracionConductor(VIAJE_ID, sessionMock);

        // Assert
        assertThat(mav.getViewName(), is("error"));
        assertThat(mav.getModel().get("error").toString(), containsString("Error al cargar el viaje"));
    }


    // =================================================================================
    // Tests: enviarValoracionUnitaria (@PostMapping("/unidad"))
    // =================================================================================

    @Test
    void enviarValoracionUnitaria_deberiaEnviarConExitoYRedirigirAMisViajes() throws Exception {
        // Arrange
        Long viajeroId = 5L;
        ValoracionIndividualInputDTO valoracionDto = new ValoracionIndividualInputDTO(2L, 5, "Muy bien");
        Usuario emisorMock = mock(Usuario.class);

        when(sessionMock.getAttribute("idUsuario")).thenReturn(viajeroId);
        when(sessionMock.getAttribute("ROL")).thenReturn("VIAJERO");
        when(servicioValoracionMock.obtenerUsuario(viajeroId)).thenReturn(emisorMock);

        // Act
        ModelAndView mav = controladorValoracion.enviarValoracionUnitaria(valoracionDto, VIAJE_ID, sessionMock);

        // Assert
        assertThat(mav.getViewName(), is("redirect:/reserva/misViajes"));
        verify(servicioValoracionMock, times(1)).valorarUsuario(emisorMock, valoracionDto, VIAJE_ID);
    }

    @Test
    void enviarValoracionUnitaria_deberiaRedirigirALoginSiNoHaySesionOEsConductor() throws Exception {
        // Case 1: No hay sesión
        when(sessionMock.getAttribute("idUsuario")).thenReturn(null);
        ModelAndView mav1 = controladorValoracion.enviarValoracionUnitaria(new ValoracionIndividualInputDTO(), VIAJE_ID, sessionMock);
        assertThat(mav1.getViewName(), is("redirect:/login"));

        // Case 2: Rol incorrecto (CONDUCTOR)
        when(sessionMock.getAttribute("idUsuario")).thenReturn(1L);
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");
        ModelAndView mav2 = controladorValoracion.enviarValoracionUnitaria(new ValoracionIndividualInputDTO(), VIAJE_ID, sessionMock);
        assertThat(mav2.getViewName(), is("redirect:/login"));

        verify(servicioValoracionMock, never()).valorarUsuario(any(), any(), anyLong());
    }

    @Test
    void enviarValoracionUnitaria_deberiaMostrarErrorYRecargarFormularioSiExcepcionDeNegocio() throws Exception {
        // Arrange
        Long viajeroId = 5L;
        ValoracionIndividualInputDTO valoracionDto = new ValoracionIndividualInputDTO(2L, 5, "Muy bien");
        Usuario emisorMock = mock(Usuario.class);
        Viaje viajeMock = crearViajeMockConConductor(2L, "Juan Perez");

        when(sessionMock.getAttribute("idUsuario")).thenReturn(viajeroId);
        when(sessionMock.getAttribute("ROL")).thenReturn("VIAJERO");
        when(servicioValoracionMock.obtenerUsuario(viajeroId)).thenReturn(emisorMock);

        // Simular falla de negocio (ej: ya valoró)
        doThrow(new DatoObligatorioException("Ya valoraste este viaje"))
                .when(servicioValoracionMock).valorarUsuario(eq(emisorMock), eq(valoracionDto), eq(VIAJE_ID));

        // Simular que la recarga de la vista tiene éxito
        when(servicioViajeMock.obtenerViajePorId(VIAJE_ID)).thenReturn(viajeMock);

        // Act
        ModelAndView mav = controladorValoracion.enviarValoracionUnitaria(valoracionDto, VIAJE_ID, sessionMock);

        // Assert
        assertThat(mav.getViewName(), is("valorarConductor"));
        assertThat(mav.getModel().get("error").toString(), is("Ya valoraste este viaje"));
        verify(servicioValoracionMock, times(1)).valorarUsuario(emisorMock, valoracionDto, VIAJE_ID);
        verify(servicioViajeMock, times(1)).obtenerViajePorId(VIAJE_ID); // Para la recarga de la vista
    }

    @Test
    void enviarValoracionUnitaria_deberiaMostrarErrorCriticoSiFallaRecargarVistaDespuesDeErrorDeNegocio() throws Exception {
        // Arrange
        Long viajeroId = 5L;
        ValoracionIndividualInputDTO valoracionDto = new ValoracionIndividualInputDTO(2L, 5, "Muy bien");
        Usuario emisorMock = mock(Usuario.class);

        when(sessionMock.getAttribute("idUsuario")).thenReturn(viajeroId);
        when(sessionMock.getAttribute("ROL")).thenReturn("VIAJERO");
        when(servicioValoracionMock.obtenerUsuario(viajeroId)).thenReturn(emisorMock);

        // Simular falla de negocio
        doThrow(new UsuarioInexistente("Conductor no existe")).when(servicioValoracionMock).valorarUsuario(any(), any(), anyLong());

        // Simular que la recarga de la vista FALLA (cae al catch anidado)
        doThrow(new RuntimeException("Error DB al buscar viaje")).when(servicioViajeMock).obtenerViajePorId(VIAJE_ID);

        // Act
        ModelAndView mav = controladorValoracion.enviarValoracionUnitaria(valoracionDto, VIAJE_ID, sessionMock);

        // Assert
        assertThat(mav.getViewName(), is("error"));
        assertThat(mav.getModel().get("error").toString(), is("Error crítico al procesar la valoración."));
        verify(servicioValoracionMock, times(1)).valorarUsuario(emisorMock, valoracionDto, VIAJE_ID);
        verify(servicioViajeMock, times(1)).obtenerViajePorId(VIAJE_ID); // Se intenta recargar
    }

    @Test
    void enviarValoracionUnitaria_deberiaMostrarErrorInesperadoSiExcepcionGenerica() throws Exception {
        // Arrange
        Long viajeroId = 5L;
        ValoracionIndividualInputDTO valoracionDto = new ValoracionIndividualInputDTO(2L, 5, "Genial");
        Usuario emisorMock = mock(Usuario.class);

        when(sessionMock.getAttribute("idUsuario")).thenReturn(viajeroId);
        when(sessionMock.getAttribute("ROL")).thenReturn("VIAJERO");
        when(servicioValoracionMock.obtenerUsuario(viajeroId)).thenReturn(emisorMock);
        // Simular excepción inesperada
        doThrow(new RuntimeException("Error de base de datos"))
                .when(servicioValoracionMock).valorarUsuario(eq(emisorMock), any(ValoracionIndividualInputDTO.class), eq(VIAJE_ID));

        // Act
        ModelAndView mav = controladorValoracion.enviarValoracionUnitaria(valoracionDto, VIAJE_ID, sessionMock);

        // Assert
        assertThat(mav.getViewName(), is("error"));
        assertThat(mav.getModel().get("error").toString(), containsString("Ocurrió un error inesperado"));
    }

    // --- Métodos Auxiliares ---
    private Viaje crearViajeMockConConductor(Long conductorId, String nombreConductor) {
        Viaje viajeMock = new Viaje();
        Conductor conductorMock = new Conductor();
        conductorMock.setId(conductorId);
        conductorMock.setNombre(nombreConductor);
        viajeMock.setConductor(conductorMock);
        return viajeMock;
    }
}