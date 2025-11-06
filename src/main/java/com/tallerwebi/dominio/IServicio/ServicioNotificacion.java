package com.tallerwebi.dominio.IServicio;

import com.tallerwebi.dominio.Entity.Notificacion;
import com.tallerwebi.dominio.Entity.Usuario;
import java.util.List;

public interface ServicioNotificacion {
    void guardarNotificacion(Usuario destinatario, String mensaje, String urlDestino);
    List<Notificacion> obtenerNoVistasYMarcarComoVistas(Long usuarioId);
}