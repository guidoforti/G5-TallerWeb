package com.tallerwebi.dominio.IServicio;

import com.mysql.cj.log.Log;
import com.tallerwebi.dominio.Entity.Vehiculo;
import com.tallerwebi.presentacion.DTO.InputsDTO.VehiculoInputDTO;
import com.tallerwebi.presentacion.DTO.VehiculoDTO;

import java.util.List;
import java.util.Optional;

public interface ServicioVehiculo {


    VehiculoDTO getById(Long Id);
    List<VehiculoDTO> obtenerVehiculosParaConductor(Long conductorId);
    Optional<VehiculoDTO> obtenerVehiculoConPatente(String patente);
    VehiculoDTO guardarVehiculo(VehiculoInputDTO vehiculoInputDTO);
}
