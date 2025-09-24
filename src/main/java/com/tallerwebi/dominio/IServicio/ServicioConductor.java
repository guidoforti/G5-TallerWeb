package com.tallerwebi.dominio.IServicio;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.excepcion.CredencialesInvalidas;
import com.tallerwebi.dominio.excepcion.FechaDeVencimientoDeLicenciaInvalida;
import com.tallerwebi.dominio.excepcion.NotFoundException;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.presentacion.DTO.ConductorDTO;
import com.tallerwebi.presentacion.DTO.ConductorLoginDTO;

import java.time.LocalDate;

public interface ServicioConductor {
    Conductor login(String usuario, String contrasenia) throws CredencialesInvalidas;
    Conductor registrar(Conductor nuevoConductor) throws UsuarioExistente, FechaDeVencimientoDeLicenciaInvalida;
}
