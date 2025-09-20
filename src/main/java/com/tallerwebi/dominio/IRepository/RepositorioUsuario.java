package com.tallerwebi.dominio.IRepository;

import com.tallerwebi.dominio.Entity.Usuario;

public interface RepositorioUsuario {

    Usuario buscarUsuario(String email, String password);
    void guardar(Usuario usuario);
    Usuario buscar(String email);
    void modificar(Usuario usuario);
}

