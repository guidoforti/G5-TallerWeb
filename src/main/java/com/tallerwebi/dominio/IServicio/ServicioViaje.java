package com.tallerwebi.dominio.IServicio;

import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.excepcion.*;

public interface ServicioViaje {

    Viaje obtenerViajePorId(Long id);

    void publicarViaje(Viaje viaje, Long conductorId, Long vehiculoId) throws UsuarioInexistente, NotFoundException,
            UsuarioNoAutorizadoException, AsientosDisponiblesMayorQueTotalesDelVehiculoException, DatoObligatorioException;
}
