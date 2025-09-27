package com.tallerwebi.dominio.ServiceImpl;

import com.tallerwebi.config.ManualModelMapper;
import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.IRepository.RepositorioConductor;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import com.tallerwebi.dominio.excepcion.CredencialesInvalidas;
import com.tallerwebi.dominio.excepcion.FechaDeVencimientoDeLicenciaInvalida;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service("servicioConductor")
public class ServicioConductorImpl implements ServicioConductor {

    private final RepositorioConductor repositorioConductor;


    @Autowired
    public ServicioConductorImpl(RepositorioConductor repositorioConductor, ManualModelMapper manualModelMapper) {
        this.repositorioConductor = repositorioConductor;

    }


    @Override
    public Conductor login(String usuario, String contrasenia) throws CredencialesInvalidas {
        Conductor conductorEncontrado = this.repositorioConductor.buscarPorEmailYContrasenia(usuario, contrasenia)
                .orElseThrow(() -> new CredencialesInvalidas("Email o contraseña inválidos"));

        return conductorEncontrado;
    }

    @Override
    public Conductor registrar(Conductor nuevoConductor) throws UsuarioExistente, FechaDeVencimientoDeLicenciaInvalida {
        // validar si ya existe un conductor con ese email
        if (repositorioConductor.buscarPorEmail(nuevoConductor.getEmail()).isPresent()) {
            throw new UsuarioExistente("Ya existe un usuario con ese email");
        }

        // validar que la fecha sea futura
        if (nuevoConductor.getFechaDeVencimientoLicencia().isBefore(LocalDate.now())) {
            throw new FechaDeVencimientoDeLicenciaInvalida("La fecha de vencimiento de la licencia debe ser mayor a la actual");
        }

        repositorioConductor.guardar(nuevoConductor);

        return nuevoConductor;

    }

    @Override
    public Conductor obtenerConductor(Long conductorId) throws UsuarioInexistente {
        Conductor conductor = repositorioConductor.buscarPorId(conductorId)
                .orElseThrow(() -> new UsuarioInexistente("No existe un usuario para su sesion. Por favor inicie sesion nuevamente."));

        return conductor;
    }
}
