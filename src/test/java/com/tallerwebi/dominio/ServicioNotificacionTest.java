package com.tallerwebi.dominio;

import com.tallerwebi.dominio.Entity.Conductor; // Importar Conductor
import com.tallerwebi.dominio.Entity.Notificacion;
import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.IRepository.RepositorioNotificacion;
import com.tallerwebi.dominio.IServicio.ServicioNotificacion;
import com.tallerwebi.dominio.ServiceImpl.ServicioNotificacionImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

public class ServicioNotificacionTest {

    private RepositorioNotificacion repositorioNotificacionMock;
    private ServicioNotificacion servicioNotificacion;
    private final Long ID_USUARIO = 5L;

    @BeforeEach
    public void setUp() {
        repositorioNotificacionMock = mock(RepositorioNotificacion.class);
        servicioNotificacion = new ServicioNotificacionImpl(repositorioNotificacionMock);
    }

    @Test
    public void obtenerYMarcarVistas_deberiaDevolverNotificacionesYMarcarComoVistas() {
        // GIVEN
        // [🟢 CORREGIDO] Usar la clase concreta Conductor en lugar de Usuario
        Notificacion n1 = new Notificacion(1L, new Conductor(), "Mensaje 1", "/url/1", LocalDateTime.now(), false);
        Notificacion n2 = new Notificacion(2L, new Conductor(), "Mensaje 2", "/url/2", LocalDateTime.now(), false);

        List<Notificacion> notificacionesPendientes = new ArrayList<>(List.of(n1, n2));

        when(repositorioNotificacionMock.findByUsuarioIdAndVistaFalse(ID_USUARIO)).thenReturn(notificacionesPendientes);

        // WHEN
        List<Notificacion> resultado = servicioNotificacion.obtenerNoVistasYMarcarComoVistas(ID_USUARIO);

        // THEN
        assertThat(resultado, is(not(empty())));
        assertThat(resultado, hasSize(2));
        assertThat(n1.getVista(), is(true));
        assertThat(n2.getVista(), is(true));

        verify(repositorioNotificacionMock, times(1)).findByUsuarioIdAndVistaFalse(ID_USUARIO);
    }

    @Test
    public void obtenerYMarcarVistas_sinNotificaciones_deberiaDevolverListaVacia() {
        // GIVEN
        when(repositorioNotificacionMock.findByUsuarioIdAndVistaFalse(ID_USUARIO)).thenReturn(List.of());

        // WHEN
        List<Notificacion> resultado = servicioNotificacion.obtenerNoVistasYMarcarComoVistas(ID_USUARIO);

        // THEN
        assertThat(resultado, is(empty()));
        verify(repositorioNotificacionMock, times(1)).findByUsuarioIdAndVistaFalse(ID_USUARIO);
    }
}