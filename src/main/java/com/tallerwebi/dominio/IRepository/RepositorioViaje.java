package com.tallerwebi.dominio.IRepository;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Ubicacion;
import com.tallerwebi.dominio.Entity.Viaje;

import java.util.List;
import java.util.Optional;

public interface RepositorioViaje {


    Optional<Viaje> buscarPorId(Long id);
    boolean guardarViaje(Viaje viaje);
    boolean modificarViaje(Viaje viaje);
    boolean borrarViaje(Long id);

    Optional<Viaje> encontrarPorOrigenDestinoYConductor(Ubicacion origen, Ubicacion destino , Conductor conductor);
}

