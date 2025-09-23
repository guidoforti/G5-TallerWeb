package com.tallerwebi.dominio.IServicio;

import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;

public interface ServicioLogin {

    Usuario consultarUsuario(String email, String password);
    void registrar(Usuario usuario) throws UsuarioExistente;

}
