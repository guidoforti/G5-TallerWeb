package com.tallerwebi.infraestructura;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallerwebi.dominio.IRepository.ClienteNominatim;
import com.tallerwebi.presentacion.DTO.NominatimResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public class ClienteNominatimImpl implements ClienteNominatim {

    private static final String BASE_URL = "https://nominatim.openstreetmap.org/search";

    RestTemplate restTemplate;
    ObjectMapper objectMapper;

    public ClienteNominatimImpl( RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }


    @Override
    public Optional<NominatimResponse> buscarPorNombre(String nombreCompleto) throws JsonProcessingException {
        //armo los parametros de la consulta para la url base
        String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                .queryParam("countrycodes", "AR")
                .queryParam("addressdetails", 0)
                .queryParam("featureType", "city")
                .queryParam("q", nombreCompleto)
                .queryParam("format", "json")
                .queryParam("limit", 1)
                .build()
                .toUriString();

        //agrego los headers necesarios
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Agent", "guidoforti96@gmail.com");
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        //hago la consulta
        ResponseEntity<String> httpResponse = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
        if (!httpResponse.getStatusCode().is2xxSuccessful()) {
            return Optional.empty();
        }
        //la respuesta devuelve un array de objetos json ( mas alla de que solo  le hayamos puesto el limite de uno
        NominatimResponse[] nominatimResponse = objectMapper.readValue(httpResponse.getBody(), NominatimResponse[].class);
        if (nominatimResponse.length == 0) {
            return Optional.empty();
        }

        return Optional.of(nominatimResponse[0]);
    }

    @Override
    public List<NominatimResponse> buscarCiudadesPorNombreAcordato(String nombreAcortado) throws JsonProcessingException {

        //Armo la url con sus parametros

        String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                .queryParam("countrycodes", "AR")
                .queryParam("addressdetails", 0)
                .queryParam("featureType", "city")
                .queryParam("q", nombreAcortado)
                .queryParam("format", "json")
                .queryParam("limit", 15)
                .build()
                .toUriString();

        //Agrego los headers necesarios
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Agent", "guidoforti96@gmail.com");
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        //hago la consulta

        try {
            ResponseEntity<String> httpResponse = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
            if (!httpResponse.getStatusCode().is2xxSuccessful()) {
                return new ArrayList<>();
            }
            // convierto cada objeto dentro del array a un nominatimREsponse
            NominatimResponse[] objectsResponse = objectMapper.readValue(httpResponse.getBody(), NominatimResponse[].class);
            //devuelvo ese array de objetos NominatimResponse en forma de lista NominatimResponse
            return Arrays.asList(objectsResponse);
        } catch (JsonProcessingException  e) {
            //devuelvo lista vacia si hubo errror de parseo
            return new ArrayList<>();
        }

    }


}

