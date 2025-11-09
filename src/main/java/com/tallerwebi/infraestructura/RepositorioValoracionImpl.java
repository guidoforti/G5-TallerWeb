package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Entity.Valoracion;
import com.tallerwebi.dominio.IRepository.RepositorioValoracion;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.IRepository.RepositorioUsuario;
import com.tallerwebi.dominio.Entity.Usuario;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioValoracion")
public class RepositorioValoracionImpl implements RepositorioValoracion {

    private SessionFactory sessionFactory;

    @Autowired
    public RepositorioValoracionImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Guarda la entidad Valoracion utilizando el método save de Hibernate.
     */
    @Override
    public void save(Valoracion valoracion) {
        this.sessionFactory.getCurrentSession().save(valoracion);
    }

    /**
     * Implementación usando HQL para buscar valoraciones por el ID del usuario receptor.
     */
    @Override
    public List<Valoracion> findByReceptorId(Long receptorId) {
        Session session = this.sessionFactory.getCurrentSession();
        
        String hql = "FROM Valoracion v WHERE v.receptor.id = :receptorId ORDER BY v.fecha DESC";
        
        return session.createQuery(hql, Valoracion.class)
                      .setParameter("receptorId", receptorId)
                      .getResultList();
    }

    @Override
    public boolean yaExisteValoracionParaViaje(Long emisorId, Long receptorId, Long viajeId) {
        String hql = "SELECT COUNT(v.id) FROM Valoracion v "
                + "WHERE v.emisor.id = :emisorId "
                + "AND v.receptor.id = :receptorId "
                + "AND v.viaje.id = :viajeId";

        Long count = sessionFactory.getCurrentSession()
                .createQuery(hql, Long.class)
                .setParameter("emisorId", emisorId)
                .setParameter("receptorId", receptorId)
                .setParameter("viajeId", viajeId)
                .uniqueResult();

        return count != null && count > 0;
    }
}
