package com.tallerwebi.presentacion;

import com.tallerwebi.presentacion.Controller.ControladorMapa;
import com.tallerwebi.dominio.IServicio.ServicioUbicacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

public class ControladorMapaTest {

    private ControladorMapa ControladorMapa;
    private ServicioUbicacion servicioUbicacionMock;

    @BeforeEach
    public void init() {
        servicioUbicacionMock = mock(ServicioUbicacion.class);
        ControladorMapa = new ControladorMapa(servicioUbicacionMock);
    }

    @Test
    public void mostrarMapaDeberiaDevolverVistaMapa() {
        ModelAndView mav = ControladorMapa.mostrarMapa();

        assertThat(mav.getViewName(), equalTo("publicarViaje"));
        verify(servicioUbicacionMock, times(1)).listarTodas();
    }
}