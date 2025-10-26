package com.tallerwebi.dominio.IRepository;


import com.tallerwebi.dominio.Entity.Parada;

import java.util.Optional;

public interface RepositorioParada {

    Optional<Parada> findByid(Long id);
}
