package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Entity.Parada;
import com.tallerwebi.dominio.IRepository.RepositorioParada;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class RepositorioParadaImpl implements RepositorioParada {

    private SessionFactory sessionFactory;


    public RepositorioParadaImpl (SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }


    @Override
    public Optional<Parada> findByid(Long id) {
       Parada parada = this.sessionFactory.getCurrentSession().get(Parada.class ,id);

        return Optional.ofNullable(parada);
    }
}
