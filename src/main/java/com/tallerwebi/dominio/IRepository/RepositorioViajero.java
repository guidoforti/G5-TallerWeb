package com.tallerwebi.dominio.IRepository;

import java.util.Optional;

import com.tallerwebi.dominio.Entity.Viajero;

public interface RepositorioViajero {

    Optional<Viajero> buscarPorId(Long id);
}
