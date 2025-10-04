package com.tallerwebi.dominio.IRepository;

import java.util.Optional;

import com.tallerwebi.dominio.Entity.Viajero;

public interface RepositorioViajero {

    Optional<Viajero> buscarPorEmailYContrasenia(String email, String contrasenia);
    Optional<Viajero> buscarPorEmail(String email);
    Optional<Viajero> buscarPorId(Long id);
    boolean guardar(Viajero viajero);

}
