package com.tallerwebi.dominio.IRepository;

import com.tallerwebi.dominio.Entity.Vehiculo;

import java.util.List;
import java.util.Optional;

public interface RepositorioVehiculo  {

    Vehiculo findById(Long id);
    List<Vehiculo> obtenerVehiculosParaConductor(Long conductorId);
    Vehiculo encontrarVehiculoConPatente(String patente);
    Vehiculo guardarVehiculo(Vehiculo vehiculo);
}
