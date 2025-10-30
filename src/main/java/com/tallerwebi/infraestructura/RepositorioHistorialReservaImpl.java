package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Entity.HistorialReserva;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.IRepository.RepositorioHistorialReserva;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("repositorioHistorialReserva")
public class RepositorioHistorialReservaImpl implements RepositorioHistorialReserva {

    private final SessionFactory sessionFactory;

    public RepositorioHistorialReservaImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<HistorialReserva> findByViaje(Viaje viaje) {
        String hql = "FROM HistorialReserva hr WHERE hr.viaje.id = :viajeId ORDER BY hr.fechaEvento ASC";

        return sessionFactory.getCurrentSession()
                .createQuery(hql, HistorialReserva.class)
                .setParameter("viajeId", viaje.getId())
                .getResultList();
    }

    @Override
    public Optional<HistorialReserva> findByViajeId(Long idViaje) {
        String hql = "FROM HistorialReserva hr WHERE hr.viaje.id = :viajeId";
        
        List<HistorialReserva> resultados = sessionFactory.getCurrentSession()
                .createQuery(hql, HistorialReserva.class)
                .setParameter("viajeId", idViaje)
                .setMaxResults(1) 
                .getResultList();

        return resultados.isEmpty() ? Optional.empty() : Optional.of(resultados.get(0));
    }

    @Override
    public void save(HistorialReserva historialReserva) {
        sessionFactory.getCurrentSession().save(historialReserva);
    }
}
