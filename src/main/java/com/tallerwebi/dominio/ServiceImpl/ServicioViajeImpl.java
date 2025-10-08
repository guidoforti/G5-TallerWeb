package com.tallerwebi.dominio.ServiceImpl;

import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Enums.EstadoDeViaje;
import com.tallerwebi.dominio.IRepository.ViajeRepository;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import com.tallerwebi.dominio.IServicio.ServicioViaje;
import com.tallerwebi.dominio.excepcion.UsuarioNoAutorizadoException;
import com.tallerwebi.dominio.excepcion.ViajeNoCancelableException;
import com.tallerwebi.dominio.excepcion.ViajeNoEncontradoException;
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


    @Override
    public void cancelarViaje(Long id, Usuario usuarioEnSesion) throws ViajeNoEncontradoException, UsuarioNoAutorizadoException, ViajeNoCancelableException {
        // valido que el rol sea de conductor primero que todo
        if(usuarioEnSesion.getRol() == null || !usuarioEnSesion.getRol().equalsIgnoreCase("CONDUCTOR")){
            throw new UsuarioNoAutorizadoException("Solo los conductores pueden cancelar viajes");
        }

        //busco viaje por id
        Viaje viaje = this.viajeRepository.findById(id);
        if(viaje == null){
            throw new ViajeNoEncontradoException("No se encontro un viaje con ese id");
        }

        //el viaje debe pertenecer al conductor
        if(!viaje.getConductor().getId().equals(usuarioEnSesion.getId())){
            throw new UsuarioNoAutorizadoException("El viaje debe pertenecer al conductor");
        }

        //valido el estado del viaje
        if(!(viaje.getEstado() == EstadoDeViaje.DISPONIBLE || viaje.getEstado() == EstadoDeViaje.COMPLETO)){
            throw new ViajeNoCancelableException("El viaje debe estar en estado DISPONIBLE o COMPLETO para cancelarse");
        }

        //cancelo el viaje guardando el estado
        viaje.setEstado(EstadoDeViaje.CANCELADO);
        this.viajeRepository.modificarViaje(viaje);
    }
}
