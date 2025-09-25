package com.tallerwebi.dominio.IRepository;

import com.tallerwebi.dominio.Entity.Conductor;

import java.util.Optional;

public interface RepositorioConductor {
    Optional<Conductor> buscarPorEmailYContrasenia(String email, String contrasenia);
    Optional<Conductor> buscarPorEmail(String email);
    Optional<Conductor> buscarPorId(Long id);
    boolean guardar(Conductor conductor);
}
