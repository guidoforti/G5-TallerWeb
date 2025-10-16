package com.tallerwebi.dominio.IRepository;

import java.util.List;
import java.util.Optional;

import com.tallerwebi.dominio.Entity.Ciudad;
import com.tallerwebi.dominio.excepcion.NotFoundException;

public interface RepositorioCiudad {
    List<Ciudad> findAll();
    Optional<Ciudad> buscarPorId(Long id);
    Optional<Ciudad> buscarPorCoordenadas(Float latitud, Float longitud);
    Ciudad guardarCiudad(Ciudad ciudad);
    void eliminarCiudad(Long id);
    Ciudad actualizarCiudad(Ciudad ciudad);
}
