package com.tallerwebi.dominio.IServicio;

import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;

import java.util.Optional;

public interface ServicioLogin {

    Optional <Usuario> consultarUsuario(String email, String password);
    void registrar(Usuario usuario) throws UsuarioExistente;

}
