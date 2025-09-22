package com.tallerwebi.dominio.IRepository;

import java.util.List;
import com.tallerwebi.dominio.Entity.Ubicacion;

public interface RepositorioUbicacion {
    List<Ubicacion> findAll();
}
