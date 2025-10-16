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

import javax.transaction.Transactional;

@Service
@Transactional
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
        return repositorioVehiculo.findById(id)
        .orElseThrow(() -> new NotFoundException("No se encontro un vehiculo"));
    }

    @Override
    public List<Vehiculo> obtenerVehiculosParaConductor(Long conductorId) {
        if (conductorId == null) {
            throw new IllegalArgumentException("El ID del conductor no puede ser nulo");
        }
        return repositorioVehiculo.obtenerVehiculosParaConductor(conductorId);
    }

    @Override
    public Vehiculo obtenerVehiculoConPatente(String patente) throws NotFoundException {
        return repositorioVehiculo.encontrarVehiculoConPatente(patente)
        .orElseThrow(() -> new NotFoundException("No se encontro un vehiculo con esta patente"));
    }

    @Override
    public Vehiculo guardarVehiculo(Vehiculo vehiculo) throws PatenteDuplicadaException {
        if(repositorioVehiculo.encontrarVehiculoConPatente(vehiculo.getPatente()).isPresent()){
            throw new PatenteDuplicadaException("La patente cargada ya existe");
        }
        return repositorioVehiculo.guardarVehiculo(vehiculo);
    }
}
