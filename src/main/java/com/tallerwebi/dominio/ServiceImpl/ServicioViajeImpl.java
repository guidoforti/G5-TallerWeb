package com.tallerwebi.dominio.ServiceImpl;


import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.IRepository.ViajeRepository;
import com.tallerwebi.dominio.IServicio.ServicioViaje;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServicioViajeImpl implements ServicioViaje {


    private ViajeRepository viajeRepository;


    public ServicioViajeImpl(ViajeRepository viajeRepository) {
        this.viajeRepository = viajeRepository;
    }

    @Override
    public Viaje obtenerViajePorId(Long id) {

        Viaje viajeADevolver = viajeRepository.findById(id);

        return  viajeADevolver;

    }
}
