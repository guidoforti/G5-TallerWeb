package com.tallerwebi.dominio.ServiceImpl;

import com.tallerwebi.config.ManualModelMapper;
import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Ubicacion;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.IRepository.RepositorioConductor;
import com.tallerwebi.dominio.IRepository.RepositorioViaje;
import com.tallerwebi.dominio.IServicio.ServicioViaje;
import com.tallerwebi.dominio.excepcion.ViajeExistente;
import com.tallerwebi.dominio.excepcion.ViajeInexistente;
import com.tallerwebi.presentacion.DTO.InputsDTO.ViajeInputDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ViajeOutputDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("servicioViaje")
public class ServicioViajeImpl implements ServicioViaje {

    private final RepositorioViaje repositorioViaje;
    private final RepositorioConductor repositorioConductor;
    private final ManualModelMapper manualModelMapper;

    @Autowired
    public ServicioViajeImpl(RepositorioViaje repositorioViaje, RepositorioConductor repositorioConductor, ManualModelMapper manualModelMapper) {
        this.repositorioViaje = repositorioViaje;
        this.repositorioConductor = repositorioConductor;
        this.manualModelMapper = manualModelMapper;
        this.servicioConductor = servicioConductor;
    }

    @Override
    public ViajeOutputDTO crearViaje(ViajeInputDTO nuevoViaje) throws ViajeExistente {

        Viaje nuevoViajeEntity = manualModelMapper.toViaje(nuevoViaje);
        boolean guardado = repositorioViaje.guardarViaje(nuevoViajeEntity);

        if (!guardado) {
            throw new ViajeExistente("Ya existe un viaje con mismo origen, destino y conductor");
        }

        return manualModelMapper.toViajeOutputDTO(nuevoViaje);
    }

    @Override
    public ViajeOutputDTO modificarViaje(ViajeInputDTO viajeModificado) throws ViajeInexistente {
        boolean modificado = repositorioViaje.modificarViaje(viajeModificado);

        if (!modificado) {
            throw new ViajeInexistente("El viaje a modificar no existe");
        }

        return manualModelMapper.toViajeOutputDTO(viajeModificado);
    }

    @Override
    public boolean borrarViaje(Long viajeId) throws ViajeInexistente {
        boolean borrado = repositorioViaje.borrarViaje(viajeId);

        if (!borrado) {
            throw new ViajeInexistente("El viaje a borrar no existe");
        }

        return true;
    }

    @Override
    public ViajeOutputDTO buscarPorId(Long viajeId) throws ViajeInexistente {
        Viaje viaje = repositorioViaje.buscarPorId(viajeId)
                .orElseThrow(() -> new ViajeInexistente("El viaje no existe"));

        return manualModelMapper.toViajeOutputDTO(viaje);
    }

    @Override
    public Optional<ViajeOutputDTO> encontrarPorOrigenDestinoYConductor(Ubicacion origen, Ubicacion destino, Conductor conductor) {
        return repositorioViaje.encontrarPorOrigenDestinoYConductor(origen, destino, conductor)
                .map(manualModelMapper::toViajeOutputDTO);
    }
}