package com.tallerwebi.dominio.IRepository;
import com.tallerwebi.dominio.Entity.Notificacion;
import java.util.List;

public interface RepositorioNotificacion {
    List<Notificacion> findByUsuarioIdAndVistaFalse(Long usuarioId);
    void save(Notificacion notificacion);
    void update(Notificacion notificacion);
}
