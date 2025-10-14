package com.tallerwebi.infraestructura;


import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Ciudad;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Enums.EstadoDeViaje;
import com.tallerwebi.dominio.IRepository.ViajeRepository;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("repositorioViaje")
public class RepositorioViajeImpl implements ViajeRepository {

    private SessionFactory sessionFactory;

    public RepositorioViajeImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Viaje findById(Long id) {
        return this.sessionFactory.getCurrentSession().get(Viaje.class, id);
    }

    @Override
    public void guardarViaje(Viaje viaje) {
        this.sessionFactory.getCurrentSession().save(viaje);
    }

    @Override
    public void modificarViajer(Viaje viaje) {
        this.sessionFactory.getCurrentSession().update(viaje);
    }

    @Override
    public void borrarViaje(Long id) {
        Viaje viaje = findById(id);
        if (viaje != null) {
            this.sessionFactory.getCurrentSession().delete(viaje);
        }
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
}
