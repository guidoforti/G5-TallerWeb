package com.tallerwebi.dominio.IRepository;

import com.tallerwebi.dominio.Entity.Vehiculo;

import java.util.List;
import java.util.Optional;

public interface RepositorioVehiculo  {

    Optional<Vehiculo> findById(Long id);
    Optional<Vehiculo> obtenerVehiculosParaConductor(Long conductorId);
    Optional<Vehiculo> encontrarVehiculoConPatente(String patente);
    void guardarVehiculo(Vehiculo vehiculo);
}
