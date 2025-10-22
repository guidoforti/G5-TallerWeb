package com.tallerwebi.dominio.IServicio;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.excepcion.*;

public interface ServicioConductor {

    Conductor registrar(Conductor nuevoConductor) throws UsuarioExistente, FechaDeVencimientoDeLicenciaInvalida;
    Conductor obtenerConductor(Long conductorId) throws UsuarioInexistente;

    Conductor guardarConductor(Conductor conductor) throws ErrorAlGuardarConductorException;
}
