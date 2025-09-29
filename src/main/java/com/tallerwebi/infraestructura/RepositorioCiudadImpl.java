package com.tallerwebi.infraestructura;


import java.util.List;

import org.springframework.stereotype.Repository;

import com.tallerwebi.dominio.Entity.Ciudad;

import com.tallerwebi.dominio.IRepository.RepositorioCiudad;

@Repository
public class RepositorioCiudadImpl implements RepositorioCiudad {
    private List<Ciudad> ciudades;

    public RepositorioCiudadImpl() {
        this.ciudades = Datos.obtenerCiudades();
    }

    @Override
    public List<Ciudad> findAll() {
        return ciudades;
    }

    @Override
    public Ciudad buscarPorId(Long id) {
        return this.ciudades.stream().filter(c-> c.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public Ciudad guardarCiudad(Ciudad ciudad) {
        Long idSiguiente = (long) this.ciudades.size();
        idSiguiente+= 1;
        ciudad.setId(idSiguiente);
        this.ciudades.add(ciudad);
        return  ciudad;
    }

    @Override
    public void eliminarCiudad(Long id) {
        Ciudad ciudad = this.ciudades.stream().filter(c-> c.getId().equals(id)).findFirst().orElse(null);
        this.ciudades.remove(ciudad);

    }

    @Override
    public Ciudad actualizarCiudad(Ciudad ciudad) {
        Ciudad ciudadExistente = this.ciudades.stream()
                .filter(c -> c.getId().equals(ciudad.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Ciudad no encontrada con ID: " + ciudad.getId()));


        int indice = this.ciudades.indexOf(ciudadExistente);
        if (indice != -1) {
            this.ciudades.set(indice, ciudad);
        }

        return ciudad;
    }
}
