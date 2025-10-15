package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Entity.Vehiculo;
import com.tallerwebi.dominio.IRepository.RepositorioVehiculo;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class RepositorioVehiculoImpl implements RepositorioVehiculo {
    SessionFactory sessionFactory;
    // List<Vehiculo> baseDeDatos;

    @Autowired
    public RepositorioVehiculoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        // this.baseDeDatos = new ArrayList<>(Datos.obtenerVehiculos());
    }

    @Override
    public Optional<Vehiculo> findById(Long id) {
        Vehiculo vehiculo = sessionFactory.getCurrentSession().get(Vehiculo.class, id);
        return Optional.ofNullable(vehiculo);
    }

    @Override
    public List<Vehiculo> obtenerVehiculosParaConductor(Long conductorId) {
        String hql = "SELECT v FROM Vehiculo v WHERE v.conductor.id = :conductorId";
        return sessionFactory.getCurrentSession().createQuery(hql, Vehiculo.class)
                .setParameter("conductorId", conductorId)
                .getResultList();
    }

    @Override
    public Optional<Vehiculo> encontrarVehiculoConPatente(String patente) {
        String hql = "SELECT v FROM Vehiculo v WHERE v.patente = :patente";
        Query<Vehiculo> query = sessionFactory.getCurrentSession().createQuery(hql, Vehiculo.class)
                .setParameter("patente", patente);
        return query.uniqueResultOptional();
    }

    @Override
    public Vehiculo guardarVehiculo(Vehiculo vehiculo) {
        sessionFactory.getCurrentSession().save(vehiculo);
        return vehiculo;
    }
}
