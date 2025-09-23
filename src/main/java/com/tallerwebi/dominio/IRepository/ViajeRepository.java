package com.tallerwebi.dominio.IRepository;

import com.tallerwebi.dominio.Entity.Viaje;

public interface ViajeRepository {


    Viaje findById(Long id);
    void guardarViaje(Viaje viaje);
    void modificarViajer(Viaje viaje);
    void borrarViaje(Long id);
}

