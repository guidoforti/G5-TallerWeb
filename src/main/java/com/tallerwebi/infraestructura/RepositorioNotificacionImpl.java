package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Entity.Notificacion;
import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.IRepository.RepositorioNotificacion;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("repositorioNotificacion")
public class RepositorioNotificacionImpl implements RepositorioNotificacion {

    private final SessionFactory sessionFactory;

    @Autowired
    public RepositorioNotificacionImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public void guardar(Notificacion notificacion) {
        getCurrentSession().save(notificacion);
    }

    @Override
    public void actualizar(Notificacion notificacion) {
        getCurrentSession().update(notificacion);
    }

    @Override
    public Optional<Notificacion> buscarPorId(Long id) {
        // HQL con named parameters (aunque find es directo)
        return Optional.ofNullable(getCurrentSession().find(Notificacion.class, id));
    }

    @Override
    public List<Notificacion> buscarPorUsuario(Usuario usuario, int limite) {
        // Buscar todas las notificaciones ordenadas por fecha (las más recientes primero)
        // Usamos una lista para el historial de la campanita
        String hql = "FROM Notificacion n WHERE n.destinatario = :usuario ORDER BY n.fechaCreacion DESC";
        return getCurrentSession().createQuery(hql, Notificacion.class)
                .setParameter("usuario", usuario)
                .setMaxResults(limite) // Limitar la lista
                .list();
    }

    @Override
    public Long contarNoLeidasPorUsuario(Usuario usuario) {
        String hql = "SELECT COUNT(n) FROM Notificacion n WHERE n.destinatario = :usuario AND n.leida = false";
        return getCurrentSession().createQuery(hql, Long.class)
                .setParameter("usuario", usuario)
                .uniqueResult();
    }
}