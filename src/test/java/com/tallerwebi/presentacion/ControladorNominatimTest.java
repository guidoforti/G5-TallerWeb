package com.tallerwebi.presentacion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tallerwebi.dominio.IServicio.ServicioNominatim;
import com.tallerwebi.presentacion.Controller.ControladorNominatim;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

public class ControladorNominatimTest {

    private ControladorNominatim controladorNominatim;
    private ServicioNominatim servicioNominatimMock;

    @BeforeEach
    public void init() {
        servicioNominatimMock = mock(ServicioNominatim.class);
        controladorNominatim = new ControladorNominatim(servicioNominatimMock);
    }

    @Test
    public void deberiaRetornarListaVaciaSiQueryEsNulo() throws Exception {
        // when
        ResponseEntity<List<String>> response = controladorNominatim.buscarCiudades(null);

        // then
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), notNullValue());
        assertThat(response.getBody(), empty());
        verify(servicioNominatimMock, never()).devolverNombresDeCiudadesPorInputIncompleto(anyString());
    }

    @Test
    public void deberiaRetornarListaVaciaSiQueryEsMenorADosCaracteres() throws Exception {
        // when
        ResponseEntity<List<String>> response = controladorNominatim.buscarCiudades("B");

        // then
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), notNullValue());
        assertThat(response.getBody(), empty());
        verify(servicioNominatimMock, never()).devolverNombresDeCiudadesPorInputIncompleto(anyString());
    }

    @Test
    public void deberiaRetornarListaVaciaSiQuerySoloTieneEspacios() throws Exception {
        // when
        ResponseEntity<List<String>> response = controladorNominatim.buscarCiudades("   ");

        // then
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), notNullValue());
        assertThat(response.getBody(), empty());
        verify(servicioNominatimMock, never()).devolverNombresDeCiudadesPorInputIncompleto(anyString());
    }

    @Test
    public void deberiaRetornarSugerenciasCuandoQueryEsValido() throws Exception {
        // given
        String query = "Buenos Aires";
        List<String> sugerenciasEsperadas = Arrays.asList(
                "Buenos Aires",
                "Buenos Aires, Provincia de Buenos Aires",
                "Buenos Aires, Capital Federal"
        );

        when(servicioNominatimMock.devolverNombresDeCiudadesPorInputIncompleto(query))
                .thenReturn(sugerenciasEsperadas);

        // when
        ResponseEntity<List<String>> response = controladorNominatim.buscarCiudades(query);

        // then
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), notNullValue());
        assertThat(response.getBody(), hasSize(3));
        assertThat(response.getBody(), containsInAnyOrder(
                "Buenos Aires",
                "Buenos Aires, Provincia de Buenos Aires",
                "Buenos Aires, Capital Federal"
        ));
        verify(servicioNominatimMock, times(1)).devolverNombresDeCiudadesPorInputIncompleto(query);
    }

    @Test
    public void deberiaRetornarListaVaciaCuandoServicioLanzaExcepcion() throws Exception {
        // given
        String query = "CiudadInvalida";
        when(servicioNominatimMock.devolverNombresDeCiudadesPorInputIncompleto(query))
                .thenThrow(new JsonProcessingException("Error parsing JSON") {});

        // when
        ResponseEntity<List<String>> response = controladorNominatim.buscarCiudades(query);

        // then
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), notNullValue());
        assertThat(response.getBody(), empty());
        verify(servicioNominatimMock, times(1)).devolverNombresDeCiudadesPorInputIncompleto(query);
    }

    @Test
    public void deberiaTrimearQueryAntesDeConsultar() throws Exception {
        // given
        String query = "  Córdoba  ";
        String queryTrimmed = "Córdoba";
        List<String> sugerencias = Arrays.asList("Córdoba", "Córdoba Capital");

        when(servicioNominatimMock.devolverNombresDeCiudadesPorInputIncompleto(queryTrimmed))
                .thenReturn(sugerencias);

        // when
        ResponseEntity<List<String>> response = controladorNominatim.buscarCiudades(query);

        // then
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), hasSize(2));
        verify(servicioNominatimMock, times(1)).devolverNombresDeCiudadesPorInputIncompleto(queryTrimmed);
    }

    @Test
    public void deberiaRetornarListaVaciaCuandoServicioDevuelveListaVacia() throws Exception {
        // given
        String query = "CiudadNoExistente";
        when(servicioNominatimMock.devolverNombresDeCiudadesPorInputIncompleto(query))
                .thenReturn(new ArrayList<>());

        // when
        ResponseEntity<List<String>> response = controladorNominatim.buscarCiudades(query);

        // then
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), notNullValue());
        assertThat(response.getBody(), empty());
        verify(servicioNominatimMock, times(1)).devolverNombresDeCiudadesPorInputIncompleto(query);
    }
}
