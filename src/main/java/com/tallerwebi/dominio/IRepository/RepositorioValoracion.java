package com.tallerwebi.dominio.IRepository;

import java.util.List;

import com.tallerwebi.dominio.Entity.Valoracion;

public interface RepositorioValoracion {
    void save(Valoracion valoracion);
    List<Valoracion> findByReceptorId(Long receptorId);
    boolean yaExisteValoracionParaViaje(Long emisorId, Long receptorId, Long viajeId);
}
