package com.tallerwebi.dominio.IRepository;

import com.tallerwebi.dominio.Entity.Conductor;

import java.util.Optional;

public interface RepositorioConductor {
    Optional<Conductor> buscarPorEmailYContrasenia(String email, String contrasenia) throws Exception;
    Optional<Conductor> buscarPorEmail(String email);
    Optional<Conductor> buscarPorId(Long id);
    void guardar(Conductor conductor);
}
