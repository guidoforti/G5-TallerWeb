package com.tallerwebi.infraestructura;


import java.util.List;
import com.tallerwebi.dominio.Entity.Ubicacion;

import com.tallerwebi.dominio.IRepository.RepositorioUbicacion;

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
