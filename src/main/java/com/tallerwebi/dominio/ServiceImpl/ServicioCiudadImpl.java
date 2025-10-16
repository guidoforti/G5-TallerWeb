package com.tallerwebi.dominio.ServiceImpl;

import java.util.List;

import com.tallerwebi.dominio.excepcion.NotFoundException;
import org.springframework.stereotype.Service;

import com.tallerwebi.dominio.Entity.Ciudad;
import com.tallerwebi.dominio.IRepository.RepositorioCiudad;
import com.tallerwebi.dominio.IServicio.ServicioCiudad;

import javax.transaction.Transactional;

@Service
@Transactional
public class ServicioCiudadImpl implements ServicioCiudad {

    private RepositorioCiudad repositorioCiudad;
    
    public ServicioCiudadImpl(RepositorioCiudad repositorioCiudad) {
        this.repositorioCiudad = repositorioCiudad;
    }
    

    @Override
    public List<Ciudad> listarTodas() {
        
        return repositorioCiudad.findAll();
    }

    @Override
    public Ciudad buscarPorId(Long id) throws NotFoundException {
        return repositorioCiudad.buscarPorId(id).
                orElseThrow(() -> new NotFoundException("La ciudad con ese id no existe"));
    }

    @Override
    public Ciudad guardarCiudad(Ciudad ciudad) {
        return repositorioCiudad.buscarPorCoordenadas(ciudad.getLatitud(), ciudad.getLongitud())
                .orElseGet(() -> {
                    return repositorioCiudad.guardarCiudad(ciudad);
                });
    }

    @Override
    public void eliminarCiudad(Long id) throws NotFoundException {
        repositorioCiudad.buscarPorId(id).
                orElseThrow(() -> new NotFoundException("No se encontro ciudad con ese id"));
        repositorioCiudad.eliminarCiudad(id);
    }

    @Override
    public Ciudad actualizarCiudad(Ciudad ciudad) throws NotFoundException {
        repositorioCiudad.buscarPorId(ciudad.getId()).
                orElseThrow(() -> new NotFoundException("No se encontro una ciudad con ese ID"));
        return repositorioCiudad.actualizarCiudad(ciudad);
    }


}
