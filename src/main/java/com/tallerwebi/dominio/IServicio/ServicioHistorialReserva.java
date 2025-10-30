
package com.tallerwebi.dominio.IServicio;

import java.util.List;

import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioNoAutorizadoException;
import com.tallerwebi.dominio.excepcion.ViajeNoEncontradoException;
import com.tallerwebi.presentacion.DTO.OutputsDTO.HistorialReservaDTO;



public interface ServicioHistorialReserva {

    /**
     * Obtiene el historial de cambios de estado de reservas para un viaje específico.
     *
     * @param idViaje ID del viaje
     * @param usuarioEnSesion Usuario autenticado (debe ser el conductor del viaje)
     * @return Lista de registros históricos
     * @throws ViajeNoEncontradoException si el viaje no existe
     * @throws UsuarioNoAutorizadoException si el usuario no es conductor del viaje
     */

    List<HistorialReservaDTO> obtenerHistorialPorViaje(Long idViaje, Usuario usuarioEnSesion) throws ViajeNoEncontradoException, UsuarioNoAutorizadoException;

}
