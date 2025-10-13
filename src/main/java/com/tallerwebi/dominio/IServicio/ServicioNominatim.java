package com.tallerwebi.dominio.IServicio;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tallerwebi.dominio.excepcion.NominatimResponseException;
import com.tallerwebi.presentacion.DTO.NominatimResponse;

import java.util.List;

public interface ServicioNominatim {

    NominatimResponse buscarCiudadPorInputCompleto(String nombreCompleto) throws JsonProcessingException, NominatimResponseException;
    List<String> devolverNombresDeCiudadesPorInputIncompleto (String nombreIncompleto) throws JsonProcessingException;
}
