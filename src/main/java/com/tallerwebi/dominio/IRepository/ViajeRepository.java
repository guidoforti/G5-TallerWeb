package com.tallerwebi.dominio.IRepository;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Ciudad;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Enums.EstadoDeViaje;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ViajeRepository {

    Optional<Viaje> findById(Long id);
    Viaje guardarViaje(Viaje viaje);
    void modificarViaje(Viaje viaje);
    void borrarViaje(Long id);
    List<Viaje> findByOrigenYDestinoYConductorYEstadoIn(Ciudad origen, Ciudad destino, Conductor conductor, List<EstadoDeViaje> estados);
    List<Viaje> findByOrigenYDestinoYConductor(Ciudad origen, Ciudad destino , Conductor conductor);
    List<Viaje> findByConductorId(Long idConductor);
    List<Viaje> buscarViajesPorFiltros(Ciudad origen, Ciudad destino, List<EstadoDeViaje> estados, LocalDateTime fechaDesde, Double precioMin, Double precioMax);
}
