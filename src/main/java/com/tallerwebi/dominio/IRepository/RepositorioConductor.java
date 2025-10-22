package com.tallerwebi.dominio.IRepository;

import com.tallerwebi.dominio.Entity.Conductor;

import java.util.Optional;

public interface RepositorioConductor {
    Optional<Conductor> buscarPorId(Long id);
}
