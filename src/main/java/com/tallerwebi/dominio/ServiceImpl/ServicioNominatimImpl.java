package com.tallerwebi.dominio.ServiceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tallerwebi.dominio.IRepository.ClienteNominatim;
import com.tallerwebi.dominio.IServicio.ServicioNominatim;
import com.tallerwebi.dominio.excepcion.NominatimResponseException;
import com.tallerwebi.presentacion.DTO.NominatimResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ServicioNominatimImpl implements ServicioNominatim {


    private ClienteNominatim clienteNominatim;


    public  ServicioNominatimImpl  (ClienteNominatim clienteNominatim) {
        this.clienteNominatim = clienteNominatim;
    }

    @Override
    public NominatimResponse buscarCiudadPorInputCompleto(String nombreCompleto) throws JsonProcessingException, NominatimResponseException {

        Optional<NominatimResponse> nominatimResponse = clienteNominatim.buscarPorNombre(nombreCompleto);
        if (nominatimResponse.isEmpty()) {
            throw  new NominatimResponseException("No se encontro una ciudad con el nombre : " + nombreCompleto);
        }

        return nominatimResponse.get();
    }

    @Override
    public List<String> devolverNombresDeCiudadesPorInputIncompleto(String nombreIncompleto) throws JsonProcessingException  {
        // no hago manejo de excepciones si las listas estan vacias ya que este metodo es para el rellenado automatico de las opciones
        List<String> opcionesADevolver = clienteNominatim.buscarCiudadesPorNombreAcordato(nombreIncompleto)
                .stream()
                .filter(nr -> "city".equalsIgnoreCase(nr.getAddressType()))
                .map(NominatimResponse::getName)
                .collect(Collectors.toList());

        return  opcionesADevolver;
    }
}
