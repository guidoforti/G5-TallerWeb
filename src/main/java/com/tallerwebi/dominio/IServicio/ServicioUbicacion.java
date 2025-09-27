package com.tallerwebi.dominio.IServicio;


import java.util.List;

import com.tallerwebi.dominio.excepcion.NotFoundException;
import com.tallerwebi.presentacion.DTO.UbicacionDTO;

public interface ServicioUbicacion {
    List<UbicacionDTO> listarTodas();
    UbicacionDTO obtenerUbicacion(Long id) throws NotFoundException;
}