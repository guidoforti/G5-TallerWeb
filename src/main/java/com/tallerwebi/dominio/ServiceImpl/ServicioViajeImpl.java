package com.tallerwebi.dominio.ServiceImpl;


import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.IRepository.ViajeRepository;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import com.tallerwebi.dominio.IServicio.ServicioViaje;
import com.tallerwebi.presentacion.DTO.InputsDTO.ViajeInputDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServicioViajeImpl implements ServicioViaje {


    private ViajeRepository viajeRepository;

    private ServicioConductor servicioConductor;

    @Autowired
    public ServicioViajeImpl(ViajeRepository viajeRepository, ServicioConductor servicioConductor) {
        this.viajeRepository = viajeRepository;
        this.servicioConductor = servicioConductor;
    }


    @Override
    public Viaje obtenerViajePorId(Long id) {
        return null;
    }

    @Override
    public void publicarViaje(ViajeInputDTO viajeInputDTO) {

    }
}
