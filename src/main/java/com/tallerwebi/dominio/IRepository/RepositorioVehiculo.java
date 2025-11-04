package com.tallerwebi.dominio.IRepository;

import com.tallerwebi.dominio.Entity.Vehiculo;
import com.tallerwebi.dominio.Enums.EstadoVerificacion;

import java.util.List;
import java.util.Optional;

public interface RepositorioVehiculo  {

    Optional<Vehiculo> findById(Long id);
    List<Vehiculo> obtenerVehiculosParaConductor(Long conductorId);//Una lista vacia representa ausencia de resultados, lo mismo que el optional
    Optional<Vehiculo> encontrarVehiculoConPatente(String patente);
    Vehiculo guardarVehiculo(Vehiculo vehiculo);
    List<Vehiculo> findByConductorIdAndEstadoVerificacionNot(Long conductorId, EstadoVerificacion estado);
}
