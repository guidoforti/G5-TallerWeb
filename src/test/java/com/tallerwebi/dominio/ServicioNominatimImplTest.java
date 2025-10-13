package com.tallerwebi.dominio;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tallerwebi.dominio.IRepository.ClienteNominatim;
import com.tallerwebi.dominio.IServicio.ServicioNominatim;
import com.tallerwebi.dominio.ServiceImpl.ServicioNominatimImpl;
import com.tallerwebi.dominio.excepcion.NominatimResponseException;
import com.tallerwebi.infraestructura.ClienteNominatimImpl;
import com.tallerwebi.presentacion.DTO.NominatimResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class ServicioNominatimImplTest {
    private ClienteNominatim clienteNominatimMockeado;
    private ServicioNominatim servicioNominatim;
    private  NominatimResponse nominatimResponseObject;
    private  NominatimResponse nominatimResponseObjectTwo;
    private  NominatimResponse nominatimResponseObjectThree;
    @BeforeEach
    void setUp () {
        this.clienteNominatimMockeado = Mockito.mock(ClienteNominatimImpl.class);
        this.servicioNominatim = new ServicioNominatimImpl(clienteNominatimMockeado);
         nominatimResponseObject = new NominatimResponse("Buenos Aires" , "-34.6095579" , "-58.3887904" , 409597036L  , "city");
        nominatimResponseObjectTwo = new NominatimResponse("Buenavista", "-12.345", "-67.890", 123456L, "city");
        nominatimResponseObjectThree = new NominatimResponse("Buenos Aires", "-35.000", "-59.000", 789012L, "administrative"); // Este no debería incluirse por el filtro

    }


    @Test
    public void cuandoBuscarCiudadPorInputCompletoDevuelveNominatimResponseOk () throws JsonProcessingException, NominatimResponseException {
        //Arrange
        String nombreABuscar = "Buenos Aires";
        Optional<NominatimResponse> response = Optional.of(nominatimResponseObject);
        when(clienteNominatimMockeado.buscarPorNombre(nombreABuscar)).thenReturn(response);

        //Act
        NominatimResponse serviceResponse = servicioNominatim.buscarCiudadPorInputCompleto(nombreABuscar);

        //Assert
        assertThat(serviceResponse.getName() , is(nombreABuscar));
        assertThat(serviceResponse.getLat() , is(nominatimResponseObject.getLat()));
        assertThat(serviceResponse.getLon() , is(nominatimResponseObject.getLon()));
        assertThat(serviceResponse.getAddressType() , is("city"));
        assertThat(serviceResponse.getNominatimPlaceId() , is(409597036L));
    }

    @Test
    public void cuandoBuscarCiudadPorInputCompletoYNoEncuentraNadaDevuelveNominatimResponseException () throws JsonProcessingException, NominatimResponseException {
        //Arrange
        String nombreABuscar = "Buenos Aires";
        Optional<NominatimResponse> response = Optional.empty();
        when(clienteNominatimMockeado.buscarPorNombre(nombreABuscar)).thenReturn(response);

        //Act - Assert
        assertThrows(NominatimResponseException.class , () -> servicioNominatim.buscarCiudadPorInputCompleto(nombreABuscar));

    }
    @Test
    public void cuandoDevolverNombresPorInputIncompletoEncuentraCiudadesDevuelveListaConNombres() throws JsonProcessingException {
        //Arrange
        String prefijoABuscar = "buen";
        List<NominatimResponse> response = List.of(nominatimResponseObject, nominatimResponseObjectTwo);
        when(clienteNominatimMockeado.buscarCiudadesPorNombreAcordato(prefijoABuscar)).thenReturn(response);

        //Act
        List<String> nombresCiudades = servicioNominatim.devolverNombresDeCiudadesPorInputIncompleto(prefijoABuscar);

        //Assert
        assertThat(nombresCiudades.size(), is(2));
        assertThat(nombresCiudades.get(0), is("Buenos Aires"));
        assertThat(nombresCiudades.get(1), is("Buenavista"));
    }

    @Test
    public void cuandoDevolverNombresPorInputIncompletoNoEncuentraCiudadesDevuelveListaVacia() throws JsonProcessingException {
        //Arrange
        String prefijoABuscar = "xyz";
        List<NominatimResponse> response = List.of();
        when(clienteNominatimMockeado.buscarCiudadesPorNombreAcordato(prefijoABuscar)).thenReturn(response);

        //Act
        List<String> nombresCiudades = servicioNominatim.devolverNombresDeCiudadesPorInputIncompleto(prefijoABuscar);

        //Assert
        assertThat(nombresCiudades.size(), is(0));
    }

    @Test
    public void cuandoDevolverNombresPorInputIncompletoFiltraSoloCiudades() throws JsonProcessingException {
        //Arrange
        String prefijoABuscar = "buen";
        List<NominatimResponse> response = List.of(nominatimResponseObject, nominatimResponseObjectTwo, nominatimResponseObjectThree);
        when(clienteNominatimMockeado.buscarCiudadesPorNombreAcordato(prefijoABuscar)).thenReturn(response);

        //Act
        List<String> nombresCiudades = servicioNominatim.devolverNombresDeCiudadesPorInputIncompleto(prefijoABuscar);

        //Assert
        assertThat(nombresCiudades.size(), is(2)); // Solo las dos ciudades con addressType "city"
        assertThat(nombresCiudades.get(0), is("Buenos Aires"));
        assertThat(nombresCiudades.get(1), is("Buenavista"));
    }

}
