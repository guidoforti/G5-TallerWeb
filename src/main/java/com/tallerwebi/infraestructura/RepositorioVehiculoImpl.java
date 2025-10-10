package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Entity.Vehiculo;
import com.tallerwebi.dominio.IRepository.RepositorioVehiculo;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class RepositorioVehiculoImpl implements RepositorioVehiculo {
    SessionFactory sessionFactory;
    //List<Vehiculo> baseDeDatos;

    @Autowired
    public RepositorioVehiculoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
         //this.baseDeDatos = new ArrayList<>(Datos.obtenerVehiculos());
    }

    @Override
    public Optional<Vehiculo> findById(Long id) {
        //return this.baseDeDatos.stream().filter(v-> v.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public List<Vehiculo> obtenerVehiculosParaConductor(Long conductorId) {
        //return this.baseDeDatos.stream().filter(v-> v.getConductor().getId().equals(conductorId)).collect(Collectors.toList());

    }

    @Override
    public Vehiculo encontrarVehiculoConPatente(String patente) {

      //return this.baseDeDatos.stream().filter(v -> v.getPatente().equals(patente)).findFirst().orElse(null);

    }

    @Override
    public Vehiculo guardarVehiculo(Vehiculo vehiculo) {
        /*Long idSiguiente = (long) (baseDeDatos.size() + 1);
        vehiculo.setId(idSiguiente);;
        this.baseDeDatos.add(vehiculo);

        return vehiculo;*/
    }
}
