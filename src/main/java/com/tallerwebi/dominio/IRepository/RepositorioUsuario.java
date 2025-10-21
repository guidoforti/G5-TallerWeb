package com.tallerwebi.dominio.IRepository;

import com.tallerwebi.dominio.Entity.Usuario;

import java.util.Optional;

public interface RepositorioUsuario {

    Optional <Usuario> buscarUsuario(String email, String password);
    Usuario guardar(Usuario usuario);
    Optional <Usuario> buscarPorEmail(String email);
    void modificarUsuario(Usuario usuario);

}

