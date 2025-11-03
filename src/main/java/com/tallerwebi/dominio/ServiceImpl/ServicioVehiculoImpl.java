package com.tallerwebi.dominio.ServiceImpl;

import com.tallerwebi.dominio.Entity.Vehiculo;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Enums.EstadoDeViaje;
import com.tallerwebi.dominio.Enums.EstadoVerificacion;
import com.tallerwebi.dominio.IRepository.RepositorioConductor;
import com.tallerwebi.dominio.IRepository.RepositorioVehiculo;
import com.tallerwebi.dominio.IRepository.ViajeRepository;
import com.tallerwebi.dominio.IServicio.ServicioVehiculo;
import com.tallerwebi.dominio.excepcion.NotFoundException;
import com.tallerwebi.dominio.excepcion.PatenteDuplicadaException;
import com.tallerwebi.dominio.excepcion.VehiculoConViajesActivosException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class ServicioVehiculoImpl implements ServicioVehiculo {

    private final RepositorioVehiculo repositorioVehiculo;
    private final ViajeRepository viajeRepository;
    private final RepositorioConductor repositorioConductor;

    @Autowired
    public ServicioVehiculoImpl(RepositorioVehiculo repositorioVehiculo,
                                RepositorioConductor repositorioConductor,
                                ViajeRepository viajeRepository) {
        this.repositorioVehiculo = repositorioVehiculo;
        this.repositorioConductor = repositorioConductor;
        this.viajeRepository = viajeRepository; // [🟢 ASIGNACIÓN DE DEPENDENCIA]
    }

    @Override
    public Vehiculo getById(Long id) throws NotFoundException {
        return repositorioVehiculo.findById(id)
                .orElseThrow(() -> new NotFoundException("No se encontro un vehiculo"));
    }

    @Override
    public List<Vehiculo> obtenerTodosLosVehiculosDeConductor(Long conductorId) {
        if (conductorId == null) {
            throw new IllegalArgumentException("El ID del conductor no puede ser nulo");
        }
        return repositorioVehiculo.obtenerVehiculosParaConductor(conductorId);
    }

    @Override
    public List<Vehiculo> obtenerVehiculosParaConductor(Long conductorId) {
        if (conductorId == null) {
            throw new IllegalArgumentException("El ID del conductor no puede ser nulo");
        }
        return repositorioVehiculo.findByConductorIdAndEstadoVerificacionNot(
                conductorId,
                EstadoVerificacion.DESACTIVADO
        );
    }

    @Override
    public Vehiculo obtenerVehiculoConPatente(String patente) throws NotFoundException {
        return repositorioVehiculo.encontrarVehiculoConPatente(patente)
                .orElseThrow(() -> new NotFoundException("No se encontro un vehiculo con esta patente"));
    }

    @Override
    public Vehiculo guardarVehiculo(Vehiculo vehiculo) throws PatenteDuplicadaException {
        if(repositorioVehiculo.encontrarVehiculoConPatente(vehiculo.getPatente()).isPresent()){
            throw new PatenteDuplicadaException("La patente cargada ya existe");
        }
        if (vehiculo.getEstadoVerificacion() == null) {
            vehiculo.setEstadoVerificacion(EstadoVerificacion.PENDIENTE);
        }
        return repositorioVehiculo.guardarVehiculo(vehiculo);
    }

    @Override
    @Transactional
    public void desactivarVehiculo(Long vehiculoId)
            throws NotFoundException, VehiculoConViajesActivosException {

        // 1. Obtener Vehículo
        Vehiculo vehiculo = repositorioVehiculo.findById(vehiculoId)
                .orElseThrow(() -> new NotFoundException("Vehículo no encontrado."));

        // 2. Estados que BLOQUEAN la desactivación
        List<EstadoDeViaje> estadosActivos = Arrays.asList(
                EstadoDeViaje.DISPONIBLE,
                EstadoDeViaje.COMPLETO,
                EstadoDeViaje.EN_CURSO
        );

        List<Viaje> viajesActivos = viajeRepository.findByVehiculoAndEstadoIn(vehiculo, estadosActivos);

        if (!viajesActivos.isEmpty()) {
            throw new VehiculoConViajesActivosException(
                    "No puedes desactivar el vehículo porque está asociado a " + viajesActivos.size() +
                            " viaje(s) en curso, disponible o completo."
            );
        }
        vehiculo.setEstadoVerificacion(EstadoVerificacion.DESACTIVADO);
        repositorioVehiculo.guardarVehiculo(vehiculo);
    }
    @Override
    @Transactional
    public void verificarViajesActivos(Long vehiculoId) throws NotFoundException, VehiculoConViajesActivosException {
        Vehiculo vehiculo = repositorioVehiculo.findById(vehiculoId)
                .orElseThrow(() -> new NotFoundException("Vehículo no encontrado."));

        // Si ya está desactivado, no debería estar en la lista de gestión, pero igual validamos
        if (vehiculo.getEstadoVerificacion() == EstadoVerificacion.DESACTIVADO) {
            throw new VehiculoConViajesActivosException("El vehículo ya está de baja.");
        }

        List<EstadoDeViaje> estadosActivos = Arrays.asList(
                EstadoDeViaje.DISPONIBLE, EstadoDeViaje.COMPLETO, EstadoDeViaje.EN_CURSO
        );

        List<Viaje> viajesActivos = viajeRepository.findByVehiculoAndEstadoIn(vehiculo, estadosActivos);

        if (!viajesActivos.isEmpty()) {
            throw new VehiculoConViajesActivosException(
                    "No puedes desactivar el vehículo porque está asociado a " + viajesActivos.size() +
                            " viaje(s) en curso, disponible o completo."
            );
        }
    }

}