package com.tallerwebi.dominio.ServiceImpl;

import java.util.List;

import com.tallerwebi.dominio.Entity.Ubicacion;
import com.tallerwebi.dominio.IRepository.RepositorioUbicacion;
import com.tallerwebi.dominio.IServicio.ServicioUbicacion;

public class ServicioUbicacionImpl implements ServicioUbicacion{

    private RepositorioUbicacion repositorioUbicacion;
    
    public ServicioUbicacionImpl(RepositorioUbicacion repositorioUbicacion) {
        this.repositorioUbicacion = repositorioUbicacion;
    }
    

    @Override
    public List<Ubicacion> listarTodas() {
        
        return repositorioUbicacion.findAll();
    }

    

}
