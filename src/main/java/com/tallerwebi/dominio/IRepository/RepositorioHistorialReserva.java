package com.tallerwebi.dominio.IRepository;

import java.util.List;

import com.tallerwebi.dominio.Entity.HistorialReserva;

public interface RepositorioHistorialReserva {
    
    List<HistorialReserva> findByViajeId(Long idViaje);
    void save(HistorialReserva historial);
}
