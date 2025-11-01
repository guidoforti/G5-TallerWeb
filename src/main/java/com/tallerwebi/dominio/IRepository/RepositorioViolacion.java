package com.tallerwebi.dominio.IRepository;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.ViolacionConductor;
import com.tallerwebi.dominio.Enums.TipoViolacion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RepositorioViolacion {

    ViolacionConductor guardar(ViolacionConductor violacion);

    Optional<ViolacionConductor> buscarPorId(Long id);

    List<ViolacionConductor> buscarPorConductorYActivaTrue(Conductor conductor);

    List<ViolacionConductor> buscarPorActivaTrueYFechaExpiracionAnteriorA(LocalDateTime fecha);

    int contarPorConductorYTipoYActivaTrue(Conductor conductor, TipoViolacion tipo);

    List<ViolacionConductor> buscarPorConductorOrderByFechaViolacionDesc(Conductor conductor);

    void actualizar(ViolacionConductor violacion);
}
