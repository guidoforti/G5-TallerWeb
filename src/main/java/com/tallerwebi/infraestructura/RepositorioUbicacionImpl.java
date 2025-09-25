package com.tallerwebi.infraestructura;


import java.util.List;

import org.springframework.stereotype.Repository;

import com.tallerwebi.dominio.Entity.Ubicacion;

import com.tallerwebi.dominio.IRepository.RepositorioUbicacion;

@Repository
public class RepositorioUbicacionImpl implements RepositorioUbicacion {
    private List<Ubicacion> ubicaciones;

    public RepositorioUbicacionImpl() {
        this.ubicaciones = Datos.obtenerUbicaciones();
    }

    @Override
    public List<Ubicacion> findAll() {
        return ubicaciones;
    }
}
