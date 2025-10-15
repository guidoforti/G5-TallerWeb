package com.tallerwebi.infraestructura;


import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.tallerwebi.dominio.Entity.Ciudad;

import com.tallerwebi.dominio.IRepository.RepositorioCiudad;
import java.util.List;
import java.util.Optional;

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
    public Optional<Ciudad> buscarPorId(Long id) {
        Ciudad ciudad = this.sessionFactory.getCurrentSession().get(Ciudad.class, id);

        return Optional.ofNullable(ciudad);
    }

    @Override
    public Optional<Ciudad> buscarPorCoordenadas(Float latitud, Float longitud) {
        // Usar un rango de tolerancia pequeño para comparar floats (0.0001 grados = ~11 metros)
        Float tolerancia = 0.0001f;
        String hql = "SELECT c FROM Ciudad c WHERE " +
                     "c.latitud BETWEEN :latMin AND :latMax AND " +
                     "c.longitud BETWEEN :lonMin AND :lonMax";

        Ciudad ciudad = this.sessionFactory.getCurrentSession()
                .createQuery(hql, Ciudad.class)
                .setParameter("latMin", latitud - tolerancia)
                .setParameter("latMax", latitud + tolerancia)
                .setParameter("lonMin", longitud - tolerancia)
                .setParameter("lonMax", longitud + tolerancia)
                // Usamos uniqueResultOptional() si sabemos que solo puede haber 0 o 1 resultado.
                .uniqueResultOptional()
                .orElse(null);
            return Optional.ofNullable(ciudad);
    }

    @Override
    public Ciudad guardarCiudad(Ciudad ciudad) {
        this.sessionFactory.getCurrentSession().save(ciudad);
        this.sessionFactory.getCurrentSession().flush();
        return ciudad;
    }

    @Override
    public void eliminarCiudad(Long id) {
        String hql = "DELETE FROM Ciudad c WHERE c.id = :id";

        // executeUpdate() elimina la entidad directamente en la BD sin cargarla en memoria
        this.sessionFactory.getCurrentSession()
                .createQuery(hql)
                .setParameter("id", id)
                .executeUpdate();
    }

    @Override
    public Ciudad actualizarCiudad(Ciudad ciudad){
        this.sessionFactory.getCurrentSession().update(ciudad);
        return ciudad;
    }
}
