package com.tallerwebi.dominio.IRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tallerwebi.presentacion.DTO.NominatimResponse;

public interface ClienteNominatim {
    NominatimResponse buscarPorNombre(String ciudad) throws JsonProcessingException;
}
