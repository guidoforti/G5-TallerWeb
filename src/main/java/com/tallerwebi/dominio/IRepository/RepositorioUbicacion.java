package com.tallerwebi.dominio.IRepository;

import java.util.List;
import java.util.Optional;

import com.tallerwebi.dominio.Entity.Ubicacion;

public interface RepositorioUbicacion {
    List<Ubicacion> findAll();
    Optional<Ubicacion> buscarPorId(Long id);
}
