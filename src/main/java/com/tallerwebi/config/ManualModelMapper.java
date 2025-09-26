package com.tallerwebi.config;
import com.tallerwebi.dominio.Entity.*;
import com.tallerwebi.presentacion.DTO.*;
import com.tallerwebi.presentacion.DTO.InputsDTO.ConductorDTO;
import com.tallerwebi.presentacion.DTO.InputsDTO.VehiculoInputDTO;
import com.tallerwebi.presentacion.DTO.InputsDTO.ViajeInputDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ViajeOutputDTO;
import com.tallerwebi.presentacion.DTO.ViajeroDTO;
import com.tallerwebi.presentacion.DTO.UbicacionDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.VehiculoOutputDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ManualModelMapper {


    // -----------------------------
    // VIAJE
    // -----------------------------

    public Viaje toViaje(ViajeInputDTO dto, Conductor conductor, Vehiculo vehiculo , Ubicacion origen , Ubicacion destino) {
        if (dto == null) return null;

        Viaje viaje = new Viaje();
        viaje.setConductor(conductor);
        viaje.setOrigen(origen);
        viaje.setDestino(destino);
        viaje.setFechaHoraDeSalida(dto.getFechaHoraDeSalida());
        viaje.setPrecio(dto.getPrecio());
        viaje.setAsientosDisponibles(dto.getAsientosDisponibles());
        viaje.setAsientosTotales(vehiculo.getAsientosTotales());
        viaje.setVehiculo(vehiculo);
        viaje.setFechaDeCreacion(LocalDateTime.now());

        return viaje;
    }

    public ViajeOutputDTO toViajeOutputDTO(Viaje viaje) {
        if (viaje == null) return null;

        ViajeOutputDTO dto = new ViajeOutputDTO();
        dto.setOrigen(toUbicacionDTO(viaje.getOrigen()));
        dto.setDestino(toUbicacionDTO(viaje.getDestino()));
        dto.setParadas(toUbicacionDTOList(viaje.getParadas()));
        dto.setFechaHoraDeSalida(viaje.getFechaHoraDeSalida());
        dto.setPrecio(viaje.getPrecio());
        dto.setAsientosDisponibles(viaje.getAsientosDisponibles());
        dto.setAsientosTotales(viaje.getAsientosTotales());
        dto.setFechaDeCreacion(viaje.getFechaDeCreacion());
        dto.setVehiculo(toVehiculoOutputDTO(viaje.getVehiculo()));
        dto.setNombreConductor(viaje.getConductor() != null ? viaje.getConductor().getNombre() : null);
        

        if (viaje.getViajeros() != null) {
            dto.setViajeros(toViajeroDTOList(viaje.getViajeros()));
        }

        return dto;
    }

    // -----------------------------
    // VIAJERO
    // -----------------------------

    public List<ViajeroDTO> toViajeroDTOList(List<Viajero> viajeros) {
        if (viajeros == null) return null;
        return viajeros.stream()
                .map(this::toViajeroDTO)
                .collect(Collectors.toList());
    }

    public ViajeroDTO toViajeroDTO(Viajero viajero) {
        if (viajero == null) return null;
        
        ViajeroDTO dto = new ViajeroDTO();
        dto.setId(viajero.getId());
        dto.setNombre(viajero.getNombre());
        dto.setEdad(viajero.getEdad());
        // No incluimos la lista de viajes para evitar referencias circulares
        return dto;
    }

    // -----------------------------
    // CONDUCTOR
    // -----------------------------

    public Conductor toConductor(ConductorLoginDTO dto) {
        if (dto == null) return null;

        Conductor conductor = new Conductor();
        conductor.setEmail(dto.getEmail());
        conductor.setContrasenia(dto.getContrasenia());

        return conductor;
    }

    public Conductor toConductor(ConductorDTO dto) {
        if (dto == null) return null;

        Conductor conductor = new Conductor();
        conductor.setId(dto.getId());
        conductor.setEmail(dto.getEmail());
        conductor.setContrasenia(dto.getContrasenia());
        conductor.setNombre(dto.getNombre());
        conductor.setFechaDeVencimientoLicencia(dto.getFechaDeVencimientoLicencia());
        conductor.setViajes(dto.getViajes());

        return conductor;
    }

    public ConductorDTO toConductorDTO(Conductor entity) {
        if (entity == null) return null;

        ConductorDTO conductor = new ConductorDTO();
        conductor.setId(entity.getId());
        conductor.setNombre(entity.getNombre());
        conductor.setEmail(entity.getEmail());
        conductor.setContrasenia(entity.getContrasenia());
        conductor.setViajes(entity.getViajes());
        conductor.setFechaDeVencimientoLicencia(entity.getFechaDeVencimientoLicencia());

        return conductor;
    }
    // -----------------------------
    // UBICACION
    // -----------------------------

    public Ubicacion toUbicacion(UbicacionDTO dto) {
        if (dto == null) return null;
        Ubicacion u = new Ubicacion();
        u.setDireccion(dto.getDireccion());
        u.setLatitud(dto.getLatitud());
        u.setLongitud(dto.getLongitud());
        return u;
    }

    private List<Ubicacion> toUbicacionList(List<UbicacionDTO> dtos) {
        return dtos == null ? null :
                dtos.stream().map(this::toUbicacion).collect(Collectors.toList());
    }

    private List<UbicacionDTO> toUbicacionDTOList(List<Ubicacion> entities) {
        return entities == null ? null :
                entities.stream().map(this::toUbicacionDTO).collect(Collectors.toList());
    }

    public UbicacionDTO toUbicacionDTO(Ubicacion u) {
        if (u == null) return null;
        UbicacionDTO dto = new UbicacionDTO();
        dto.setDireccion(u.getDireccion());
        dto.setLatitud(u.getLatitud());
        dto.setLongitud(u.getLongitud());
        return dto;
    }

    // -----------------------------
    // VEHICULO
    // -----------------------------

    public VehiculoOutputDTO toVehiculoOutputDTO(Vehiculo v) {
        if (v == null) return null;
        VehiculoOutputDTO dto = new VehiculoOutputDTO();
        dto.setModelo(v.getModelo());
        dto.setAnio(v.getAnio());
        dto.setPatente(v.getPatente());
        dto.setAsientosTotales(v.getAsientosTotales());
        dto.setEstadoVerificacion(v.getEstadoVerificacion());
        return dto;
    }

    // -----------------------------
    // VEHICULO
    // -----------------------------

    public Vehiculo toVehiculo(VehiculoInputDTO dto, Conductor conductor) {
        if (dto == null) return null;

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setConductor(conductor);
        vehiculo.setModelo(dto.getModelo());
        vehiculo.setAnio(dto.getAnio());
        vehiculo.setPatente(dto.getPatente());
        vehiculo.setAsientosTotales(dto.getAsientosTotales());
        vehiculo.setEstadoVerificacion(dto.getEstadoVerificacion());

        return vehiculo;
    }
}
