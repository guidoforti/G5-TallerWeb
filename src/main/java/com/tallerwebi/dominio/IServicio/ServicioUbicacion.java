package com.tallerwebi.dominio.IServicio;


import java.util.List;

import org.springframework.stereotype.Service;
import com.tallerwebi.dominio.Entity.Ubicacion;

public interface ServicioUbicacion {
    List<Ubicacion> listarTodas();
}