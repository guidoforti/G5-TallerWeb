package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Entity.Vehiculo;
import com.tallerwebi.dominio.IRepository.RepositorioVehiculo;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class RepositorioVehiculoImpl implements RepositorioVehiculo {

    List<Vehiculo> baseDeDatos;


    public RepositorioVehiculoImpl() {
        this.baseDeDatos = Datos.obtenerVehiculos();
    }

    @Override
    public Vehiculo findById(Long id) {
        return null;
    }

    @Override
    public List<Vehiculo> obtenerVehiculosParaConductor(Long conductorId) {
        return List.of();

    }

    @Override
    public Vehiculo encontrarVehiculoConPatente(String patente) {
        return this.baseDeDatos.stream().filter(v-> v.getPatente().equals(patente)).findFirst().orElse(null);
    }

    @Override
    public Vehiculo guardarVehiculo(Vehiculo vehiculo) {
        Long idSiguiente = (long) (baseDeDatos.size() + 1);
        vehiculo.setId(idSiguiente);;
        this.baseDeDatos.add(vehiculo);

        return vehiculo;
    }
}
