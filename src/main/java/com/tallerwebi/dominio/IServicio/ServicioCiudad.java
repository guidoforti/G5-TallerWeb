package com.tallerwebi.dominio.IServicio;


import java.util.List;

import com.tallerwebi.dominio.Entity.Ciudad;
import com.tallerwebi.dominio.excepcion.NotFoundException;

public interface ServicioCiudad {
    List<Ciudad> listarTodas();
    Ciudad buscarPorId(Long id) throws NotFoundException;
    Ciudad guardarCiudad(Ciudad ciudad);
    void eliminarCiudad(Long id) throws NotFoundException;
    Ciudad actualizarCiudad(Ciudad ciudad) throws NotFoundException;
}