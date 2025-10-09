package com.tallerwebi.infraestructura;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallerwebi.dominio.IRepository.ClienteNominatim;
import com.tallerwebi.presentacion.DTO.NominatimResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Repository
public class ClienteNominatimImpl implements ClienteNominatim {

    private static final String BASE_URL = "https://nominatim.openstreetmap.org/search";

    RestTemplate restTemplate;
    ObjectMapper objectMapper;

    public  ClienteNominatimImpl () {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }


    @Override
    public NominatimResponse buscarPorNombre(String ciudad) throws JsonProcessingException {
        //armo los parametros de la consulta para la url base
        String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                .queryParam("addressdetails" , 0)
                .queryParam("q" , ciudad)
                .queryParam("format", "json")
                .queryParam("limit", 1)
                .build()
                .toUriString();

        //agrego los headers necesarios
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Agent", "guidoforti96@gmail.com");
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        //hago la consulta
        ResponseEntity<String> httpResponse = restTemplate.exchange(url , HttpMethod.GET , httpEntity, String.class);

        //parseo el json a una  clase java
        NominatimResponse nominatimResponse = objectMapper.readValue(httpResponse.getBody() , NominatimResponse.class);
        return  nominatimResponse;
    }
}

