package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Entity.Notificacion;
import com.tallerwebi.dominio.IRepository.RepositorioNotificacion;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("repositorioNotificacion")
public class RepositorioNotificacionImpl implements RepositorioNotificacion {

    private final SessionFactory sessionFactory;

    public RepositorioNotificacionImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<Notificacion> findByUsuarioIdAndVistaFalse(Long usuarioId) {
        // [🟢 HQL CORREGIDO] Usa el nombre del campo 'usuario' y la columna 'usuario_id'
        // NOTA: HQL navega por la PROPIEDAD (n.usuario), no por el nombre de la columna (usuario_id).
        String hql = "FROM Notificacion n WHERE n.usuario.id = :usuarioId AND n.vista = FALSE ORDER BY n.fechaCreacion DESC";

        return sessionFactory.getCurrentSession()
                .createQuery(hql, Notificacion.class)
                .setParameter("usuarioId", usuarioId)
                .getResultList();
    }

    @Override
    public void save(Notificacion notificacion) {
        sessionFactory.getCurrentSession().save(notificacion);
    }

    @Override
    public void update(Notificacion notificacion) {
        sessionFactory.getCurrentSession().update(notificacion);
    }
}