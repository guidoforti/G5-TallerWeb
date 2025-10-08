package com.tallerwebi.dominio.IServicio;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.excepcion.*;

public interface ServicioConductor {
    Conductor login(String usuario, String contrasenia) throws Exception;
    Conductor registrar(Conductor nuevoConductor) throws UsuarioExistente, FechaDeVencimientoDeLicenciaInvalida;
    Conductor obtenerConductor(Long conductorId) throws UsuarioInexistente;

    Conductor guardarConductor(Conductor conductor) throws ErrorAlGuardarConductorException;
}
