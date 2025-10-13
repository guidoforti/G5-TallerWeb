package com.tallerwebi.dominio.IRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tallerwebi.presentacion.DTO.NominatimResponse;

import java.util.List;
import java.util.Optional;

public interface ClienteNominatim {
    Optional<NominatimResponse> buscarPorNombre(String nombreCompleto) throws JsonProcessingException;
    List<NominatimResponse> buscarCiudadesPorNombreAcordato (String nombreAcortado) throws JsonProcessingException;
}
