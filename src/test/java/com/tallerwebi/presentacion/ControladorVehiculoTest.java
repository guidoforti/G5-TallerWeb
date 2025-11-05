package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Vehiculo;
import com.tallerwebi.dominio.Enums.EstadoVerificacion;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import com.tallerwebi.dominio.IServicio.ServicioVehiculo;
import com.tallerwebi.dominio.excepcion.NotFoundException;
import com.tallerwebi.dominio.excepcion.PatenteDuplicadaException;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.presentacion.Controller.ControladorVehiculo;
import com.tallerwebi.presentacion.DTO.InputsDTO.VehiculoInputDTO;
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
    private ServicioConductor servicioConductorMock;
    private HttpSession sessionMock;
    private VehiculoInputDTO vehiculoInputDTO;
    private Conductor conductorMock;
    private Vehiculo vehiculoMock;

    @BeforeEach
    public void init() {
        servicioVehiculoMock = mock(ServicioVehiculo.class);
        servicioConductorMock = mock(ServicioConductor.class);
        controladorVehiculo = new ControladorVehiculo(servicioVehiculoMock, servicioConductorMock);
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
        when(servicioVehiculoMock.guardarVehiculo(any(Vehiculo.class))).thenReturn(vehiculoMock);

        ModelAndView modelAndView = controladorVehiculo.registrarVehiculo(vehiculoInputDTO, sessionMock);

        String expectedRedirect = "redirect:/vehiculos/listarVehiculos?mensaje=%C2%A1Veh%C3%ADculo+%27ABC123%27+registrado+con+%C3%A9xito%21";

        assertThat(modelAndView.getViewName(), equalToIgnoringCase(expectedRedirect));
        verify(servicioVehiculoMock, times(1)).guardarVehiculo(any(Vehiculo.class));
    }


    @Test
    public void registrarVehiculo_conPatenteDuplicada_deberiaMostrarError() throws Exception {
        when(sessionMock.getAttribute("ROL")).thenReturn("CONDUCTOR");
        when(sessionMock.getAttribute("idUsuario")).thenReturn(1L);
        when(servicioConductorMock.obtenerConductor(1L)).thenReturn(conductorMock);
        when(servicioVehiculoMock.guardarVehiculo(any(Vehiculo.class)))
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
}
