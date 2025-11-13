package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Entity.Reserva;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.Enums.EstadoReserva;
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
        String hql = "SELECT r FROM Reserva r WHERE r.viaje.id = :viajeId AND r.viajero.id = :viajeroId";
        Query<Reserva> query = sessionFactory.getCurrentSession()
                .createQuery(hql, Reserva.class)
                .setParameter("viajeId", viaje.getId())
                .setParameter("viajeroId", viajero.getId());

        List<Reserva> resultados = query.getResultList();
        return resultados.isEmpty() ? Optional.empty() : Optional.of(resultados.get(0));
    }

    @Override
    public List<Reserva> findByViaje(Viaje viaje) {
        String hql = "SELECT r FROM Reserva r WHERE r.viaje.id = :viajeId ORDER BY r.fechaSolicitud ASC";
        return sessionFactory.getCurrentSession()
                .createQuery(hql, Reserva.class)
                .setParameter("viajeId", viaje.getId())
                .getResultList();
    }

    @Override
    public List<Reserva> findByViajero(Viajero viajero) {
        String hql = "SELECT r FROM Reserva r WHERE r.viajero.id = :viajeroId ORDER BY r.fechaSolicitud DESC";
        return sessionFactory.getCurrentSession()
                .createQuery(hql, Reserva.class)
                .setParameter("viajeroId", viajero.getId())
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

    @Override
    public List<Reserva> findConfirmadasByViaje(Viaje viaje) {
        String hql = "SELECT r FROM Reserva r WHERE r.viaje.id = :viajeId AND r.estado = :estado ORDER BY r.fechaSolicitud ASC";
        return sessionFactory.getCurrentSession()
                .createQuery(hql, Reserva.class)
                .setParameter("viajeId", viaje.getId())
                .setParameter("estado", EstadoReserva.CONFIRMADA)
                .getResultList();
    }

    @Override
    public List<Reserva> findByViajeroAndEstadoInOrderByViajesFechaSalidaAsc(Viajero viajero, List<EstadoReserva> estados) {
        String hql = "SELECT r FROM Reserva r " +
                "WHERE r.viajero.id = :viajeroId " +
                "AND r.estado IN (:estados) " +
                "ORDER BY r.viaje.fechaHoraDeSalida ASC";
        return sessionFactory.getCurrentSession()
                .createQuery(hql, Reserva.class)
                .setParameter("viajeroId", viajero.getId())
                .setParameter("estados", estados)
                .getResultList();
    }

    @Override
    public List<Reserva> findViajesConfirmadosPorViajero(Viajero viajero) {
        String hql = "SELECT r FROM Reserva r " +
                "WHERE r.viajero.id = :viajeroId " +
                "AND r.estado = :estado";
        return sessionFactory.getCurrentSession()
                .createQuery(hql, Reserva.class)
                .setParameter("viajeroId", viajero.getId())
                .setParameter("estado", EstadoReserva.CONFIRMADA)
                .getResultList();
    }
    @Override
    public Optional<Reserva> findByViajeroIdAndViajeIdAndEstadoIn(Long viajeroId, Long viajeId, List<EstadoReserva> estados) {
        String hql = "SELECT r FROM Reserva r WHERE r.viajero.id = :viajeroId AND r.viaje.id = :viajeId AND r.estado IN (:estados)";

        List<Reserva> resultados = sessionFactory.getCurrentSession()
                .createQuery(hql, Reserva.class)
                .setParameter("viajeroId", viajeroId)
                .setParameter("viajeId", viajeId)
                .setParameterList("estados", estados)
                .setMaxResults(1) // Solo necesitamos saber si existe al menos uno
                .getResultList();

        return resultados.isEmpty() ? Optional.empty() : Optional.of(resultados.get(0));
    }

    @Override
    public List<Reserva> findCanceladasByViajero(Viajero viajero) {
        String hql = "SELECT r FROM Reserva r " +
            "WHERE r.viajero.id = :viajeroId " +
            "AND r.estado = :estadoCancelado"; 
            
    return sessionFactory.getCurrentSession()
            .createQuery(hql, Reserva.class)
            .setParameter("viajeroId", viajero.getId())
            .setParameter("estadoCancelado", EstadoReserva.CANCELADA_POR_CONDUCTOR) 
            .getResultList();
    }
}
