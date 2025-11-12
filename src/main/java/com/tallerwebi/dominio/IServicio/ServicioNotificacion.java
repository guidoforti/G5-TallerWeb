package com.tallerwebi.dominio.IServicio;

import com.tallerwebi.dominio.Entity.Notificacion;
import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.Enums.TipoNotificacion;
import com.tallerwebi.dominio.excepcion.NotFoundException;

import java.util.List;

public interface ServicioNotificacion {

    void crearYEnviar(Usuario destinatario, TipoNotificacion tipo, String mensaje, String urlDestino);

    Long contarNoLeidas(Long idUsuario) throws NotFoundException;

    List<Notificacion> obtenerYMarcarComoLeidas(Long idUsuario) throws NotFoundException;

    List<Notificacion> buscarUltimas(Long idUsuario) throws NotFoundException;

    void marcarComoLeida(Long idNotificacion) throws NotFoundException;
}