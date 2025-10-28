package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.IServicio.ServicioHistorialReserva;
import com.tallerwebi.presentacion.DTO.OutputsDTO.HistorialReservaDTO;
import com.tallerwebi.dominio.excepcion.UsuarioNoAutorizadoException;
import com.tallerwebi.dominio.excepcion.ViajeNoEncontradoException;
import com.tallerwebi.presentacion.Controller.ControladorHistorialReserva;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*; // Asegúrate de tener esta importación
import static org.mockito.ArgumentMatchers.eq; // Importar explícitamente eq
import static org.mockito.ArgumentMatchers.any; // Importar explícitamente any

public class ControladorHistorialReservaTest {

    private ControladorHistorialReserva historialReservaController;
    private ServicioHistorialReserva servicioHistorialReservaMock;
    private HttpSession sessionMock;
    
    private final Long ID_VIAJE = 10L;
    private final Long ID_CONDUCTOR = 2L;

    @BeforeEach
    void setUp() {
        // Inicialización de mocks
        servicioHistorialReservaMock = mock(ServicioHistorialReserva.class);
        sessionMock = mock(HttpSession.class);
        
        // Inicialización del controlador
        historialReservaController = new ControladorHistorialReserva(servicioHistorialReservaMock);
    }
    
    // --- Casos de Éxito ---

    @Test
    public void deberiaMostrarHistorialDeReservasSiElUsuarioEsElConductorDelViaje() throws Exception {
        // given
        Conductor conductor = new Conductor();
        conductor.setId(ID_CONDUCTOR);
        conductor.setRol("CONDUCTOR");
        
        List<HistorialReservaDTO> historialEsperado = Arrays.asList(
            new HistorialReservaDTO(), new HistorialReservaDTO()
        );

        when(sessionMock.getAttribute("usuario")).thenReturn(conductor);
        // USANDO eq() y any() CONSISTENTEMENTE
        when(servicioHistorialReservaMock.obtenerHistorialPorViaje(eq(ID_VIAJE), any(Usuario.class)))
            .thenReturn(historialEsperado);

        // when
        ModelAndView mav = historialReservaController.verHistorialPorViaje(ID_VIAJE, sessionMock);

        // then
        assertThat(mav.getViewName(), is("historial-reservas"));
        assertThat(mav.getModel().get("historialReservas"), is(historialEsperado));
        // USANDO eq() y any() CONSISTENTEMENTE
        verify(servicioHistorialReservaMock, times(1)).obtenerHistorialPorViaje(eq(ID_VIAJE), any(Usuario.class));
    }

    // --- Casos de Error y Autorización ---

    @Test
    public void siElUsuarioNoEstaEnSesionDeberiaRedirigirALogin() {
        // given
        when(sessionMock.getAttribute("usuario")).thenReturn(null);

        // when
        ModelAndView mav = historialReservaController.verHistorialPorViaje(ID_VIAJE, sessionMock);

        // then
        assertThat(mav.getViewName(), is("redirect:/login"));
        verifyNoInteractions(servicioHistorialReservaMock);
    }
    
    @Test
    public void siElUsuarioEsOtroConductorODiferenteDeberiaDevolverErrorDeAutorizacion() throws Exception {
        // given
        Conductor usuarioNoAutorizado = new Conductor();
        usuarioNoAutorizado.setId(99L); // ID diferente
        usuarioNoAutorizado.setRol("CONDUCTOR"); 
        
        when(sessionMock.getAttribute("usuario")).thenReturn(usuarioNoAutorizado);
        
        // USANDO eq() y any() CONSISTENTEMENTE
        doThrow(new UsuarioNoAutorizadoException("No tenés permisos para ver el historial de este viaje."))
            .when(servicioHistorialReservaMock).obtenerHistorialPorViaje(eq(ID_VIAJE), any(Usuario.class));

        // when
        ModelAndView mav = historialReservaController.verHistorialPorViaje(ID_VIAJE, sessionMock);

        // then
        assertThat(mav.getViewName(), is("error"));
        assertThat(mav.getModel().get("error").toString(), containsString("Error de Acceso:"));
        // USANDO eq() y any() CONSISTENTEMENTE
        verify(servicioHistorialReservaMock, times(1)).obtenerHistorialPorViaje(eq(ID_VIAJE), any(Usuario.class));
    }
    
    @Test
    public void siElViajeNoExisteDeberiaDevolverErrorDeViajeNoEncontrado() throws Exception {
        // given
        Conductor conductor = new Conductor();
        conductor.setId(ID_CONDUCTOR);
        conductor.setRol("CONDUCTOR");
        
        when(sessionMock.getAttribute("usuario")).thenReturn(conductor);
        
        // USANDO eq() y any() CONSISTENTEMENTE
        doThrow(new ViajeNoEncontradoException("No se encontró el viaje con ID " + ID_VIAJE))
            .when(servicioHistorialReservaMock).obtenerHistorialPorViaje(eq(ID_VIAJE), any(Usuario.class));

        // when
        ModelAndView mav = historialReservaController.verHistorialPorViaje(ID_VIAJE, sessionMock);

        // then
        assertThat(mav.getViewName(), is("error"));
        assertThat(mav.getModel().get("error").toString(), containsString("No se encontró el viaje"));
        // USANDO eq() y any() CONSISTENTEMENTE
        verify(servicioHistorialReservaMock, times(1)).obtenerHistorialPorViaje(eq(ID_VIAJE), any(Usuario.class));
    }


    @Test
public void siElUsuarioLogueadoNoEsConductorDeberiaRedirigirALogin() {
    // given
    Usuario viajero = new Conductor(); // Usamos Conductor para la instanciación, pero cambiamos el rol
    viajero.setId(10L);
    viajero.setRol("VIAJERO"); // Rol que NO es CONDUCTOR
    
    when(sessionMock.getAttribute("usuario")).thenReturn(viajero);

    // when
    ModelAndView mav = historialReservaController.verHistorialPorViaje(ID_VIAJE, sessionMock);

    // then
    assertThat(mav.getViewName(), is("redirect:/login"));
    verifyNoInteractions(servicioHistorialReservaMock); // El servicio no debe llamarse
}

@Test
public void siElServicioLanzaUnaExcepcionGenericaDeberiaDevolverErrorInesperado() throws Exception {
    // given
    Conductor conductor = new Conductor();
    conductor.setId(ID_CONDUCTOR);
    conductor.setRol("CONDUCTOR");
    
    when(sessionMock.getAttribute("usuario")).thenReturn(conductor);
    
    // El servicio lanza cualquier RuntimeException (genérica)
    doThrow(new RuntimeException("Error de base de datos desconocido"))
        .when(servicioHistorialReservaMock).obtenerHistorialPorViaje(eq(ID_VIAJE), any(Usuario.class));

    // when
    ModelAndView mav = historialReservaController.verHistorialPorViaje(ID_VIAJE, sessionMock);

    // then
    assertThat(mav.getViewName(), is("error"));
    assertThat(mav.getModel().get("error").toString(), containsString("Ocurrió un error inesperado"));
    verify(servicioHistorialReservaMock, times(1)).obtenerHistorialPorViaje(eq(ID_VIAJE), any(Usuario.class));
}

@Test
public void siElViajeExistePeroNoTieneHistorialDeberiaMostrarUnaListaVacia() throws Exception {
    // given
    Conductor conductor = new Conductor();
    conductor.setId(ID_CONDUCTOR);
    conductor.setRol("CONDUCTOR");
    
    // El servicio retorna una lista vacía
    List<HistorialReservaDTO> historialVacio = List.of(); 

    when(sessionMock.getAttribute("usuario")).thenReturn(conductor);
    when(servicioHistorialReservaMock.obtenerHistorialPorViaje(eq(ID_VIAJE), any(Usuario.class)))
        .thenReturn(historialVacio);

    // when
    ModelAndView mav = historialReservaController.verHistorialPorViaje(ID_VIAJE, sessionMock);

    // then
    assertThat(mav.getViewName(), is("historial-reservas"));
    // Verifica que el modelo contenga la lista vacía
    assertThat((List<HistorialReservaDTO>) mav.getModel().get("historialReservas"), is(empty())); 
    assertThat(mav.getModel().get("mensajeExito").toString(), containsString("Historial cargado correctamente"));
    verify(servicioHistorialReservaMock, times(1)).obtenerHistorialPorViaje(eq(ID_VIAJE), any(Usuario.class));
}
}