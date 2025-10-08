package com.tallerwebi.dominio;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Enums.EstadoDeViaje;
import com.tallerwebi.dominio.IRepository.RepositorioConductor;
import com.tallerwebi.dominio.IRepository.ViajeRepository;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import com.tallerwebi.dominio.IServicio.ServicioViaje;
import com.tallerwebi.dominio.ServiceImpl.ServicioConductorImpl;
import com.tallerwebi.dominio.ServiceImpl.ServicioViajeImpl;
import com.tallerwebi.dominio.excepcion.CredencialesInvalidas;
import com.tallerwebi.dominio.excepcion.FechaDeVencimientoDeLicenciaInvalida;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.dominio.excepcion.UsuarioNoAutorizadoException;
import com.tallerwebi.dominio.excepcion.ViajeNoCancelableException;
import com.tallerwebi.dominio.excepcion.ViajeNoEncontradoException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ServicioViajeTest {

    private ViajeRepository repositorioViajeMock;
    private ServicioViaje servicio;
    private ServicioConductor servicioConductorMock;

    @BeforeEach
    void setUp() {
        repositorioViajeMock = mock(ViajeRepository.class);
        servicioConductorMock = mock(ServicioConductor.class);
        //deje el modelMapper en null ya que no lo estamos utilizando
        servicio = new ServicioViajeImpl(repositorioViajeMock, servicioConductorMock);
    }

    @Test
    void seDebeCancelarViajeCorrectamente() throws Exception {
        
        Conductor conductor = new Conductor();
        conductor.setId(1L);
        conductor.setEmail("pepito@gmail.com");
        
        Usuario usuarioEnSesion = new Usuario();
        usuarioEnSesion.setId(1L);
        usuarioEnSesion.setRol("CONDUCTOR");

        Viaje viaje = new Viaje();
        viaje.setId(100L);
        viaje.setConductor(conductor);
        viaje.setEstado(EstadoDeViaje.DISPONIBLE);

        when(repositorioViajeMock.findById(100L)).thenReturn(viaje);

        servicio.cancelarViaje(100L, usuarioEnSesion);

        assertEquals(EstadoDeViaje.CANCELADO, viaje.getEstado());
        verify(repositorioViajeMock).modificarViaje(viaje);
}


    @Test
        void noDebeCancelarSiUnUsuarioNoTieneRol() {
        
        Usuario usuarioSinRol = new Usuario();
        usuarioSinRol.setId(2L);
        usuarioSinRol.setRol(null);

        assertThrows(UsuarioNoAutorizadoException.class,
                () -> servicio.cancelarViaje(100L, usuarioSinRol));

        verify(repositorioViajeMock, never()).modificarViaje(any());
    }

    @Test
    void noDebeCancelarSiUnViajeNoExiste() {
        Usuario usuarioConductor = new Usuario();
        usuarioConductor.setId(1L);
        usuarioConductor.setRol("CONDUCTOR");

        when(repositorioViajeMock.findById(999L)).thenReturn(null);

        assertThrows(ViajeNoEncontradoException.class,
                () -> servicio.cancelarViaje(999L, usuarioConductor));

        verify(repositorioViajeMock, never()).modificarViaje(any());
}

   @Test
    void noSeDebeCancelarSiUnViajeNoPerteneceAlConductor() {
        
        Usuario usuarioConductor = new Usuario();
        usuarioConductor.setId(1L);
        usuarioConductor.setRol("CONDUCTOR");

        // el viaje pertenece a otro conductor
        Conductor otroConductor = new Conductor();
        otroConductor.setId(99L);

        Viaje viaje = new Viaje();
        viaje.setId(100L);
        viaje.setConductor(otroConductor);
        viaje.setEstado(EstadoDeViaje.DISPONIBLE);

        when(repositorioViajeMock.findById(100L)).thenReturn(viaje);

        assertThrows(UsuarioNoAutorizadoException.class,
                () -> servicio.cancelarViaje(100L, usuarioConductor));

        verify(repositorioViajeMock, never()).modificarViaje(any());
}

@Test
    void noSeDebeCancelarSiElEstadoDelViajeEsFinalizado() {
    
        Usuario usuarioConductor = new Usuario();
        usuarioConductor.setId(1L);
        usuarioConductor.setRol("CONDUCTOR");

        Conductor conductor = new Conductor();
        conductor.setId(1L);

        Viaje viaje = new Viaje();
        viaje.setId(100L);
        viaje.setConductor(conductor);
        //el viaje se finalizo
        viaje.setEstado(EstadoDeViaje.FINALIZADO);

        when(repositorioViajeMock.findById(100L)).thenReturn(viaje);

        assertThrows(ViajeNoCancelableException.class,
                () -> servicio.cancelarViaje(100L, usuarioConductor));

        verify(repositorioViajeMock, never()).modificarViaje(any());
}

@Test
    void noSeDebeCancelarSiElEstadoDelViajeEsCancelado() {

        Usuario usuarioConductor = new Usuario();
        usuarioConductor.setId(1L);
        usuarioConductor.setRol("CONDUCTOR");

        Conductor conductor = new Conductor();
        conductor.setId(1L);

        Viaje viaje = new Viaje();
        viaje.setId(100L);
        viaje.setConductor(conductor);
        //viaje cancelado
        viaje.setEstado(EstadoDeViaje.CANCELADO);

        when(repositorioViajeMock.findById(100L)).thenReturn(viaje);

        assertThrows(ViajeNoCancelableException.class,
                () -> servicio.cancelarViaje(100L, usuarioConductor));

        verify(repositorioViajeMock, never()).modificarViaje(any());
}

    

}
