package com.tallerwebi.infraestructura;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallerwebi.presentacion.DTO.NominatimResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;


public class ClienteNoiminatimImplTest {

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;
    private ClienteNominatimImpl clienteNominatim;

    @BeforeEach
    void setUp() {
        restTemplate = Mockito.mock(RestTemplate.class);
        objectMapper = new ObjectMapper();
        clienteNominatim = new ClienteNominatimImpl(restTemplate, objectMapper);
    }

    @Test
    void queDevuelvaCiudadCuandoRespuestaEsExitosa() throws Exception {
        // arrange
        String json = "[{" +
                "\"place_id\":123," +
                "\"name\":\"Buenos Aires\"," +
                "\"lat\":\"-34.6037\"," +
                "\"lon\":\"-58.3816\"" +
                "}]";

        ResponseEntity<String> response = new ResponseEntity<>(json, HttpStatus.OK);

        // mockeo la respuesta del restTemplate
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(response);

        // act
        Optional<NominatimResponse> result = clienteNominatim.buscarPorNombre("Buenos Aires");

        // assert
        assertTrue(result.isPresent());
        assertEquals("Buenos Aires", result.get().getName());
        assertEquals("-34.6037", result.get().getLat());
        assertEquals("-58.3816", result.get().getLon());
    }

    @Test
    void queDevuelvaEmptyCuandoNoHayResultados() throws Exception {
        // arrange
        String json = "[]";
        ResponseEntity<String> response = new ResponseEntity<>(json, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(response);

        // act
        Optional<NominatimResponse> result = clienteNominatim.buscarPorNombre("CiudadInexistente");

        // assert
        assertTrue(result.isEmpty());
    }

    @Test
    void queDevuelvaListaDeCiudadesCuandoBusquedaEsPorPrefijo() throws Exception {
        // arrange
        String json = "[" +
                "{\"name\":\"Buenos Aires\", \"lat\":\"-34.6\", \"lon\":\"-58.3\"}," +
                "{\"name\":\"Buen Pasto\", \"lat\":\"-45.0\", \"lon\":\"-69.4\"}" +
                "]";
        ResponseEntity<String> response = new ResponseEntity<>(json, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(response);

        // act
        List<NominatimResponse> results = clienteNominatim.buscarCiudadesPorNombreAcordato("buen");

        // assert
        assertEquals(2, results.size());
        assertEquals("Buenos Aires", results.get(0).getName());
    }

    @Test
    void queDevuelvaListaVaciaSiHayErrorDeParseo() throws Exception {
        // arrange
        String json = "respuesta no valida";
        ResponseEntity<String> response = new ResponseEntity<>(json, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(response);

        // act
        List<NominatimResponse> results = clienteNominatim.buscarCiudadesPorNombreAcordato("buen");

        // assert
        assertTrue(results.isEmpty());
    }

    @Test
    void queDevuelvaEmptySiStatusNoEs200() throws Exception {
        // arrange
        ResponseEntity<String> response = new ResponseEntity<>("", HttpStatus.BAD_REQUEST);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(response);

        // act
        Optional<NominatimResponse> result = clienteNominatim.buscarPorNombre("Buenos Aires");

        // assert
        assertTrue(result.isEmpty());
    }
}
