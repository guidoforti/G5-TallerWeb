package com.tallerwebi.infraestructura;

import java.util.Optional;

import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.IRepository.RepositorioViajero;

public class RepositorioViajeroImpl implements RepositorioViajero{

    @Override
    public Optional<Viajero> buscarPorEmailYContrasenia(String email, String contrasenia) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buscarPorEmailYContrasenia'");
    }

    @Override
    public Optional<Viajero> buscarPorEmail(String email) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buscarPorEmail'");
    }

    @Override
    public Optional<Viajero> buscarPorId(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buscarPorId'");
    }

    @Override
    public boolean guardar(Viajero viajero) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'guardar'");
    }
    
}
