package com.tallerwebi.presentacion;

import com.tallerwebi.presentacion.Controller.ControladorMapa;
import com.tallerwebi.dominio.IServicio.ServicioCiudad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

public class ControladorMapaTest {

    private ControladorMapa ControladorMapa;
    private ServicioCiudad servicioCiudadMock;

    @BeforeEach
    public void init() {
        servicioCiudadMock = mock(ServicioCiudad.class);
        ControladorMapa = new ControladorMapa(servicioCiudadMock);
    }

    @Test
    public void mostrarMapaDeberiaDevolverVistaMapa() {
        ModelAndView mav = ControladorMapa.mostrarMapa();

        assertThat(mav.getViewName(), equalTo("publicarViaje"));
        verify(servicioCiudadMock, times(1)).listarTodas();
    }
}