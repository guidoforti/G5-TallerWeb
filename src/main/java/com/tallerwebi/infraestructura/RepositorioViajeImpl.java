package com.tallerwebi.infraestructura;


import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Ubicacion;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.IRepository.RepositorioViaje;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class RepositorioViajeImpl implements RepositorioViaje {

    List<Viaje> baseDeDatos = new ArrayList<>();

    public RepositorioViajeImpl() {
        this.baseDeDatos = Datos.obtenerViajes();
    }

    // Constructor alternativo para tests (lista vacía)
    public RepositorioViajeImpl(boolean vacio) {
        this.baseDeDatos = new ArrayList<>();
    }

    @Override
    public Optional<Viaje> buscarPorId(Long id) {

        return this.baseDeDatos.stream().filter(v -> v.getId().equals(id)).findFirst();

    }

    @Override
    public boolean modificarViaje(Viaje viaje) {
        for (int i = 0; i < baseDeDatos.size(); i++) {
            if (baseDeDatos.get(i).getId().equals(viaje.getId())) {
                // Reemplazo el objeto completo
                baseDeDatos.set(i, viaje);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean borrarViaje(Long id) {
        return baseDeDatos.removeIf(v -> v.getId().equals(id));
    }


    @Override
    public Optional<Viaje> encontrarPorOrigenDestinoYConductor(Ubicacion origen, Ubicacion destino, Conductor conductor) {
        return baseDeDatos.stream().filter(v -> v.getConductor().equals(conductor)
                && v.getOrigen().equals(origen)
                && v.getDestino().equals(destino)).findFirst();
    }

    @Override
    public boolean guardarViaje(Viaje viaje) {
        // validamos que no exista un email repetido
        Optional<Viaje> viajeExistente = this.encontrarPorOrigenDestinoYConductor(viaje.getOrigen(), viaje.getDestino(), viaje.getConductor());
        if (viajeExistente.isPresent()) {
            return false;
        }
        this.baseDeDatos.add(viaje);
        return true;
    }


}
