package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Entity.Ciudad;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallerwebi.dominio.IServicio.ServicioCiudad;
import com.tallerwebi.presentacion.Controller.ControladorCiudad;
import com.tallerwebi.presentacion.DTO.CiudadDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ControladorCiudadTest {

    private MockMvc mockMvc;

    @Mock
    private ServicioCiudad servicioCiudadMock;

    @InjectMocks
    private ControladorCiudad ciudadControlador;

    private ObjectMapper objectMapper;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(ciudadControlador).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void listarCiudades_deberiaDevolverListaDTO() throws Exception {
        List<Ciudad> ciudades = Arrays.asList(
                new Ciudad(1L, "Buenos Aires", -34.6f, -58.4f),
                new Ciudad(2L, "Cordoba", -31.4f, -64.2f)
        );
        when(servicioCiudadMock.listarTodas()).thenReturn(ciudades);

        mockMvc.perform(get("/ciudades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(ciudades.size()))
                .andExpect(jsonPath("$[0].nombre").value("Buenos Aires"));

        verify(servicioCiudadMock, times(1)).listarTodas();
    }

    @Test
    public void crearOrigen_deberiaGuardarCiudadYDevolverDTO() throws Exception {
        CiudadDTO ciudadDTO = new CiudadDTO("Rosario", -32.9f, -60.7f);
        Ciudad ciudadGuardada = ciudadDTO.toEntity();
        ciudadGuardada.setId(1L);

        when(servicioCiudadMock.guardarCiudad(any(Ciudad.class))).thenReturn(ciudadGuardada);

        mockMvc.perform(post("/ciudades/origen")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ciudadDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Rosario"))
                .andExpect(jsonPath("$.latitud").value(-32.9))
                .andExpect(jsonPath("$.longitud").value(-60.7));

        verify(servicioCiudadMock, times(1)).guardarCiudad(any(Ciudad.class));
    }

    @Test
    public void crearDestino_deberiaGuardarCiudadYDevolverDTO() throws Exception {
        CiudadDTO ciudadDTO = new CiudadDTO("Mendoza", -32.9f, -68.8f);
        Ciudad ciudadGuardada = ciudadDTO.toEntity();
        ciudadGuardada.setId(2L);

        when(servicioCiudadMock.guardarCiudad(any(Ciudad.class))).thenReturn(ciudadGuardada);

        mockMvc.perform(post("/ciudades/destino")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ciudadDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Mendoza"))
                .andExpect(jsonPath("$.latitud").value(-32.9))
                .andExpect(jsonPath("$.longitud").value(-68.8));

        verify(servicioCiudadMock, times(1)).guardarCiudad(any(Ciudad.class));
    }

    @Test
    public void crearOrigenDestino_deberiaGuardarDosCiudadesYDevolver2DTO() throws Exception {
        CiudadDTO origenDTO = new CiudadDTO("La Plata", -34.9f, -57.9f);
        CiudadDTO destinoDTO = new CiudadDTO("San Juan", -31.5f, -68.5f);

        Ciudad origenGuardada = origenDTO.toEntity();
        origenGuardada.setId(1L);
        Ciudad destinoGuardada = destinoDTO.toEntity();
        destinoGuardada.setId(2L);

        when(servicioCiudadMock.guardarCiudad(any(Ciudad.class)))
                .thenReturn(origenGuardada)
                .thenReturn(destinoGuardada);

        List<CiudadDTO> listaDTO = Arrays.asList(origenDTO, destinoDTO);

        mockMvc.perform(post("/ciudades/origen-destino")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(listaDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("La Plata"))
                .andExpect(jsonPath("$[1].nombre").value("San Juan"));

        verify(servicioCiudadMock, times(2)).guardarCiudad(any(Ciudad.class));
    }

}
