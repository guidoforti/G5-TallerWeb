package com.tallerwebi.dominio.IRepository;

import com.tallerwebi.dominio.Entity.Notificacion;
import com.tallerwebi.dominio.Entity.Usuario;

import java.util.List;
import java.util.Optional;

public interface RepositorioNotificacion {

    void guardar(Notificacion notificacion);
    void actualizar(Notificacion notificacion);
    Optional<Notificacion> buscarPorId(Long id);
    List<Notificacion> buscarPorUsuario(Usuario usuario, int limite);

    Long contarNoLeidasPorUsuario(Usuario usuario);
}