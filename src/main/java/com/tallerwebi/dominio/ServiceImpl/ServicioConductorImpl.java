package com.tallerwebi.dominio.ServiceImpl;

import com.tallerwebi.config.ManualModelMapper;
import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.IRepository.RepositorioConductor;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import com.tallerwebi.dominio.excepcion.CredencialesInvalidas;
import com.tallerwebi.dominio.excepcion.FechaDeVencimientoDeLicenciaInvalida;
import com.tallerwebi.dominio.excepcion.NotFoundException;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.presentacion.DTO.ConductorDTO;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.presentacion.DTO.ConductorDTO;
import com.tallerwebi.presentacion.DTO.ConductorLoginDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service("servicioConductor")
public class ServicioConductorImpl implements ServicioConductor {

    private final RepositorioConductor repositorioConductor;
    private final ManualModelMapper manualModelMapper;

    @Autowired
    public ServicioConductorImpl(RepositorioConductor repositorioConductor, ManualModelMapper manualModelMapper) {
        this.repositorioConductor = repositorioConductor;
        this.manualModelMapper = manualModelMapper;
    }


    @Override
    public ConductorDTO login(String usuario, String contrasenia) throws CredencialesInvalidas {
        Conductor conductorEncontrado = this.repositorioConductor.buscarPorEmailYContrasenia(usuario, contrasenia)
                .orElseThrow(() -> new CredencialesInvalidas("Email o contraseña inválidos"));

        return this.manualModelMapper.toConductorDTO(conductorEncontrado);
    }

    @Override
    public ConductorDTO registrar(Conductor nuevoConductor) throws UsuarioExistente, FechaDeVencimientoDeLicenciaInvalida {
        // validar si ya existe un conductor con ese email
        if (repositorioConductor.buscarPorEmail(nuevoConductor.getEmail()).isPresent()) {
            throw new UsuarioExistente("Ya existe un usuario con ese email");
        }

        // validar que la fecha sea futura
        if (nuevoConductor.getFechaDeVencimientoLicencia().isBefore(LocalDate.now())) {
            throw new FechaDeVencimientoDeLicenciaInvalida("La fecha de vencimiento de la licencia debe ser mayor a la actual");
        }

        repositorioConductor.guardar(nuevoConductor);

        return this.manualModelMapper.toConductorDTO(nuevoConductor);

    }

    @Override
    public ConductorDTO obtenerConductor(Long conductorId) throws UsuarioInexistente {
        Conductor conductor = repositorioConductor.buscarPorId(conductorId)
                .orElseThrow(() -> new UsuarioInexistente("No existe un usuario para su sesion. Por favor inicie sesion nuevamente."));

        return manualModelMapper.toConductorDTO(conductor);
    }
}
