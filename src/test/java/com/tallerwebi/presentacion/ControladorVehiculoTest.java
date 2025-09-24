package com.tallerwebi.presentacion;

import com.tallerwebi.config.ManualModelMapper;
import com.tallerwebi.dominio.IServicio.ServicioVehiculo;
import com.tallerwebi.dominio.excepcion.NotFoundException;
import com.tallerwebi.dominio.excepcion.PatenteDuplicadaException;
import com.tallerwebi.dominio.excepcion.UsuarioNoAutorizadoException;
import com.tallerwebi.presentacion.Controller.ControladorVehiculo;
import com.tallerwebi.presentacion.DTO.InputsDTO.VehiculoInputDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.VehiculoOutputDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.mockito.Mockito.*;


public class ControladorVehiculoTest {

    private ControladorVehiculo controladorVehiculo;
    private ServicioVehiculo servicioVehiculoMock;
    private HttpSession sessionMock;
    private VehiculoInputDTO vehiculoInputDTO;
    private VehiculoOutputDTO vehiculoOutputDTO;

    @BeforeEach
    public void init() {
        servicioVehiculoMock = mock(ServicioVehiculo.class);
        controladorVehiculo = new ControladorVehiculo(servicioVehiculoMock);
        sessionMock = mock(HttpSession.class);
        vehiculoInputDTO = new VehiculoInputDTO();
        vehiculoOutputDTO = new VehiculoOutputDTO();

        // Configuración común para la sesión
        when(sessionMock.getAttribute("rol")).thenReturn("CONDUCTOR");
        when(sessionMock.getAttribute("usuarioId")).thenReturn(1L);
    }

    @Test
    public void registrarVehiculo_conDatosValidos_deberiaRedirigirAlHome() throws Exception {
        // given
        when(servicioVehiculoMock.guardarVehiculo(any(VehiculoInputDTO.class), anyLong()))
                .thenReturn(vehiculoOutputDTO);

        // when
        ModelAndView modelAndView = controladorVehiculo.registrarVehiculo(vehiculoInputDTO, sessionMock);

        // then
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
        verify(servicioVehiculoMock, times(1)).guardarVehiculo(any(VehiculoInputDTO.class), eq(1L));
    }

    @Test
    public void registrarVehiculo_conPatenteDuplicada_deberiaMostrarError() throws Exception {
        // given
        String mensajeError = "La patente ya está registrada";
        when(servicioVehiculoMock.guardarVehiculo(any(VehiculoInputDTO.class), anyLong()))
                .thenThrow(new PatenteDuplicadaException(mensajeError));

        // when
        ModelAndView modelAndView = controladorVehiculo.registrarVehiculo(vehiculoInputDTO, sessionMock);

        // then
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("registrarVehiculo"));
        assertThat(modelAndView.getModel().get("error"), equalTo(mensajeError));
    }

    @Test
    public void registrarVehiculo_conUsuarioNoAutorizado_deberiaMostrarError() {
        // given
        when(sessionMock.getAttribute("rol")).thenReturn("USUARIO"); // No es CONDUCTOR

        // when
        ModelAndView modelAndView = controladorVehiculo.registrarVehiculo(vehiculoInputDTO, sessionMock);

        // then
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("usuarioNoAutorizado"));
        assertThat(modelAndView.getModel().get("error"), equalTo("no tienes permisos para acceder a este recurso"));
    }

    @Test
    public void mostrarFormularioDeRegistroVehiculo_conUsuarioNoAutorizado_deberiaMostrarError() throws Exception {
        // given
        when(sessionMock.getAttribute("rol")).thenReturn("USUARIO"); // No es CONDUCTOR

        // when
        ModelAndView modelAndView = controladorVehiculo.mostrarFormularioDeRegistroVehiculo(sessionMock);

        // then
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("usuarioNoAutorizado"));
        assertThat(modelAndView.getModel().get("error"), equalTo("no tienes permisos para acceder a este recurso"));
    }
}
