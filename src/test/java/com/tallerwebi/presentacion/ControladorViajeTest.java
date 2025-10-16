package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Entity.Ciudad;
import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.Entity.Vehiculo;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Enums.EstadoDeViaje;
import com.tallerwebi.dominio.Enums.EstadoVerificacion;
import com.tallerwebi.dominio.IServicio.ServicioCiudad;
import com.tallerwebi.dominio.IServicio.ServicioNominatim;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import com.tallerwebi.dominio.IServicio.ServicioVehiculo;
import com.tallerwebi.dominio.IServicio.ServicioViaje;
import com.tallerwebi.dominio.excepcion.AsientosDisponiblesMayorQueTotalesDelVehiculoException;
import com.tallerwebi.dominio.excepcion.DatoObligatorioException;
import com.tallerwebi.dominio.excepcion.NotFoundException;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.dominio.excepcion.UsuarioNoAutorizadoException;
import com.tallerwebi.dominio.excepcion.ViajeNoCancelableException;
import com.tallerwebi.dominio.excepcion.ViajeNoEncontradoException;
import com.tallerwebi.presentacion.Controller.ControladorViaje;
import com.tallerwebi.presentacion.DTO.InputsDTO.ViajeInputDTO;
import com.tallerwebi.presentacion.DTO.NominatimResponse;
import com.tallerwebi.presentacion.DTO.OutputsDTO.DetalleViajeOutputDTO;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ControladorViajeTest {

    private ControladorViaje controladorViaje;
    private ServicioViaje servicioViajeMock;
    private ServicioVehiculo servicioVehiculoMock;
    private ServicioNominatim servicioNominatimMock;
    private ServicioCiudad servicioCiudadMock;
    private ServicioConductor servicioConductorMock;
    private HttpSession sessionMock;

    @BeforeEach
    public void init() throws Exception {
        servicioViajeMock = mock(ServicioViaje.class);
        servicioVehiculoMock = mock(ServicioVehiculo.class);
        servicioNominatimMock = mock(ServicioNominatim.class);
        servicioCiudadMock = mock(ServicioCiudad.class);
        servicioConductorMock = mock(ServicioConductor.class);
        controladorViaje = new ControladorViaje(servicioViajeMock, servicioVehiculoMock, servicioNominatimMock, servicioCiudadMock, servicioConductorMock);
        sessionMock = mock(HttpSession.class);

        // Setup mocks por defecto para Nominatim y Ciudad (se pueden sobrescribir en tests específicos)
        NominatimResponse origenResponse = new NominatimResponse("Buenos Aires", "-34.6037", "-58.3816", 1L, "city");
        NominatimResponse destinoResponse = new NominatimResponse("Córdoba", "-31.4201", "-64.1888", 2L, "city");

        when(servicioNominatimMock.buscarCiudadPorInputCompleto("Buenos Aires")).thenReturn(origenResponse);
        when(servicioNominatimMock.buscarCiudadPorInputCompleto("Córdoba")).thenReturn(destinoResponse);

        Ciudad buenosAires = new Ciudad(1L, "Buenos Aires", -34.6037f, -58.3816f);
        Ciudad cordoba = new Ciudad(2L, "Córdoba", -31.4201f, -64.1888f);

        when(servicioCiudadMock.guardarCiudad(any(Ciudad.class))).thenAnswer(invocation -> {
            Ciudad ciudad = invocation.getArgument(0);
            if (ciudad.getId() == null) {
                ciudad.setId(1L);
            }
            return ciudad;
        });
    }

    // Helper method para agregar ciudades por defecto a un DTO
    private void agregarCiudadesAlDTO(ViajeInputDTO dto) {
        dto.setNombreCiudadOrigen("Buenos Aires");
        dto.setNombreCiudadDestino("Córdoba");
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
        viajeInputDTO.setNombreCiudadOrigen("Buenos Aires");
        viajeInputDTO.setNombreCiudadDestino("Córdoba");

        when(sessionMock.getAttribute("usuarioId")).thenReturn(conductorId);
        when(sessionMock.getAttribute("rol")).thenReturn("CONDUCTOR");
        doNothing().when(servicioViajeMock).publicarViaje(any(Viaje.class), anyLong(), anyLong());

        // when
        ModelAndView mav = controladorViaje.publicarViaje(viajeInputDTO, sessionMock);

        // then
        assertThat(mav.getViewName(), equalTo("redirect:/conductor/home"));
        verify(servicioViajeMock, times(1)).publicarViaje(any(Viaje.class), eq(conductorId), eq(1L));
        verify(servicioNominatimMock, times(1)).buscarCiudadPorInputCompleto("Buenos Aires");
        verify(servicioNominatimMock, times(1)).buscarCiudadPorInputCompleto("Córdoba");
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
        agregarCiudadesAlDTO(viajeInputDTO);

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
        agregarCiudadesAlDTO(viajeInputDTO);

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
        agregarCiudadesAlDTO(viajeInputDTO);

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
        agregarCiudadesAlDTO(viajeInputDTO);

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
        agregarCiudadesAlDTO(viajeInputDTO);

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
        viajeInputDTO.setNombreCiudadOrigen("Buenos Aires");
        viajeInputDTO.setNombreCiudadDestino("Córdoba");

        when(sessionMock.getAttribute("usuarioId")).thenReturn(conductorIdEnSesion);
        when(sessionMock.getAttribute("rol")).thenReturn("CONDUCTOR");
        doNothing().when(servicioViajeMock).publicarViaje(any(Viaje.class), anyLong(), anyLong());

        // when
        controladorViaje.publicarViaje(viajeInputDTO, sessionMock);

        // then
        // Verificar que se llama con el conductorId de la sesión, no del DTO
        verify(servicioViajeMock, times(1)).publicarViaje(any(Viaje.class), eq(conductorIdEnSesion), eq(1L));
    }

    /*@Test
    public void deberiaCancelarViajeExitosamente() throws Exception {
        // given
        Long viajeId = 1L;
        Long conductorId = 10L;

        when(sessionMock.getAttribute("usuarioId")).thenReturn(conductorId);
        when(sessionMock.getAttribute("rol")).thenReturn("CONDUCTOR");

        doNothing().when(servicioViajeMock).cancelarViaje(eq(viajeId), any(Usuario.class));

        // when
        ModelAndView mav = controladorViaje.cancelarViaje(viajeId, sessionMock);

        // then
        assertThat(mav.getViewName(), equalTo("redirect:/viaje/listar"));
        assertThat(mav.getModel().containsKey("exito"), equalTo(true));
        assertThat(mav.getModel().get("exito").toString(),equalTo("El viaje fue cancelado exitosamente."));

        verify(servicioViajeMock, times(1)).cancelarViaje(eq(viajeId), any(Usuario.class));
    }*/

    @Test
    public void deberiaRedirigirALoginSiNoHaySesionAlCancelar() throws ViajeNoEncontradoException, UsuarioNoAutorizadoException, ViajeNoCancelableException {
        // given
        when(sessionMock.getAttribute("usuarioId")).thenReturn(null);

        // when
        ModelAndView mav = controladorViaje.cancelarViaje(1L, sessionMock);

        // then
        assertThat(mav.getViewName(), equalTo("redirect:/conductor/login"));
        verify(servicioViajeMock, never()).cancelarViaje(anyLong(), any(Usuario.class));
    }

    @Test
    public void deberiaRedirigirALoginSiRolNoEsConductorAlCancelar() throws ViajeNoEncontradoException, UsuarioNoAutorizadoException, ViajeNoCancelableException {
        // given
        when(sessionMock.getAttribute("usuarioId")).thenReturn(1L);
        when(sessionMock.getAttribute("rol")).thenReturn("VIAJERO");

        // when
        ModelAndView mav = controladorViaje.cancelarViaje(1L, sessionMock);

        // then
        assertThat(mav.getViewName(), equalTo("redirect:/conductor/login"));
        verify(servicioViajeMock, never()).cancelarViaje(anyLong(), any(Usuario.class));
    }

    @Test
    public void deberiaMostrarErrorSiViajeNoEncontrado() throws Exception {
        // given
        Long viajeId = 5L;
        Long conductorId = 1L;

        when(sessionMock.getAttribute("usuarioId")).thenReturn(conductorId);
        when(sessionMock.getAttribute("rol")).thenReturn("CONDUCTOR");

        doThrow(new ViajeNoEncontradoException("No se encontró el viaje"))
            .when(servicioViajeMock).cancelarViaje(eq(viajeId), any(Usuario.class));

        // when
        ModelAndView mav = controladorViaje.cancelarViaje(viajeId, sessionMock);

        // then
        assertThat(mav.getViewName(), equalTo("errorCancelarViaje"));
        assertThat(mav.getModel().containsKey("error"), equalTo(true));
        assertThat(mav.getModel().get("error").toString(), containsString("No se encontró el viaje"));

        verify(servicioViajeMock, times(1)).cancelarViaje(eq(viajeId), any(Usuario.class));
    }

    @Test
    public void deberiaMostrarErrorSiUsuarioNoAutorizadoAlCancelar() throws Exception {
        // given
        Long viajeId = 3L;
        Long conductorId = 1L;

        when(sessionMock.getAttribute("usuarioId")).thenReturn(conductorId);
        when(sessionMock.getAttribute("rol")).thenReturn("CONDUCTOR");

        doThrow(new UsuarioNoAutorizadoException("No tiene permisos para cancelar este viaje."))
            .when(servicioViajeMock).cancelarViaje(eq(viajeId), any(Usuario.class));

        // when
        ModelAndView mav = controladorViaje.cancelarViaje(viajeId, sessionMock);

        // then
        assertThat(mav.getViewName(), equalTo("errorCancelarViaje"));
        assertThat(mav.getModel().containsKey("error"), equalTo(true));
        assertThat(mav.getModel().get("error").toString(),
            equalTo("No tiene permisos para cancelar este viaje."));
    }

    @Test
    public void deberiaMostrarErrorSiViajeNoCancelable() throws Exception {
        // given
        Long viajeId = 7L;
        Long conductorId = 2L;

        when(sessionMock.getAttribute("usuarioId")).thenReturn(conductorId);
        when(sessionMock.getAttribute("rol")).thenReturn("CONDUCTOR");

        doThrow(new ViajeNoCancelableException("El viaje no se puede cancelar en este estado."))
            .when(servicioViajeMock).cancelarViaje(eq(viajeId), any(Usuario.class));

        // when
        ModelAndView mav = controladorViaje.cancelarViaje(viajeId, sessionMock);

        // then
        assertThat(mav.getViewName(), equalTo("errorCancelarViaje"));
        assertThat(mav.getModel().containsKey("error"), equalTo(true));
        assertThat(mav.getModel().get("error").toString(),
            equalTo("El viaje no se puede cancelar en este estado."));
    }


    @Test
    public void deberiaMostrarErrorSiNoHaySesionAlListar() throws Exception {
        // GIVEN
        when(sessionMock.getAttribute("usuarioId")).thenReturn(null);
        when(sessionMock.getAttribute("rol")).thenReturn("CONDUCTOR");

        // WHEN
        ModelAndView mav = controladorViaje.listarViajes(sessionMock);

        // THEN
        assertThat(mav.getViewName(), equalTo("errorAcceso"));
        assertThat(mav.getModel().get("error").toString(), containsString("Debés iniciar sesión como conductor"));

        // CORRECCIÓN: Usamos eq(null) o verificamos que nunca se llamó con cualquier Long.
        // Si el problema es anyLong(), lo cambiamos por un valor explícito o null.
        // Si el flujo de código se corta antes de castear, usamos Long.class.
        verify(servicioConductorMock, never()).obtenerConductor(any(Long.class));
        verify(servicioViajeMock, never()).listarViajesPorConductor(any());
    }


    @Test
    public void deberiaMostrarErrorSiConductorDeSesionNoExiste() throws Exception {
        Long conductorId = 1L;

        when(sessionMock.getAttribute("usuarioId")).thenReturn(conductorId);
        when(sessionMock.getAttribute("rol")).thenReturn("CONDUCTOR");

        // Asumiendo que UsuarioInexistente es tu excepción de negocio que extiende Exception
        when(servicioConductorMock.obtenerConductor(conductorId))
                .thenThrow(new UsuarioInexistente("El conductor de la sesión no fue encontrado."));

        ModelAndView mav = controladorViaje.listarViajes(sessionMock);

        assertThat(mav.getViewName(), equalTo("errorAcceso"));
        assertThat(mav.getModel().get("error").toString(), containsString("El conductor de la sesión no fue encontrado."));
        verify(servicioViajeMock, never()).listarViajesPorConductor(any());
        verify(servicioConductorMock).obtenerConductor(conductorId);
    }

    @Test
    public void deberiaListarViajesCorrectamente() throws Exception {
        Long conductorId = 1L;

        when(sessionMock.getAttribute("usuarioId")).thenReturn(conductorId);
        when(sessionMock.getAttribute("rol")).thenReturn("CONDUCTOR");

        Conductor conductorMock = new Conductor();
        conductorMock.setId(conductorId);

        when(servicioConductorMock.obtenerConductor(conductorId)).thenReturn(conductorMock);

        Viaje viaje1 = new Viaje();
        viaje1.setId(10L);
        viaje1.setOrigen(new Ciudad(null, "Morón", 0f, 0f));
        viaje1.setDestino(new Ciudad(null, "Lanús", 0f, 0f));
        viaje1.setVehiculo(new Vehiculo());
        viaje1.setFechaHoraDeSalida(LocalDateTime.now().plusDays(1));
        viaje1.setEstado(EstadoDeViaje.DISPONIBLE);
        viaje1.setAsientosDisponibles(3);

        List<Viaje> viajes = List.of(viaje1);

        when(servicioViajeMock.listarViajesPorConductor(any(Conductor.class))).thenReturn(viajes);

        ModelAndView mav = controladorViaje.listarViajes(sessionMock);

        assertThat(mav.getViewName(), equalTo("listarViajesConductor"));
        assertThat(mav.getModel().containsKey("listaViajes"), equalTo(true));
        assertThat(((List<?>) mav.getModel().get("listaViajes")).size(), equalTo(1));

        verify(servicioConductorMock).obtenerConductor(conductorId);
        verify(servicioViajeMock).listarViajesPorConductor(any(Conductor.class));
    }


    @Test
    public void deberiaMostrarErrorSiUsuarioNoAutorizadoAlListar() throws Exception {
        Long conductorId = 1L;

        when(sessionMock.getAttribute("usuarioId")).thenReturn(conductorId);
        when(sessionMock.getAttribute("rol")).thenReturn("CONDUCTOR");

        Conductor conductorMock = new Conductor();
        conductorMock.setId(conductorId);

        when(servicioConductorMock.obtenerConductor(conductorId)).thenReturn(conductorMock);

        when(servicioViajeMock.listarViajesPorConductor(any(Conductor.class)))
                .thenThrow(new UsuarioNoAutorizadoException("No tenés permisos para ver los viajes"));

        ModelAndView mav = controladorViaje.listarViajes(sessionMock);

        assertThat(mav.getViewName(), equalTo("errorAcceso"));
        assertThat(mav.getModel().get("error").toString(), containsString("No tenés permisos para ver los viajes"));

        verify(servicioConductorMock).obtenerConductor(conductorId);
        verify(servicioViajeMock).listarViajesPorConductor(any(Conductor.class));
    }

    @Test
    public void deberiaDevolverListaVaciaSiNoHayViajes() throws Exception {
        Long conductorId = 2L;

        when(sessionMock.getAttribute("usuarioId")).thenReturn(conductorId);
        when(sessionMock.getAttribute("rol")).thenReturn("CONDUCTOR");

        Conductor conductorMock = new Conductor();
        conductorMock.setId(conductorId);

        when(servicioConductorMock.obtenerConductor(conductorId)).thenReturn(conductorMock);

        when(servicioViajeMock.listarViajesPorConductor(any(Conductor.class))).thenReturn(List.of());

        ModelAndView mav = controladorViaje.listarViajes(sessionMock);

        assertThat(mav.getViewName(), equalTo("listarViajesConductor"));
        assertThat(mav.getModel().containsKey("listaViajes"), equalTo(true));
        assertThat(((List<?>) mav.getModel().get("listaViajes")).isEmpty(), equalTo(true));

        verify(servicioConductorMock).obtenerConductor(conductorId);
        verify(servicioViajeMock).listarViajesPorConductor(any(Conductor.class));
    }

    @Test
    public void deberiaMostrarErrorSiRolEsConductorPeroSesionEsNull() throws Exception {
        when(sessionMock.getAttribute("usuarioId")).thenReturn(null);
        when(sessionMock.getAttribute("rol")).thenReturn("CONDUCTOR");

        ModelAndView mav = controladorViaje.listarViajes(sessionMock);

        assertThat(mav.getViewName(), equalTo("errorAcceso"));
        assertThat(mav.getModel().get("error").toString(), containsString("Debés iniciar sesión como conductor"));
        // CORRECCIÓN: Si anyLong() falla, usamos any(Long.class) para ser más explícitos con el tipo.
        verify(servicioConductorMock, never()).obtenerConductor(any(Long.class));
        verify(servicioViajeMock, never()).listarViajesPorConductor(any());
    }

    @Test
    public void deberiaMostrarErrorSiIdExistePeroRolEsNull() throws Exception {
        Long conductorId = 3L;

        when(sessionMock.getAttribute("usuarioId")).thenReturn(conductorId);
        when(sessionMock.getAttribute("rol")).thenReturn(null);

        ModelAndView mav = controladorViaje.listarViajes(sessionMock);

        assertThat(mav.getViewName(), equalTo("errorAcceso"));
        assertThat(mav.getModel().get("error").toString(), containsString("Debés iniciar sesión como conductor"));
        // CORRECCIÓN: Si anyLong() falla, usamos any(Long.class).
        verify(servicioConductorMock, never()).obtenerConductor(any(Long.class));
        verify(servicioViajeMock, never()).listarViajesPorConductor(any());
    }

    @Test
    public void deberiaMostrarDetalleDeViajeExistente() throws Exception {
        // given
        Long viajeId = 1L;

        // Crear ciudades de origen y destino
        Ciudad origen = new Ciudad();
        origen.setId(1L);
        origen.setNombre("Buenos Aires");

        Ciudad destino = new Ciudad();
        destino.setId(2L);
        destino.setNombre("Córdoba");

        // Crear vehículo
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(1L);
        vehiculo.setModelo("Toyota Corolla");
        vehiculo.setAnio("2020");
        vehiculo.setPatente("ABC123");
        vehiculo.setAsientosTotales(5);

        // Crear viaje con todos los campos necesarios
        Viaje viajeMock = new Viaje();
        viajeMock.setId(viajeId);
        viajeMock.setPrecio(1500.0);
        viajeMock.setAsientosDisponibles(3);
        viajeMock.setOrigen(origen);
        viajeMock.setDestino(destino);
        viajeMock.setVehiculo(vehiculo);
        viajeMock.setParadas(new ArrayList<>());  // Lista vacía de paradas
        viajeMock.setViajeros(new ArrayList<>()); // Lista vacía de viajeros

        when(servicioViajeMock.obtenerDetalleDeViaje(viajeId)).thenReturn(viajeMock);

        // when
        ModelAndView mav = controladorViaje.verDetalleDeUnViaje(sessionMock, viajeId);

        // then
        assertThat(mav.getViewName(), equalTo("detalleViaje"));
        assertThat(mav.getModel().containsKey("detalle"), equalTo(true));
        assertThat(mav.getModel().get("detalle"), instanceOf(DetalleViajeOutputDTO.class));
        assertThat(mav.getModel().containsKey("error"), equalTo(false));

        verify(servicioViajeMock, times(1)).obtenerDetalleDeViaje(viajeId);
    }

    @Test
    public void deberiaMostrarErrorSiElViajeNoExiste() throws Exception {
        // given
        Long viajeId = 999L;
        when(servicioViajeMock.obtenerDetalleDeViaje(viajeId))
                .thenThrow(new NotFoundException("No se encontró el viaje con ID: " + viajeId));

        // when
        ModelAndView mav = controladorViaje.verDetalleDeUnViaje(sessionMock, viajeId);

        // then
        assertThat(mav.getViewName(), equalTo("detalleViaje"));
        assertThat(mav.getModel().containsKey("error"), equalTo(true));
        assertThat(mav.getModel().get("error").toString(),
                equalTo("No se encontró el viaje con ID: " + viajeId));
        assertThat(mav.getModel().containsKey("detalle"), equalTo(false));
    }



    // Test para cuando se active la validación de rol
    @Test
    public void deberiaRedirigirSiNoEsConductorCuandoSeActiveValidacion() throws Exception {
        // given
        Long viajeId = 1L;
        when(sessionMock.getAttribute("rol")).thenReturn("USUARIO");

        // Descomentar cuando se active la validación
        // when
        // ModelAndView mav = controladorViaje.verDetalleDeUnViaje(sessionMock, viajeId);

        // then
        // assertThat(mav.getViewName(), equalTo("redirect:/conductor/login"));
        // verify(servicioViajeMock, never()).obtenerDetalleDeViaje(anyLong());
    }

    @Test
    public void deberiaMostrarDetalleSiEsConductorCuandoSeActiveValidacion() throws Exception {
        // given
        Long viajeId = 1L;

        // Configurar datos del viaje
        Ciudad origen = new Ciudad(1L, "Buenos Aires", -34.6037f, -58.3816f);
        Ciudad destino = new Ciudad(2L, "Córdoba", -31.4201f, -64.1888f);

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(1L);
        vehiculo.setModelo("Toyota Corolla");
        vehiculo.setAnio("2020");
        vehiculo.setPatente("ABC123");
        vehiculo.setAsientosTotales(5);

        Viaje viajeMock = new Viaje();
        viajeMock.setId(viajeId);
        viajeMock.setPrecio(1500.0);
        viajeMock.setAsientosDisponibles(3);
        viajeMock.setOrigen(origen);
        viajeMock.setDestino(destino);
        viajeMock.setVehiculo(vehiculo);
        viajeMock.setParadas(new ArrayList<>());
        viajeMock.setViajeros(new ArrayList<>());

        when(sessionMock.getAttribute("rol")).thenReturn("CONDUCTOR");
        when(servicioViajeMock.obtenerDetalleDeViaje(viajeId)).thenReturn(viajeMock);

        // Descomentar cuando se active la validación
        // when
        // ModelAndView mav = controladorViaje.verDetalleDeUnViaje(sessionMock, viajeId);

        // then
        // assertThat(mav.getViewName(), equalTo("detalleViaje"));
        // assertThat(mav.getModel().containsKey("detalle"), equalTo(true));
        // verify(servicioViajeMock, times(1)).obtenerDetalleDeViaje(viajeId);
    }

}
