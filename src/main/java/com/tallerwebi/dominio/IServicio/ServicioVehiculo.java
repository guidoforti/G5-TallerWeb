package com.tallerwebi.dominio.IServicio;

import com.tallerwebi.dominio.Entity.Vehiculo;
import com.tallerwebi.dominio.excepcion.NotFoundException;
import com.tallerwebi.dominio.excepcion.PatenteDuplicadaException;
import com.tallerwebi.dominio.excepcion.VehiculoConViajesActivosException;
import com.tallerwebi.presentacion.DTO.InputsDTO.VehiculoInputDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.VehiculoOutputDTO;

import java.util.List;
import java.util.Optional;

public interface ServicioVehiculo {


    Vehiculo getById(Long Id) throws NotFoundException;
    List<Vehiculo> obtenerTodosLosVehiculosDeConductor(Long conductorId);
    List<Vehiculo> obtenerVehiculosParaConductor(Long conductorId);
    Vehiculo obtenerVehiculoConPatente(String patente) throws NotFoundException;
    Vehiculo guardarVehiculo(Vehiculo vehiculo) throws PatenteDuplicadaException, NotFoundException;
    void desactivarVehiculo(Long vehiculoId) throws NotFoundException, VehiculoConViajesActivosException;
    void verificarViajesActivos(Long vehiculoId) throws NotFoundException, VehiculoConViajesActivosException;
}
