package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Vehiculo;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Enums.EstadoVerificacion;
import com.tallerwebi.dominio.IServicio.ServicioVehiculo;
import com.tallerwebi.dominio.IServicio.ServicioViaje;
import com.tallerwebi.dominio.excepcion.AsientosDisponiblesMayorQueTotalesDelVehiculoException;
import com.tallerwebi.dominio.excepcion.DatoObligatorioException;
import com.tallerwebi.dominio.excepcion.NotFoundException;
import com.tallerwebi.dominio.excepcion.UsuarioNoAutorizadoException;
import com.tallerwebi.presentacion.Controller.ControladorViaje;
import com.tallerwebi.presentacion.DTO.InputsDTO.ViajeInputDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ControladorViajeTest {

    private ControladorViaje controladorViaje;
    private ServicioViaje servicioViajeMock;
    private ServicioVehiculo servicioVehiculoMock;
    private HttpSession sessionMock;

    @BeforeEach
    public void init() {
        servicioViajeMock = mock(ServicioViaje.class);
        servicioVehiculoMock = mock(ServicioVehiculo.class);
        controladorViaje = new ControladorViaje(servicioViajeMock, servicioVehiculoMock);
        sessionMock = mock(HttpSession.class);
    }

    @Test
    public void deberiaRetornarFormularioConVehiculosCuandoConductorLogueado() {
        // given
        Long conductorId = 1L;
        Conductor conductor = new Conductor(conductorId, "Juan", "juan@test.com", "pass", LocalDate.now().plusDays(30), new ArrayList<>(), new ArrayList<>());

        Vehiculo vehiculo1 = new Vehiculo(1L, "ABC123", "Toyota Corolla", "2020", 5, EstadoVerificacion.VERIFICADO, conductor);
        Vehiculo vehiculo2 = new Vehiculo(2L, "DEF456", "Honda Civic", "2019", 5, EstadoVerificacion.VERIFICADO, conductor);
        List<Vehiculo> vehiculos = Arrays.asList(vehiculo1, vehiculo2);

        when(sessionMock.getAttribute("usuarioId")).thenReturn(conductorId);
        when(sessionMock.getAttribute("rol")).thenReturn("CONDUCTOR");
        when(servicioVehiculoMock.obtenerVehiculosParaConductor(conductorId)).thenReturn(vehiculos);

        // when
        ModelAndView mav = controladorViaje.irAPublicarViaje(sessionMock);

        // then
        assertThat(mav.getViewName(), equalTo("publicarViaje"));
        assertThat(mav.getModel().containsKey("viaje"), equalTo(true));
        assertThat(mav.getModel().containsKey("vehiculos"), equalTo(true));

        ViajeInputDTO viajeDTO = (ViajeInputDTO) mav.getModel().get("viaje");
        assertThat(viajeDTO.getConductorId(), equalTo(conductorId));

        @SuppressWarnings("unchecked")
        List<Vehiculo> vehiculosEnModelo = (List<Vehiculo>) mav.getModel().get("vehiculos");
        assertThat(vehiculosEnModelo, hasSize(2));
        assertThat(vehiculosEnModelo.get(0).getPatente(), equalTo("ABC123"));
        assertThat(vehiculosEnModelo.get(1).getPatente(), equalTo("DEF456"));

        verify(servicioVehiculoMock, times(1)).obtenerVehiculosParaConductor(conductorId);
    }

    @Test
    public void deberiaRedirigirALoginSiNoHaySesion() {
        // given
        when(sessionMock.getAttribute("usuarioId")).thenReturn(null);

        // when
        ModelAndView mav = controladorViaje.irAPublicarViaje(sessionMock);

        // then
        assertThat(mav.getViewName(), equalTo("redirect:/conductor/login"));
        verify(servicioVehiculoMock, never()).obtenerVehiculosParaConductor(anyLong());
    }

    @Test
    public void deberiaRedirigirALoginSiRolNoEsConductor() {
        // given
        when(sessionMock.getAttribute("usuarioId")).thenReturn(1L);
        when(sessionMock.getAttribute("rol")).thenReturn("VIAJERO");

        // when
        ModelAndView mav = controladorViaje.irAPublicarViaje(sessionMock);

        // then
        assertThat(mav.getViewName(), equalTo("redirect:/conductor/login"));
        verify(servicioVehiculoMock, never()).obtenerVehiculosParaConductor(anyLong());
    }

    @Test
    public void deberiaPublicarViajeExitosamente() throws Exception {
        // given
        Long conductorId = 1L;
        ViajeInputDTO viajeInputDTO = new ViajeInputDTO();
        viajeInputDTO.setIdVehiculo(1L);
        viajeInputDTO.setFechaHoraDeSalida(LocalDateTime.now().plusDays(1));
        viajeInputDTO.setPrecio(1500.0);
        viajeInputDTO.setAsientosDisponibles(3);

        when(sessionMock.getAttribute("usuarioId")).thenReturn(conductorId);
        when(sessionMock.getAttribute("rol")).thenReturn("CONDUCTOR");
        doNothing().when(servicioViajeMock).publicarViaje(any(Viaje.class), anyLong(), anyLong());

        // when
        ModelAndView mav = controladorViaje.publicarViaje(viajeInputDTO, sessionMock);

        // then
        assertThat(mav.getViewName(), equalTo("redirect:/conductor/home"));
        verify(servicioViajeMock, times(1)).publicarViaje(any(Viaje.class), eq(conductorId), eq(1L));
    }

    @Test
    public void deberiaRedirigirALoginEnPostSiNoHaySesion() throws Exception {
        // given
        ViajeInputDTO viajeInputDTO = new ViajeInputDTO();
        when(sessionMock.getAttribute("usuarioId")).thenReturn(null);

        // when
        ModelAndView mav = controladorViaje.publicarViaje(viajeInputDTO, sessionMock);

        // then
        assertThat(mav.getViewName(), equalTo("redirect:/conductor/login"));
        verify(servicioViajeMock, never()).publicarViaje(any(Viaje.class), anyLong(), anyLong());
    }

    @Test
    public void deberiaRedirigirALoginEnPostSiRolNoEsConductor() throws Exception {
        // given
        ViajeInputDTO viajeInputDTO = new ViajeInputDTO();
        when(sessionMock.getAttribute("usuarioId")).thenReturn(1L);
        when(sessionMock.getAttribute("rol")).thenReturn("VIAJERO");

        // when
        ModelAndView mav = controladorViaje.publicarViaje(viajeInputDTO, sessionMock);

        // then
        assertThat(mav.getViewName(), equalTo("redirect:/conductor/login"));
        verify(servicioViajeMock, never()).publicarViaje(any(Viaje.class), anyLong(), anyLong());
    }

    @Test
    public void deberiaMostrarErrorSiVehiculoNoPerteneceConductor() throws Exception {
        // given
        Long conductorId = 1L;
        ViajeInputDTO viajeInputDTO = new ViajeInputDTO();
        viajeInputDTO.setIdVehiculo(999L);
        viajeInputDTO.setFechaHoraDeSalida(LocalDateTime.now().plusDays(1));
        viajeInputDTO.setPrecio(1500.0);
        viajeInputDTO.setAsientosDisponibles(3);

        Conductor conductor = new Conductor(conductorId, "Juan", "juan@test.com", "pass", LocalDate.now().plusDays(30), new ArrayList<>(), new ArrayList<>());
        Vehiculo vehiculo = new Vehiculo(1L, "ABC123", "Toyota Corolla", "2020", 5, EstadoVerificacion.VERIFICADO, conductor);
        List<Vehiculo> vehiculos = Arrays.asList(vehiculo);

        when(sessionMock.getAttribute("usuarioId")).thenReturn(conductorId);
        when(sessionMock.getAttribute("rol")).thenReturn("CONDUCTOR");
        when(servicioVehiculoMock.obtenerVehiculosParaConductor(conductorId)).thenReturn(vehiculos);
        doThrow(new UsuarioNoAutorizadoException("El vehículo seleccionado no pertenece al conductor"))
            .when(servicioViajeMock).publicarViaje(any(Viaje.class), anyLong(), anyLong());

        // when
        ModelAndView mav = controladorViaje.publicarViaje(viajeInputDTO, sessionMock);

        // then
        assertThat(mav.getViewName(), equalTo("publicarViaje"));
        assertThat(mav.getModel().containsKey("error"), equalTo(true));
        assertThat(mav.getModel().get("error").toString(),
            equalTo("El vehículo seleccionado no pertenece al conductor"));
        assertThat(mav.getModel().containsKey("vehiculos"), equalTo(true));
        assertThat(mav.getModel().containsKey("viaje"), equalTo(true));
    }

    @Test
    public void deberiaMostrarErrorSiAsientosInvalidos() throws Exception {
        // given
        Long conductorId = 1L;
        ViajeInputDTO viajeInputDTO = new ViajeInputDTO();
        viajeInputDTO.setIdVehiculo(1L);
        viajeInputDTO.setFechaHoraDeSalida(LocalDateTime.now().plusDays(1));
        viajeInputDTO.setPrecio(1500.0);
        viajeInputDTO.setAsientosDisponibles(10);

        Conductor conductor = new Conductor(conductorId, "Juan", "juan@test.com", "pass", LocalDate.now().plusDays(30), new ArrayList<>(), new ArrayList<>());
        Vehiculo vehiculo = new Vehiculo(1L, "ABC123", "Toyota Corolla", "2020", 5, EstadoVerificacion.VERIFICADO, conductor);
        List<Vehiculo> vehiculos = Arrays.asList(vehiculo);

        when(sessionMock.getAttribute("usuarioId")).thenReturn(conductorId);
        when(sessionMock.getAttribute("rol")).thenReturn("CONDUCTOR");
        when(servicioVehiculoMock.obtenerVehiculosParaConductor(conductorId)).thenReturn(vehiculos);
        doThrow(new AsientosDisponiblesMayorQueTotalesDelVehiculoException("Los asientos disponibles no pueden ser mayores a 4"))
            .when(servicioViajeMock).publicarViaje(any(Viaje.class), anyLong(), anyLong());

        // when
        ModelAndView mav = controladorViaje.publicarViaje(viajeInputDTO, sessionMock);

        // then
        assertThat(mav.getViewName(), equalTo("publicarViaje"));
        assertThat(mav.getModel().containsKey("error"), equalTo(true));
        assertThat(mav.getModel().get("error").toString(),
            containsString("Los asientos disponibles no pueden ser mayores a 4"));
    }

    @Test
    public void deberiaMostrarErrorSiDatosObligatoriosFaltan() throws Exception {
        // given
        Long conductorId = 1L;
        ViajeInputDTO viajeInputDTO = new ViajeInputDTO();
        viajeInputDTO.setIdVehiculo(1L);
        viajeInputDTO.setFechaHoraDeSalida(LocalDateTime.now().plusDays(1));
        viajeInputDTO.setPrecio(null); // precio obligatorio faltante
        viajeInputDTO.setAsientosDisponibles(3);

        Conductor conductor = new Conductor(conductorId, "Juan", "juan@test.com", "pass", LocalDate.now().plusDays(30), new ArrayList<>(), new ArrayList<>());
        Vehiculo vehiculo = new Vehiculo(1L, "ABC123", "Toyota Corolla", "2020", 5, EstadoVerificacion.VERIFICADO, conductor);
        List<Vehiculo> vehiculos = Arrays.asList(vehiculo);

        when(sessionMock.getAttribute("usuarioId")).thenReturn(conductorId);
        when(sessionMock.getAttribute("rol")).thenReturn("CONDUCTOR");
        when(servicioVehiculoMock.obtenerVehiculosParaConductor(conductorId)).thenReturn(vehiculos);
        doThrow(new DatoObligatorioException("El precio debe ser mayor a 0"))
            .when(servicioViajeMock).publicarViaje(any(Viaje.class), anyLong(), anyLong());

        // when
        ModelAndView mav = controladorViaje.publicarViaje(viajeInputDTO, sessionMock);

        // then
        assertThat(mav.getViewName(), equalTo("publicarViaje"));
        assertThat(mav.getModel().containsKey("error"), equalTo(true));
        assertThat(mav.getModel().get("error").toString(),
            equalTo("El precio debe ser mayor a 0"));
    }

    @Test
    public void deberiaMostrarErrorSiVehiculoNoExiste() throws Exception {
        // given
        Long conductorId = 1L;
        ViajeInputDTO viajeInputDTO = new ViajeInputDTO();
        viajeInputDTO.setIdVehiculo(999L);
        viajeInputDTO.setFechaHoraDeSalida(LocalDateTime.now().plusDays(1));
        viajeInputDTO.setPrecio(1500.0);
        viajeInputDTO.setAsientosDisponibles(3);

        when(sessionMock.getAttribute("usuarioId")).thenReturn(conductorId);
        when(sessionMock.getAttribute("rol")).thenReturn("CONDUCTOR");
        when(servicioVehiculoMock.obtenerVehiculosParaConductor(conductorId)).thenReturn(new ArrayList<>());
        doThrow(new NotFoundException("No se encontró un vehículo con el ID: 999"))
            .when(servicioViajeMock).publicarViaje(any(Viaje.class), anyLong(), anyLong());

        // when
        ModelAndView mav = controladorViaje.publicarViaje(viajeInputDTO, sessionMock);

        // then
        assertThat(mav.getViewName(), equalTo("publicarViaje"));
        assertThat(mav.getModel().containsKey("error"), equalTo(true));
        assertThat(mav.getModel().get("error").toString(),
            containsString("No se encontró un vehículo"));
    }

    @Test
    public void deberiaMostrarErrorSiFechaEsPasada() throws Exception {
        // given
        Long conductorId = 1L;
        ViajeInputDTO viajeInputDTO = new ViajeInputDTO();
        viajeInputDTO.setIdVehiculo(1L);
        viajeInputDTO.setFechaHoraDeSalida(LocalDateTime.now().minusDays(1)); // Fecha pasada
        viajeInputDTO.setPrecio(1500.0);
        viajeInputDTO.setAsientosDisponibles(3);

        Conductor conductor = new Conductor(conductorId, "Juan", "juan@test.com", "pass", LocalDate.now().plusDays(30), new ArrayList<>(), new ArrayList<>());
        Vehiculo vehiculo = new Vehiculo(1L, "ABC123", "Toyota Corolla", "2020", 5, EstadoVerificacion.VERIFICADO, conductor);
        List<Vehiculo> vehiculos = Arrays.asList(vehiculo);

        when(sessionMock.getAttribute("usuarioId")).thenReturn(conductorId);
        when(sessionMock.getAttribute("rol")).thenReturn("CONDUCTOR");
        when(servicioVehiculoMock.obtenerVehiculosParaConductor(conductorId)).thenReturn(vehiculos);
        doThrow(new DatoObligatorioException("La fecha y hora de salida debe ser mayor a la fecha actual"))
            .when(servicioViajeMock).publicarViaje(any(Viaje.class), anyLong(), anyLong());

        // when
        ModelAndView mav = controladorViaje.publicarViaje(viajeInputDTO, sessionMock);

        // then
        assertThat(mav.getViewName(), equalTo("publicarViaje"));
        assertThat(mav.getModel().containsKey("error"), equalTo(true));
        assertThat(mav.getModel().get("error").toString(),
            equalTo("La fecha y hora de salida debe ser mayor a la fecha actual"));
    }

    @Test
    public void deberiaSetearConductorIdDesdeSesionPorSeguridad() throws Exception {
        // given
        Long conductorIdEnSesion = 1L;
        Long conductorIdEnDTO = 999L; // Intentando cambiar el ID

        ViajeInputDTO viajeInputDTO = new ViajeInputDTO();
        viajeInputDTO.setConductorId(conductorIdEnDTO);
        viajeInputDTO.setIdVehiculo(1L);
        viajeInputDTO.setFechaHoraDeSalida(LocalDateTime.now().plusDays(1));
        viajeInputDTO.setPrecio(1500.0);
        viajeInputDTO.setAsientosDisponibles(3);

        when(sessionMock.getAttribute("usuarioId")).thenReturn(conductorIdEnSesion);
        when(sessionMock.getAttribute("rol")).thenReturn("CONDUCTOR");
        doNothing().when(servicioViajeMock).publicarViaje(any(Viaje.class), anyLong(), anyLong());

        // when
        controladorViaje.publicarViaje(viajeInputDTO, sessionMock);

        // then
        // Verificar que se llama con el conductorId de la sesión, no del DTO
        verify(servicioViajeMock, times(1)).publicarViaje(any(Viaje.class), eq(conductorIdEnSesion), eq(1L));
    }
}
