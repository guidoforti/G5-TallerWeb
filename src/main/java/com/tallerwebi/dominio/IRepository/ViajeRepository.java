package com.tallerwebi.dominio.IRepository;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Ubicacion;
import com.tallerwebi.dominio.Entity.Viaje;

import java.util.List;

public interface ViajeRepository {


    Viaje findById(Long id);
    void guardarViaje(Viaje viaje);
    void modificarViajer(Viaje viaje);
    void borrarViaje(Long id);

    List<Viaje> findByOrigenYDestinoYConductor(Ubicacion origen, Ubicacion destino , Conductor conductor);
}

