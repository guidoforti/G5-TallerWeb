package com.tallerwebi.dominio.IServicio;

import com.tallerwebi.dominio.excepcion.NotFoundException;
import com.tallerwebi.dominio.excepcion.PatenteDuplicadaException;
import com.tallerwebi.presentacion.DTO.InputsDTO.VehiculoInputDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.VehiculoOutputDTO;

import java.util.List;
import java.util.Optional;

public interface ServicioVehiculo {


    VehiculoOutputDTO getById(Long Id);
    List<VehiculoOutputDTO> obtenerVehiculosParaConductor(Long conductorId);
    VehiculoOutputDTO obtenerVehiculoConPatente(String patente) throws NotFoundException;
    VehiculoOutputDTO guardarVehiculo(VehiculoInputDTO vehiculoInputDTO, Long idConductor) throws PatenteDuplicadaException, NotFoundException;
}
