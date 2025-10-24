package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.IRepository.RepositorioUsuario;
import com.tallerwebi.dominio.Entity.Usuario;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("repositorioUsuario")
public class RepositorioUsuarioImpl implements RepositorioUsuario {

    private SessionFactory sessionFactory;

    @Autowired
    public RepositorioUsuarioImpl(SessionFactory sessionFactory){
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Optional<Usuario> buscarUsuario(String email, String contrasenia) {

        final Session session = sessionFactory.getCurrentSession();
        String hql = "SELECT V FROM Usuario V WHERE V.email = :email AND contrasenia = :contrasenia";
        Query<Usuario> query = sessionFactory.getCurrentSession().createQuery(hql, Usuario.class)
                .setParameter("email", email)
                .setParameter("contrasenia", contrasenia);
        return query.uniqueResultOptional();
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        sessionFactory.getCurrentSession().save(usuario);
        return usuario;
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        String hql = "SELECT V FROM Usuario V WHERE V.email = :email";
        Query<Usuario> query = sessionFactory.getCurrentSession().createQuery(hql, Usuario.class)
                .setParameter("email", email);

        return query.uniqueResultOptional();
    }

    @Override
    public void modificarUsuario(Usuario usuario) {
        sessionFactory.getCurrentSession().update(usuario);
    }

}
