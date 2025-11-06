package com.tallerwebi.dominio.IServicio;

import java.util.List;

import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.Entity.Valoracion;
import com.tallerwebi.dominio.excepcion.DatoObligatorioException;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.presentacion.DTO.InputsDTO.ValoracionNuevaInputDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ValoracionOutputDTO;

public interface ServicioValoracion {
    void valorarUsuario(Usuario emisor, ValoracionNuevaInputDTO dto)
        throws UsuarioInexistente, DatoObligatorioException;

    List<Valoracion> obtenerValoracionesDeUsuario(Long usuarioId);

    Double calcularPromedioValoraciones(Long usuarioId);
}
