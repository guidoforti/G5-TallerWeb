package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Entity.Reserva;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.IRepository.ReservaRepository;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("repositorioReserva")
public class RepositorioReservaImpl implements ReservaRepository {

    private final SessionFactory sessionFactory;

    public RepositorioReservaImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<Reserva> findByViajeAndViajero(Viaje viaje, Viajero viajero) {
        String hql = "SELECT r FROM Reserva r WHERE r.viaje = :viaje AND r.viajero = :viajero";
        Query<Reserva> query = sessionFactory.getCurrentSession()
                .createQuery(hql, Reserva.class)
                .setParameter("viaje", viaje)
                .setParameter("viajero", viajero);

        List<Reserva> resultados = query.getResultList();
        return resultados.isEmpty() ? Optional.empty() : Optional.of(resultados.get(0));
    }

    @Override
    public List<Reserva> findByViaje(Viaje viaje) {
        String hql = "SELECT r FROM Reserva r WHERE r.viaje = :viaje ORDER BY r.fechaSolicitud ASC";
        return sessionFactory.getCurrentSession()
                .createQuery(hql, Reserva.class)
                .setParameter("viaje", viaje)
                .getResultList();
    }

    @Override
    public List<Reserva> findByViajero(Viajero viajero) {
        String hql = "SELECT r FROM Reserva r WHERE r.viajero = :viajero ORDER BY r.fechaSolicitud DESC";
        return sessionFactory.getCurrentSession()
                .createQuery(hql, Reserva.class)
                .setParameter("viajero", viajero)
                .getResultList();
    }

    @Override
    public Reserva save(Reserva reserva) {
        sessionFactory.getCurrentSession().save(reserva);
        return reserva;
    }

    @Override
    public Optional<Reserva> findById(Long id) {
        Reserva reserva = sessionFactory.getCurrentSession().get(Reserva.class, id);
        return Optional.ofNullable(reserva);
    }

    @Override
    public void update(Reserva reserva) {
        sessionFactory.getCurrentSession().update(reserva);
    }
}
