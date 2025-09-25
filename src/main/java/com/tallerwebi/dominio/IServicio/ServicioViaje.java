package com.tallerwebi.dominio.IServicio;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Ubicacion;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.excepcion.ViajeExistente;
import com.tallerwebi.dominio.excepcion.ViajeInexistente;
import com.tallerwebi.presentacion.DTO.InputsDTO.ViajeInputDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ViajeOutputDTO;

import java.util.Optional;

public interface ServicioViaje {
    ViajeOutputDTO crearViaje(ViajeInputDTO nuevoViaje) throws ViajeExistente;
    ViajeOutputDTO modificarViaje(ViajeInputDTO viajeModificado) throws ViajeInexistente;
    boolean borrarViaje(Long viajeId) throws ViajeInexistente;
    ViajeOutputDTO buscarPorId(Long viajeId) throws ViajeInexistente;
    Optional<ViajeOutputDTO> encontrarPorOrigenDestinoYConductor(Ubicacion origen, Ubicacion destino, Conductor conductor);
}