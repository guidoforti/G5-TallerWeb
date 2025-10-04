package com.tallerwebi.infraestructura;


import java.util.List;

import com.tallerwebi.dominio.excepcion.NotFoundException;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.tallerwebi.dominio.Entity.Ciudad;

import com.tallerwebi.dominio.IRepository.RepositorioCiudad;

@Repository
public class RepositorioCiudadImpl implements RepositorioCiudad {

    SessionFactory sessionFactory;

    @Autowired
    public RepositorioCiudadImpl( SessionFactory sessionFactory) {
     this.sessionFactory = sessionFactory;
    }

    @Override
    public List<Ciudad> findAll() {

        String hql = "SELECT c FROM Ciudad c";
        return this.sessionFactory.getCurrentSession().createQuery(hql, Ciudad.class).getResultList();

    }

    @Override
    public Ciudad buscarPorId(Long id) {
        String hql = "SELECT c FROM Ciudad c WHERE c.id = :id";
        return this.sessionFactory.getCurrentSession().createQuery(hql, Ciudad.class).setParameter("id", id).getSingleResult();
    }

    @Override
    public Ciudad guardarCiudad(Ciudad ciudad) {

        this.sessionFactory.getCurrentSession().save(ciudad);
        return  ciudad;
    }

    @Override
    public void eliminarCiudad(Long id) {
        String hql = "SELECT c FROM Ciudad c WHERE c.id = :id";
        Ciudad ciudad = this.sessionFactory.getCurrentSession()
                .createQuery(hql, Ciudad.class)
                .setParameter("id", id)
                .getSingleResult();
        this.sessionFactory.getCurrentSession().delete(ciudad);
    }

    @Override
    public Ciudad actualizarCiudad(Ciudad ciudad){
        this.sessionFactory.getCurrentSession().update(ciudad);
        return ciudad;
    }
}
