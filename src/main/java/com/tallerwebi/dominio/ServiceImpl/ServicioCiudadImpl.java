package com.tallerwebi.dominio.ServiceImpl;

import java.util.List;

import com.tallerwebi.dominio.excepcion.NotFoundException;
import org.springframework.stereotype.Service;

import com.tallerwebi.dominio.Entity.Ciudad;
import com.tallerwebi.dominio.IRepository.RepositorioCiudad;
import com.tallerwebi.dominio.IServicio.ServicioCiudad;

@Service
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
        Ciudad ciudad =  repositorioCiudad.buscarPorId(id);
        if (ciudad == null) {
            throw  new NotFoundException("La ciudad con ese id no existe");
        }
        return ciudad;
    }

    @Override
    public Ciudad guardarCiudad(Ciudad ciudad) {
        return null;
    }

    @Override
    public Ciudad eliminarCiudad(Long id) {
        return null;
    }

    @Override
    public Ciudad actualizarCiudad(Ciudad ciudad) {
        return null;
    }


}
