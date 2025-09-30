package com.tallerwebi.infraestructura;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.IRepository.RepositorioViajero;

@Repository
public class RepositorioViajeroImpl implements RepositorioViajero{

    private Map<Long, Viajero> viajeros = new HashMap();
    private Long proximoId = 1L;

    public RepositorioViajeroImpl(){
        for(Viajero v : Datos.obtenerViajeros()){
            v.setId(this.proximoId++);
            viajeros.put(v.getId(), v);
        }
    }

    @Override
    public Optional<Viajero> buscarPorEmailYContrasenia(String email, String contrasenia) {
        return null;
    }

    @Override
    public Optional<Viajero> buscarPorEmail(String email) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buscarPorEmail'");
    }

    @Override
    public Optional<Viajero> buscarPorId(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buscarPorId'");
    }

    @Override
    public boolean guardar(Viajero viajero) {
        // Chequeo que exista
        Boolean viajeroExistente = this.viajeros.values().stream().anyMatch(v -> v.getEmail().equals(viajero.getEmail()));
        if(viajeroExistente == true){
            return false;
        } 
        
        // Si no existe, asigno ID previo a guardarlo
        viajero.setId(proximoId++);
        viajeros.put(viajero.getId(), viajero);
        return true;
    }
    
}
