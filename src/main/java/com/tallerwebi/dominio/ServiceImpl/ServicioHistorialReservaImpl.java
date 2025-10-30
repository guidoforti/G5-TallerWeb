package com.tallerwebi.dominio.ServiceImpl;
import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.IRepository.RepositorioHistorialReserva;
import com.tallerwebi.dominio.IRepository.ViajeRepository;
import com.tallerwebi.dominio.IServicio.ServicioHistorialReserva;
import com.tallerwebi.dominio.excepcion.UsuarioNoAutorizadoException;
import com.tallerwebi.dominio.excepcion.ViajeNoEncontradoException;
import com.tallerwebi.presentacion.DTO.OutputsDTO.HistorialReservaDTO;

@Service("servicioHistorialReserva")
@Transactional
public class ServicioHistorialReservaImpl implements ServicioHistorialReserva{

    private final RepositorioHistorialReserva repositorioHistorialReserva;
    private final ViajeRepository repositorioViaje;
    
    @Autowired
    public ServicioHistorialReservaImpl(RepositorioHistorialReserva repositorioHistorialReserva,
                                        ViajeRepository repositorioViaje) {
        this.repositorioHistorialReserva = repositorioHistorialReserva;
        this.repositorioViaje = repositorioViaje;
    }


    @Override
    public List<HistorialReservaDTO> obtenerHistorialPorViaje(Long idViaje, Usuario usuarioEnSesion)
            throws ViajeNoEncontradoException, UsuarioNoAutorizadoException {
       
                Viaje viaje = repositorioViaje.findById(idViaje)
                .orElseThrow(() -> new ViajeNoEncontradoException("No se encontró el viaje con ID " + idViaje));

        if (!viaje.getConductor().getId().equals(usuarioEnSesion.getId())) {
            throw new UsuarioNoAutorizadoException("No tenés permisos para ver el historial de este viaje.");
        }

        return repositorioHistorialReserva.findByViaje(viaje)
                .stream()
                .map(HistorialReservaDTO::new)
                .collect(Collectors.toList());
    }
    
}
