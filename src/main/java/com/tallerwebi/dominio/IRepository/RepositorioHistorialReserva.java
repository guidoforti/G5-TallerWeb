package com.tallerwebi.dominio.IRepository;

import java.util.List;
import java.util.Optional;

import com.tallerwebi.dominio.Entity.HistorialReserva;
import com.tallerwebi.dominio.Entity.Viaje;

public interface RepositorioHistorialReserva {
    
    /*este metodo obtiene el historial de un viaje en especifico, ordenado por fecha*/
    List<HistorialReserva> findByViaje(Viaje viaje);
    Optional<HistorialReserva> findByViajeId(Long idViaje);
    void save(HistorialReserva historialReserva);
}
