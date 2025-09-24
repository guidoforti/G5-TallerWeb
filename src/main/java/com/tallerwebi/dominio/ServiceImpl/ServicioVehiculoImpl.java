package com.tallerwebi.dominio.ServiceImpl;


import com.tallerwebi.config.ManualModelMapper;
import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Vehiculo;
import com.tallerwebi.dominio.IRepository.RepositorioConductor;
import com.tallerwebi.dominio.IRepository.RepositorioVehiculo;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import com.tallerwebi.dominio.IServicio.ServicioVehiculo;
import com.tallerwebi.dominio.excepcion.NotFoundException;
import com.tallerwebi.dominio.excepcion.PatenteDuplicadaException;
import com.tallerwebi.presentacion.DTO.ConductorDTO;
import com.tallerwebi.presentacion.DTO.InputsDTO.VehiculoInputDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.VehiculoOutputDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServicioVehiculoImpl implements ServicioVehiculo {

    RepositorioVehiculo repositorioVehiculo;
    ManualModelMapper manualModelMapper;
    RepositorioConductor repositorioConductor;

    @Autowired
    public ServicioVehiculoImpl(RepositorioVehiculo repositorioVehiculo, ManualModelMapper manualModelMapper, RepositorioConductor repositorioConductor) {
        this.repositorioVehiculo = repositorioVehiculo;
        this.manualModelMapper = manualModelMapper;
        this.repositorioConductor = repositorioConductor;
    }

    @Override
    public VehiculoOutputDTO getById(Long id) throws NotFoundException {


        Vehiculo vehiculo = repositorioVehiculo.findById(id);
        if (vehiculo == null) {
            throw new NotFoundException("No se encontró un vehículo con el ID: " + id);
        }

        return manualModelMapper.toVehiculoOutputDTO(vehiculo);
    }

    @Override
    public List<VehiculoOutputDTO> obtenerVehiculosParaConductor(Long conductorId) {
        if (conductorId == null) {
            throw new IllegalArgumentException("El ID del conductor no puede ser nulo");
        }

        List<Vehiculo> vehiculos = repositorioVehiculo.obtenerVehiculosParaConductor(conductorId);

        return vehiculos.stream()
                .map(manualModelMapper::toVehiculoOutputDTO)
                .collect(java.util.stream.Collectors.toList());
    }
    @Override
    public VehiculoOutputDTO obtenerVehiculoConPatente(String patente) throws NotFoundException {
        Vehiculo vehiculo = repositorioVehiculo.encontrarVehiculoConPatente(patente);
        if (vehiculo == null) {
            throw new NotFoundException("no se encontro un vehiculo con esa patente");
        }
        VehiculoOutputDTO vehiculoDTO = manualModelMapper.toVehiculoOutputDTO(vehiculo);

        return vehiculoDTO;
    }

    @Override
    public VehiculoOutputDTO guardarVehiculo(VehiculoInputDTO vehiculoInputDTO, Long idConductor) throws PatenteDuplicadaException {


        if (repositorioVehiculo.encontrarVehiculoConPatente(vehiculoInputDTO.getPatente()) != null) {
            throw new PatenteDuplicadaException("La patente cargada ya existe");
        }

        Optional<Conductor> conductor = repositorioConductor.buscarPorId(idConductor);

        Vehiculo vehiculoToSave = manualModelMapper.toVehiculo(vehiculoInputDTO, conductor.get());

        Vehiculo vehiculo = repositorioVehiculo.guardarVehiculo(vehiculoToSave);

        return manualModelMapper.toVehiculoOutputDTO(vehiculo);

    }
}
