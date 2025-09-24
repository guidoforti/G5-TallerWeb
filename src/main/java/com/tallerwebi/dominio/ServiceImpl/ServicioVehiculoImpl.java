package com.tallerwebi.dominio.ServiceImpl;


import com.tallerwebi.config.ManualModelMapper;
import com.tallerwebi.dominio.IRepository.RepositorioVehiculo;
import com.tallerwebi.dominio.IServicio.ServicioVehiculo;
import com.tallerwebi.presentacion.DTO.InputsDTO.VehiculoInputDTO;
import com.tallerwebi.presentacion.DTO.VehiculoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServicioVehiculoImpl implements ServicioVehiculo {

    RepositorioVehiculo repositorioVehiculo;
    ManualModelMapper manualModelMapper;

    @Autowired
    public ServicioVehiculoImpl(RepositorioVehiculo repositorioVehiculo, ManualModelMapper manualModelMapper) {
        this.repositorioVehiculo = repositorioVehiculo;
        this.manualModelMapper = manualModelMapper;
    }

    @Override
    public VehiculoDTO getById(Long Id) {
        return null;
    }

    @Override
    public List<VehiculoDTO> obtenerVehiculosParaConductor(Long conductorId) {
        return List.of();
    }

    @Override
    public Optional<VehiculoDTO> obtenerVehiculoConPatente(String patente) {
    return null;
    }

    @Override
    public VehiculoDTO guardarVehiculo(VehiculoInputDTO vehiculoInputDTO) {
    return null;

    }
}
