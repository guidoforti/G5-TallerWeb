package com.tallerwebi.dominio.ServiceImpl;


import com.tallerwebi.dominio.Entity.Vehiculo;
import com.tallerwebi.dominio.IRepository.RepositorioConductor;
import com.tallerwebi.dominio.IRepository.RepositorioVehiculo;
import com.tallerwebi.dominio.IServicio.ServicioVehiculo;
import com.tallerwebi.dominio.excepcion.NotFoundException;
import com.tallerwebi.dominio.excepcion.PatenteDuplicadaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicioVehiculoImpl implements ServicioVehiculo {

    RepositorioVehiculo repositorioVehiculo;

    RepositorioConductor repositorioConductor;

    @Autowired
    public ServicioVehiculoImpl(RepositorioVehiculo repositorioVehiculo, RepositorioConductor repositorioConductor) {
        this.repositorioVehiculo = repositorioVehiculo;

        this.repositorioConductor = repositorioConductor;
    }

    @Override
    public Vehiculo getById(Long id) throws NotFoundException {


        Vehiculo vehiculo = repositorioVehiculo.findById(id);
        if (vehiculo == null) {
            throw new NotFoundException("No se encontró un vehículo con el ID: " + id);
        }

        return  vehiculo;
    }

    @Override
    public List<Vehiculo> obtenerVehiculosParaConductor(Long conductorId) {
        if (conductorId == null) {
            throw new IllegalArgumentException("El ID del conductor no puede ser nulo");
        }

        List<Vehiculo> vehiculos = repositorioVehiculo.obtenerVehiculosParaConductor(conductorId);

        return  vehiculos;
    }
    @Override
    public Vehiculo obtenerVehiculoConPatente(String patente) throws NotFoundException {
        Vehiculo vehiculo = repositorioVehiculo.encontrarVehiculoConPatente(patente);
        if (vehiculo == null) {
            throw new NotFoundException("no se encontro un vehiculo con esa patente");
        }

        return vehiculo;
    }

    @Override
    public Vehiculo guardarVehiculo(Vehiculo vehiculo) throws PatenteDuplicadaException {


        if (repositorioVehiculo.encontrarVehiculoConPatente(vehiculo.getPatente()) != null) {
            throw new PatenteDuplicadaException("La patente cargada ya existe");
        }


        Vehiculo vehiculoSaved = repositorioVehiculo.guardarVehiculo(vehiculo);

        return vehiculoSaved;

    }
}
