package com.tallerwebi.infraestructura;

import org.springframework.stereotype.Repository;

import com.tallerwebi.dominio.IRepository.RepositorioViajero;
import com.tallerwebi.dominio.Entity.Viajero;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@Repository
public class RepositorioViajeroImpl implements RepositorioViajero {

    private final SessionFactory sessionFactory;

    @Autowired
    public RepositorioViajeroImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<Viajero> buscarPorEmailYContrasenia(String email, String contrasenia) {
        String hql = "SELECT V FROM Viajero V WHERE V.email = :email AND contrasenia = :contrasenia";
        Query<Viajero> query = sessionFactory.getCurrentSession().createQuery(hql, Viajero.class)
                .setParameter("email", email)
                .setParameter("contrasenia", contrasenia);

        return query.uniqueResultOptional();
    }

    @Override
    public Optional<Viajero> buscarPorEmail(String email) {
        String hql = "SELECT V FROM Viajero V WHERE V.email = :email";
        Query<Viajero> query = sessionFactory.getCurrentSession().createQuery(hql, Viajero.class)
                .setParameter("email", email);

        return query.uniqueResultOptional();
    }

    @Override
    public Optional<Viajero> buscarPorId(Long id) {
        Viajero viajero = sessionFactory.getCurrentSession().get(Viajero.class, id);
        return Optional.ofNullable(viajero);
    }

    @Override
    public Viajero guardarViajero(Viajero viajero) {
        sessionFactory.getCurrentSession().save(viajero);
        return viajero;
    }

}
