package com.tallerwebi.dominio.ServiceImpl;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.IRepository.RepositorioViajero;
import com.tallerwebi.dominio.IServicio.ServicioViajero;
import com.tallerwebi.dominio.excepcion.CredencialesInvalidas;
import com.tallerwebi.dominio.excepcion.DatoObligatorioException;
import com.tallerwebi.dominio.excepcion.EdadInvalidaException;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;

@Service("servicioViajero")
@Transactional
public class ServicioViajeroImpl implements ServicioViajero{


    private final RepositorioViajero repositorioViajero;


    @Autowired
    public ServicioViajeroImpl(RepositorioViajero repositorioViajero) {
        this.repositorioViajero = repositorioViajero;

    }

    @Override
    public Viajero login(String nombreUsuario, String contrasenia) throws CredencialesInvalidas {
        Viajero viajeroEncontrado = this.repositorioViajero.buscarPorEmailYContrasenia(nombreUsuario, contrasenia)
                .orElseThrow(() -> new CredencialesInvalidas("Email o contraseña inválidos."));
       
                return viajeroEncontrado;
    }

    @Override
    public Viajero registrar(Viajero nuevoViajero) throws UsuarioExistente, EdadInvalidaException, DatoObligatorioException {
    
        if (repositorioViajero.buscarPorEmail(nuevoViajero.getEmail()).isPresent()) {
            throw new UsuarioExistente("Ya existe un usuario con ese email.");
        }

        if (nuevoViajero.getNombre() == null || nuevoViajero.getNombre().isBlank()) {
            throw new DatoObligatorioException("El nombre es obligatorio.");
        }
        
        if (nuevoViajero.getEdad() == null) {
            throw new EdadInvalidaException("La edad es obligatoria.");
        }
        if (nuevoViajero.getEdad() < 18) {
            throw new EdadInvalidaException("El usuario debe ser mayor de 18 años.");
        }
        if (nuevoViajero.getEdad() > 120) {
            throw new EdadInvalidaException("La edad ingresada no es válida.");
        }

        repositorioViajero.guardarViajero(nuevoViajero);

        return nuevoViajero;
    }

    @Override
    public Viajero obtenerViajero(Long viajeroId) throws UsuarioInexistente {
        Viajero viajero = repositorioViajero.buscarPorId(viajeroId)
                .orElseThrow(() -> new UsuarioInexistente("No existe un usuario para su sesion. Por favor inicie sesion nuevamente."));

        return viajero;
    }

}
