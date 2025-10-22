package com.tallerwebi.dominio.ServiceImpl;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.IRepository.RepositorioConductor;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import com.tallerwebi.dominio.IServicio.ServicioLogin;
import com.tallerwebi.dominio.excepcion.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;

@Service("servicioConductor")
@Transactional
public class ServicioConductorImpl implements ServicioConductor {

    private final RepositorioConductor repositorioConductor;
    private final ServicioLogin servicioLogin;

    @Autowired
    public ServicioConductorImpl(RepositorioConductor repositorioConductor, ServicioLogin servicioLogin) {
        this.repositorioConductor = repositorioConductor;
        this.servicioLogin = servicioLogin;

    }

    @Override
    public Conductor registrar(Conductor nuevoConductor) throws UsuarioExistente, FechaDeVencimientoDeLicenciaInvalida {
        // validar que la fecha sea futura
        if (nuevoConductor.getFechaDeVencimientoLicencia().isBefore(LocalDate.now())) {
            throw new FechaDeVencimientoDeLicenciaInvalida("La fecha de vencimiento de la licencia debe ser mayor a la actual");
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
}