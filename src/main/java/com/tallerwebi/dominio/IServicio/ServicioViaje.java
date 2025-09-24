package com.tallerwebi.dominio.IServicio;

import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.presentacion.DTO.InputsDTO.ViajeInputDTO;

public interface ServicioViaje {

Viaje obtenerViajePorId (Long id);

void publicarViaje (ViajeInputDTO viajeInputDTO);
}
