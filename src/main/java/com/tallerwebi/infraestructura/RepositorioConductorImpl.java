package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.IRepository.RepositorioConductor;
import com.tallerwebi.infraestructura.Datos;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@Repository("repositorioConductor")
public class RepositorioConductorImpl implements RepositorioConductor {

    private Map<Long, Conductor> conductores = new HashMap<>();
    private Long secuenciaId = 1L;

    public RepositorioConductorImpl() {
        for( Conductor conductor : Datos.obtenerConductores() ) {
            conductor.setId(this.secuenciaId++);
            this.conductores.put(conductor.getId(), conductor);
        }
    }

    @Override
    public Optional<Conductor> buscarPorEmailYContrasenia(String email, String contrasenia) {
        return conductores.values().stream()
                .filter(c -> c.getEmail().equals(email) && c.getContrasenia().equals(contrasenia))
                .findFirst();
    }

    @Override
    public Optional<Conductor> buscarPorEmail(String email) {
        return conductores.values().stream()
                .filter(c -> c.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public Optional<Conductor> buscarPorId(Long id) {
        return Optional.ofNullable(conductores.get(id));
    }

    @Override
    public boolean guardar(Conductor conductor) {
        // validamos que no exista un email repetido
        boolean existe = this.conductores.values().stream()
                .anyMatch(c -> c.getEmail().equals(conductor.getEmail()));
        if (existe) {
            return false;
        }

        // asignamos id incremental antes de guardar
        conductor.setId(secuenciaId++);
        this.conductores.put(conductor.getId(), conductor);
        return true;
    }
}
