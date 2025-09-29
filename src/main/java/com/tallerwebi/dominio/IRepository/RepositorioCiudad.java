package com.tallerwebi.dominio.IRepository;

import java.util.List;
import com.tallerwebi.dominio.Entity.Ciudad;
import com.tallerwebi.dominio.excepcion.NotFoundException;

public interface RepositorioCiudad {
    List<Ciudad> findAll();
    Ciudad buscarPorId(Long id);
    Ciudad guardarCiudad(Ciudad ciudad);
    void eliminarCiudad(Long id);
    Ciudad actualizarCiudad(Ciudad ciudad);
}
