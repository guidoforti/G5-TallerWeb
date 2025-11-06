package com.tallerwebi.dominio.ServiceImpl;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import com.tallerwebi.dominio.IServicio.ServicioLogin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tallerwebi.dominio.Entity.Valoracion;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.IRepository.RepositorioValoracion;
import com.tallerwebi.dominio.IRepository.RepositorioViajero;
import com.tallerwebi.dominio.IServicio.ServicioViajero;
import com.tallerwebi.dominio.excepcion.CredencialesInvalidas;
import com.tallerwebi.dominio.excepcion.DatoObligatorioException;
import com.tallerwebi.dominio.excepcion.EdadInvalidaException;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ValoracionOutputDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ViajeroOutputDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ViajeroPerfilOutPutDTO;

@Service("servicioViajero")
@Transactional
public class ServicioViajeroImpl implements ServicioViajero{


    private final RepositorioViajero repositorioViajero;
    private final ServicioLogin servicioLogin;
    private final RepositorioValoracion repositorioValoracion;

    @Autowired
    public ServicioViajeroImpl(RepositorioViajero repositorioViajero, ServicioLogin servicioLogin, RepositorioValoracion repositorioValoracion) {
        this.repositorioViajero = repositorioViajero;
        this.servicioLogin  = servicioLogin;
        this.repositorioValoracion = repositorioValoracion;
    }

    @Override
    public Viajero registrar(Viajero nuevoViajero) throws UsuarioExistente, EdadInvalidaException, DatoObligatorioException {


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

        nuevoViajero.setRol("VIAJERO");
        nuevoViajero.setActivo(true);
        servicioLogin.registrar(nuevoViajero);

        return nuevoViajero;
    }

    @Override
    public Viajero obtenerViajero(Long viajeroId) throws UsuarioInexistente {
        Viajero viajero = repositorioViajero.buscarPorId(viajeroId)
                .orElseThrow(() -> new UsuarioInexistente("No existe un usuario para su sesion. Por favor inicie sesion nuevamente."));

        return viajero;
    }

    @Override
    public ViajeroPerfilOutPutDTO obtenerPerfilViajero(Long viajeroId) throws UsuarioInexistente {
        Viajero viajero = repositorioViajero.buscarPorId(viajeroId)
                .orElseThrow(() -> new UsuarioInexistente("No se encontró el viajero con id " + viajeroId));

        List<Valoracion> valoraciones = repositorioValoracion.findByReceptorId(viajeroId);

        double promedio = valoraciones.stream()
                .mapToInt(Valoracion::getPuntuacion)
                .average()
                .orElse(0.0);

        List<ValoracionOutputDTO> valoracionesDTO = valoraciones.stream()
                .map(ValoracionOutputDTO::new)
                .collect(Collectors.toList());

        return new ViajeroPerfilOutPutDTO(viajero, valoracionesDTO, promedio);
    }
}
