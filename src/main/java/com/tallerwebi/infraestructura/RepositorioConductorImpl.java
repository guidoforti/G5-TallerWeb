package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.IRepository.RepositorioConductor;
import com.tallerwebi.infraestructura.Datos;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@Repository("repositorioConductor")
public class RepositorioConductorImpl implements RepositorioConductor {

    SessionFactory sessionFactory;

    public RepositorioConductorImpl(SessionFactory sessionFactory) {

        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<Conductor> buscarPorEmailYContrasenia(String email, String contrasenia) throws Exception {


            String hql = "SELECT c FROM Conductor c WHERE c.email= :email AND c.contrasenia= :contrasenia";
            Conductor conductor = this.sessionFactory.getCurrentSession().createQuery(hql, Conductor.class)
                    .setParameter("email", email)
                    .setParameter("contrasenia", contrasenia)
                    .uniqueResult();

            return  Optional.ofNullable(conductor);

    }

    @Override
    public Optional<Conductor> buscarPorEmail(String email) {
        String hql = "SELECT c FROM Conductor c WHERE email = :email";
        Conductor conductor = this.sessionFactory.getCurrentSession().createQuery(hql, Conductor.class)
                .setParameter("email", email)
                .getSingleResult();

        return Optional.of(conductor);
    }

    @Override
    public Optional<Conductor> buscarPorId(Long id) {
        String hql = "SELECT c FROM Conductor c WHERE id = :id";
        Conductor conductor = this.sessionFactory.getCurrentSession().createQuery(hql, Conductor.class)
                .setParameter("id", id)
                .getSingleResult();

        return Optional.of(conductor);
    }

    @Override
    public void guardar(Conductor conductor) {
        this.sessionFactory.getCurrentSession().save(conductor);
    }
}
