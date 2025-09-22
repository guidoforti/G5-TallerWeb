package com.tallerwebi.infraestructura;

import java.util.ArrayList;
import java.util.List;
import com.tallerwebi.dominio.Entity.Ubicacion;

import com.tallerwebi.dominio.IRepository.RepositorioUbicacion;

public class RepositorioUbicacionImpl implements RepositorioUbicacion {
    private List<Ubicacion> ubicaciones = new ArrayList<>();

    public RepositorioUbicacionImpl() {
        ubicaciones.add(new Ubicacion(1L, -34.653, -58.620, "Castelar, Buenos Aires"));
        ubicaciones.add(new Ubicacion(2L, -34.669, -58.562, "Morón, Buenos Aires"));
        ubicaciones.add(new Ubicacion(3L, -34.608, -58.373, "CABA, Obelisco"));
    }

    @Override
    public List<Ubicacion> findAll() {
        return ubicaciones;
    }
}
