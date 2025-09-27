package com.tallerwebi.dominio.ServiceImpl;

import com.tallerwebi.config.ManualModelMapper;
import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Ubicacion;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Entity.Vehiculo;
import com.tallerwebi.dominio.IRepository.RepositorioViaje;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import com.tallerwebi.dominio.IServicio.ServicioUbicacion;
import com.tallerwebi.dominio.IServicio.ServicioVehiculo;
import com.tallerwebi.dominio.IServicio.ServicioViaje;
import com.tallerwebi.dominio.excepcion.ViajeExistente;
import com.tallerwebi.dominio.excepcion.ViajeInexistente;
import com.tallerwebi.dominio.excepcion.NotFoundException;
import com.tallerwebi.presentacion.DTO.InputsDTO.ViajeInputDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ViajeOutputDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service("servicioViaje")
public class ServicioViajeImpl implements ServicioViaje {

    private final RepositorioViaje repositorioViaje;
    private final ServicioConductor servicioConductor;
    private final ServicioUbicacion servicioUbicacion;
    private final ServicioVehiculo servicioVehiculo;
    private final ManualModelMapper manualModelMapper;

    @Autowired
    public ServicioViajeImpl(
            RepositorioViaje repositorioViaje,
            ServicioConductor servicioConductor,
            ServicioUbicacion servicioUbicacion,
            ServicioVehiculo servicioVehiculo,
            ManualModelMapper manualModelMapper
    ) {
        this.repositorioViaje = repositorioViaje;
        this.servicioConductor = servicioConductor;
        this.servicioUbicacion = servicioUbicacion;
        this.servicioVehiculo = servicioVehiculo;
        this.manualModelMapper = manualModelMapper;
    }

    @Override
    public ViajeOutputDTO crearViaje(ViajeInputDTO nuevoViaje) throws ViajeExistente {
        Conductor conductor;
        Ubicacion origen;
        Ubicacion destino;
        Vehiculo vehiculo;

        try {
            conductor = manualModelMapper.toConductor(
                    servicioConductor.obtenerConductor(nuevoViaje.getConductorId())
            );
        } catch (Exception e) {
            throw new RuntimeException("No se encontró el conductor con id " + nuevoViaje.getConductorId(), e);
        }

        try {
            origen = manualModelMapper.toUbicacion(
                    servicioUbicacion.obtenerUbicacion(nuevoViaje.getIdOrigen())
            );
        } catch (Exception e) {
            throw new RuntimeException("No se encontró la ubicación de origen con id " + nuevoViaje.getIdOrigen(), e);
        }

        try {
            destino = manualModelMapper.toUbicacion(
                    servicioUbicacion.obtenerUbicacion(nuevoViaje.getIdDestino())
            );
        } catch (Exception e) {
            throw new RuntimeException("No se encontró la ubicación de destino con id " + nuevoViaje.getIdDestino(), e);
        }

        try {
            vehiculo = manualModelMapper.toVehiculo(
                    servicioVehiculo.getById(nuevoViaje.getIdVehiculo()),
                    conductor
            );
        } catch (Exception e) {
            throw new RuntimeException("No se encontró el vehículo con id " + nuevoViaje.getIdVehiculo(), e);
        }

        // Verificar si ya existe un viaje con mismo conductor, origen y destino
        Optional<Viaje> viajeExistente = repositorioViaje.encontrarPorOrigenDestinoYConductor(origen, destino, conductor);
        if (viajeExistente.isPresent()) {
            throw new ViajeExistente("Ya existe un viaje con ese conductor, origen y destino");
        }

        // Crear viaje
        Viaje viaje = new Viaje(
                null,
                conductor,
                null, // viajeros
                origen,
                destino,
                null, // paradas
                nuevoViaje.getFechaHoraDeSalida(),
                nuevoViaje.getPrecio(),
                nuevoViaje.getAsientosDisponibles(),
                nuevoViaje.getAsientosDisponibles(), // asientosTotales = disponibles al inicio
                LocalDateTime.now(),
                vehiculo
        );

        repositorioViaje.guardarViaje(viaje);

        return manualModelMapper.toViajeOutputDTO(viaje);
    }



    @Override
    public ViajeOutputDTO modificarViaje(ViajeInputDTO viajeModificado) throws ViajeInexistente {
        Optional<Viaje> existente = repositorioViaje.buscarPorId(viajeModificado.getConductorId().longValue());
        if (existente.isEmpty()) {
            throw new ViajeInexistente("El viaje que querés modificar no existe");
        }

        // Aquí podrías hacer un mapping de los campos modificables y luego guardar
        Viaje viaje = existente.get();
        viaje.setFechaHoraDeSalida(viajeModificado.getFechaHoraDeSalida());
        viaje.setPrecio(viajeModificado.getPrecio());
        viaje.setAsientosDisponibles(viajeModificado.getAsientosDisponibles());

        repositorioViaje.modificarViajer(viaje);

        return manualModelMapper.toViajeOutputDTO(viaje);
    }

    @Override
    public boolean borrarViaje(Long viajeId) throws ViajeInexistente {
        Optional<Viaje> viaje = repositorioViaje.buscarPorId(viajeId);
        if (viaje.isEmpty()) throw new ViajeInexistente("El viaje no existe");
        return repositorioViaje.borrarViaje(viajeId);
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
