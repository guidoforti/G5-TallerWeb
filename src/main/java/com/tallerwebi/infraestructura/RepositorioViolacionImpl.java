package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.ViolacionConductor;
import com.tallerwebi.dominio.Enums.TipoViolacion;
import com.tallerwebi.dominio.IRepository.RepositorioViolacion;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository("repositorioViolacion")
public class RepositorioViolacionImpl implements RepositorioViolacion {

    private final SessionFactory sessionFactory;

    public RepositorioViolacionImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public ViolacionConductor guardar(ViolacionConductor violacion) {
        sessionFactory.getCurrentSession().save(violacion);
        return violacion;
    }

    @Override
    public Optional<ViolacionConductor> buscarPorId(Long id) {
        ViolacionConductor violacion = sessionFactory.getCurrentSession()
            .get(ViolacionConductor.class, id);
        return Optional.ofNullable(violacion);
    }

    @Override
    public List<ViolacionConductor> buscarPorConductorYActivaTrue(Conductor conductor) {
        String hql = "FROM ViolacionConductor v WHERE v.conductor = :conductor AND v.activa = true";
        return sessionFactory.getCurrentSession()
            .createQuery(hql, ViolacionConductor.class)
            .setParameter("conductor", conductor)
            .getResultList();
    }

    @Override
    public List<ViolacionConductor> buscarPorActivaTrueYFechaExpiracionAnteriorA(LocalDateTime fecha) {
        String hql = "FROM ViolacionConductor v WHERE v.activa = true AND v.fechaExpiracion < :fecha";
        return sessionFactory.getCurrentSession()
            .createQuery(hql, ViolacionConductor.class)
            .setParameter("fecha", fecha)
            .getResultList();
    }

    @Override
    public int contarPorConductorYTipoYActivaTrue(Conductor conductor, TipoViolacion tipo) {
        String hql = "SELECT COUNT(v) FROM ViolacionConductor v WHERE v.conductor = :conductor AND v.tipo = :tipo AND v.activa = true";
        Long count = sessionFactory.getCurrentSession()
            .createQuery(hql, Long.class)
            .setParameter("conductor", conductor)
            .setParameter("tipo", tipo)
            .getSingleResult();
        return count.intValue();
    }

    @Override
    public List<ViolacionConductor> buscarPorConductorOrderByFechaViolacionDesc(Conductor conductor) {
        String hql = "FROM ViolacionConductor v WHERE v.conductor = :conductor ORDER BY v.fechaViolacion DESC";
        return sessionFactory.getCurrentSession()
            .createQuery(hql, ViolacionConductor.class)
            .setParameter("conductor", conductor)
            .getResultList();
    }

    @Override
    public void actualizar(ViolacionConductor violacion) {
        sessionFactory.getCurrentSession().update(violacion);
    }
}
