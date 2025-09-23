package com.tallerwebi.infraestructura;


import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Ubicacion;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.IRepository.ViajeRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ViajeRepositoryImpl implements ViajeRepository {

    List<Viaje> baseDeDatos = new ArrayList<>();

    public ViajeRepositoryImpl() {
        this.baseDeDatos = Datos.obtenerViajes();
    }

    @Override
    public Viaje findById(Long id) {

        return this.baseDeDatos.stream().filter(v -> v.getId().equals(id)).findFirst().get();

    }

    @Override
    public void guardarViaje(Viaje viaje) {

        this.baseDeDatos.add(viaje);
    }

    @Override
    public void modificarViajer(Viaje viaje) {

        for (int i = 0; i < baseDeDatos.size(); i++) {
            if (baseDeDatos.get(i).getId().equals(viaje.getId())) {
                // Reemplazo el objeto completo
                baseDeDatos.set(i, viaje);
                return;
            }
        }

    }

    @Override
    public void borrarViaje(Long id) {

        for (Viaje viaje : this.baseDeDatos) {
            if (viaje.getId().equals(id)) {
                this.baseDeDatos.remove(viaje);
            }
        }
    }

    @Override
    public List<Viaje> findByOrigenYDestinoYConductor(Ubicacion origen, Ubicacion destino, Conductor conductor) {
        return baseDeDatos.stream().filter(v -> v.getConductor().equals(conductor)
                && v.getOrigen().equals(origen)
                && v.getDestino().equals(destino)).collect(Collectors.toList());
    }


}
