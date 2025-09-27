package com.tallerwebi.dominio.ServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.tallerwebi.config.ManualModelMapper;
import com.tallerwebi.dominio.excepcion.NotFoundException;
import com.tallerwebi.presentacion.DTO.UbicacionDTO;
import org.springframework.stereotype.Service;

import com.tallerwebi.dominio.Entity.Ubicacion;
import com.tallerwebi.dominio.IRepository.RepositorioUbicacion;
import com.tallerwebi.dominio.IServicio.ServicioUbicacion;

@Service
public class ServicioUbicacionImpl implements ServicioUbicacion{

    private RepositorioUbicacion repositorioUbicacion;
    private ManualModelMapper manualModelMapper;
    
    public ServicioUbicacionImpl(RepositorioUbicacion repositorioUbicacion, ManualModelMapper manualModelMapper) {
        this.repositorioUbicacion = repositorioUbicacion;
        this.manualModelMapper = manualModelMapper;
    }
    

    @Override
    public List<UbicacionDTO> listarTodas() {
        
        return repositorioUbicacion.findAll()
                .stream()
                .map(this.manualModelMapper::toUbicacionDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UbicacionDTO obtenerUbicacion(Long ubicacionId) throws NotFoundException {
        Ubicacion ubicacion = repositorioUbicacion.buscarPorId(ubicacionId)
                .orElseThrow(() -> new NotFoundException("No existe un usuario para su sesion. Por favor inicie sesion nuevamente."));

        return this.manualModelMapper.toUbicacionDTO(ubicacion);
    }


}
