package com.tallerwebi.dominio.ServiceImpl;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Valoracion;
import com.tallerwebi.dominio.IRepository.RepositorioConductor;
import com.tallerwebi.dominio.IRepository.RepositorioValoracion;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import com.tallerwebi.dominio.IServicio.ServicioLogin;
import com.tallerwebi.dominio.excepcion.*;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ConductorPerfilOutPutDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ValoracionOutputDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service("servicioConductor")
@Transactional
public class ServicioConductorImpl implements ServicioConductor {

    private final RepositorioConductor repositorioConductor;
    private final ServicioLogin servicioLogin;
    private final RepositorioValoracion repositorioValoracion;

    @Autowired
    public ServicioConductorImpl(RepositorioConductor repositorioConductor, ServicioLogin servicioLogin, RepositorioValoracion repositorioValoracion) {
        this.repositorioConductor = repositorioConductor;
        this.servicioLogin = servicioLogin;
        this.repositorioValoracion = repositorioValoracion;
    }

    @Override
    public Conductor registrar(Conductor nuevoConductor) throws UsuarioExistente, FechaDeVencimientoDeLicenciaInvalida, EdadInvalidaException {
        // validar que la fecha sea futura
        if (nuevoConductor.getFechaDeVencimientoLicencia().isBefore(LocalDate.now())) {
            throw new FechaDeVencimientoDeLicenciaInvalida("La fecha de vencimiento de la licencia debe ser mayor a la actual");
        }
        if (nuevoConductor.getEdad() < 18) {
            throw new EdadInvalidaException("El usuario debe ser mayor de 18 años.");
        }

        nuevoConductor.setRol("CONDUCTOR");
        nuevoConductor.setActivo(true);

        servicioLogin.registrar(nuevoConductor);

        return nuevoConductor;

    }

    @Override
    public Conductor obtenerConductor(Long conductorId) throws UsuarioInexistente {
        Conductor conductor = repositorioConductor.buscarPorId(conductorId)
                .orElseThrow(() -> new UsuarioInexistente("No existe un usuario para su sesion. Por favor inicie sesion nuevamente."));

        return conductor;
    }

    @Override
    public ConductorPerfilOutPutDTO obtenerPerfilDeConductor(Long conductorId) throws UsuarioInexistente {
        Conductor conductor = repositorioConductor.buscarPorId(conductorId)
                .orElseThrow(() -> new UsuarioInexistente("No se encontró el conductor con id " + conductorId));

        List<Valoracion> valoraciones = repositorioValoracion.findByReceptorId(conductorId);

        double promedio = valoraciones.stream()
                .mapToInt(Valoracion::getPuntuacion)
                .average()
                .orElse(0.0);

        List<ValoracionOutputDTO> valoracionesDTO = valoraciones.stream()
                .map(ValoracionOutputDTO::new)
                .collect(Collectors.toList());

        return new ConductorPerfilOutPutDTO(conductor, valoracionesDTO, promedio);
    }
}