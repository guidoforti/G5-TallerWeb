package com.tallerwebi.dominio.ServiceImpl;


import com.tallerwebi.dominio.Entity.Notificacion;
import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.IRepository.RepositorioNotificacion;
import com.tallerwebi.dominio.IServicio.ServicioNotificacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service("servicioNotificacion")
@Transactional
public class ServicioNotificacionImpl implements ServicioNotificacion{

    private RepositorioNotificacion repositorioNotificacion;

    @Autowired
    public ServicioNotificacionImpl(RepositorioNotificacion repositorioNotificacion) {
        this.repositorioNotificacion = repositorioNotificacion;
    }
    @Override // [Ahora compilable]
    public List<Notificacion> obtenerNoVistasYMarcarComoVistas(Long usuarioId) {
        // 1. Obtener todas las no vistas
        List<Notificacion> noVistas = repositorioNotificacion.findByUsuarioIdAndVistaFalse(usuarioId);

        // 2. Marcar como vistas y guardar
        for (Notificacion n : noVistas) {
            n.setVista(true);
            // El @Transactional se encarga del dirty checking
        }
        return noVistas;
    }

    @Override
    public void guardarNotificacion(Usuario destinatario, String mensaje, String urlDestino) {
        // Debes implementar esta lógica aquí
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(destinatario);
        notificacion.setMensaje(mensaje);
        notificacion.setUrlDestino(urlDestino);
        // La fecha y vista se inicializan en la entidad
        repositorioNotificacion.save(notificacion);
    }
}
