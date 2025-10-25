package com.tallerwebi.infraestructura;


import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Ciudad;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.Enums.EstadoDeViaje;
import com.tallerwebi.dominio.IRepository.ViajeRepository;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository("repositorioViaje")
public class RepositorioViajeImpl implements ViajeRepository {

    private SessionFactory sessionFactory;

    public RepositorioViajeImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<Viaje> findById(Long id) {
        Viaje viaje = sessionFactory.getCurrentSession().get(Viaje.class, id);
        return Optional.ofNullable(viaje);
    }

    @Override
    public Viaje guardarViaje(Viaje viaje) {
        this.sessionFactory.getCurrentSession().save(viaje);
        return viaje;
    }

    @Override
    public void modificarViaje(Viaje viaje) {
        this.sessionFactory.getCurrentSession().update(viaje);
    }

    @Override
    public void borrarViaje(Long id) {
        Optional<Viaje> viajeOptional = findById(id);

        viajeOptional.ifPresent(viaje -> {
            this.sessionFactory.getCurrentSession().delete(viaje);
        });
    }

    @Override
    public List<Viaje> findByOrigenYDestinoYConductor(Ciudad origen, Ciudad destino, Conductor conductor) {
        String hql = "SELECT v FROM Viaje v WHERE v.conductor = :conductor AND v.origen = :origen AND v.destino = :destino";
        return this.sessionFactory.getCurrentSession()
                .createQuery(hql, Viaje.class)
                .setParameter("conductor", conductor)
                .setParameter("origen", origen)
                .setParameter("destino", destino)
                .getResultList();
    }

    @Override
    public List<Viaje> findByConductorId(Long idConductor) {

    String hql = "SELECT v FROM Viaje v WHERE v.conductor.id = :idConductor";
    return this.sessionFactory.getCurrentSession()
            .createQuery(hql, Viaje.class)
            .setParameter("idConductor", idConductor)
            .getResultList();
    }

    @Override
    public List<Viaje> findByOrigenYDestinoYConductorYEstadoIn(Ciudad origen, Ciudad destino, Conductor conductor, List<EstadoDeViaje> estados) {
        String hql = "SELECT v FROM Viaje v WHERE v.conductor = :conductor AND v.origen = :origen AND v.destino = :destino AND v.estado IN (:estados)";
        return this.sessionFactory.getCurrentSession()
                .createQuery(hql, Viaje.class)
                .setParameter("conductor", conductor)
                .setParameter("origen", origen)
                .setParameter("destino", destino)
                .setParameter("estados", estados)
                .getResultList();
    }

    @Override
    public List<Viaje> buscarViajesPorFiltros(Ciudad origen, Ciudad destino, List<EstadoDeViaje> estados, LocalDateTime fechaDesde, Double precioMin, Double precioMax) {
        StringBuilder hql = new StringBuilder("SELECT v FROM Viaje v WHERE v.origen = :origen AND v.destino = :destino AND v.estado IN (:estados)");

        // Add optional filters
        if (fechaDesde != null) {
            hql.append(" AND v.fechaHoraDeSalida >= :fechaDesde");
        }
        if (precioMin != null) {
            hql.append(" AND v.precio >= :precioMin");
        }
        if (precioMax != null) {
            hql.append(" AND v.precio <= :precioMax");
        }

        hql.append(" ORDER BY v.fechaHoraDeSalida ASC");

        Query<Viaje> query = this.sessionFactory.getCurrentSession()
                .createQuery(hql.toString(), Viaje.class)
                .setParameter("origen", origen)
                .setParameter("destino", destino)
                .setParameter("estados", estados);

        // Set optional parameters
        if (fechaDesde != null) {
            query.setParameter("fechaDesde", fechaDesde);
        }
        if (precioMin != null) {
            query.setParameter("precioMin", precioMin);
        }
        if (precioMax != null) {
            query.setParameter("precioMax", precioMax);
        }

        return query.getResultList();
    }
}
