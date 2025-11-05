package com.tallerwebi.dominio.IServicio;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.excepcion.*;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ConductorPerfilOutPutDTO;

public interface ServicioConductor {

    Conductor registrar(Conductor nuevoConductor) throws UsuarioExistente, FechaDeVencimientoDeLicenciaInvalida, EdadInvalidaException;
    Conductor obtenerConductor(Long conductorId) throws UsuarioInexistente;
    ConductorPerfilOutPutDTO obtenerPerfilDeConductor(Long conductorId) throws UsuarioInexistente;
}
