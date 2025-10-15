package com.tallerwebi.dominio.ServiceImpl;


import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Vehiculo;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Enums.EstadoDeViaje;
import com.tallerwebi.dominio.IRepository.ViajeRepository;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import com.tallerwebi.dominio.IServicio.ServicioVehiculo;
import com.tallerwebi.dominio.IServicio.ServicioViaje;
import com.tallerwebi.dominio.excepcion.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class ServicioViajeImpl implements ServicioViaje {


    private ViajeRepository viajeRepository;
    private ServicioConductor servicioConductor;
    private ServicioVehiculo servicioVehiculo;

    @Autowired
    public ServicioViajeImpl(ViajeRepository viajeRepository, ServicioConductor servicioConductor, ServicioVehiculo servicioVehiculo) {
        this.viajeRepository = viajeRepository;
        this.servicioConductor = servicioConductor;
        this.servicioVehiculo = servicioVehiculo;
    }


    @Override
    public Viaje obtenerViajePorId(Long id) throws NotFoundException {
        return viajeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No se encontró el viaje"));
    }

    @Override
    public void publicarViaje(Viaje viaje, Long conductorId, Long vehiculoId) throws UsuarioInexistente, NotFoundException,
            UsuarioNoAutorizadoException, AsientosDisponiblesMayorQueTotalesDelVehiculoException, DatoObligatorioException, ViajeDuplicadoException {

        // Validar datos obligatorios
        validarDatosObligatorios(viaje, conductorId, vehiculoId);

        // Obtener y validar conductor
        Conductor conductor = servicioConductor.obtenerConductor(conductorId);

        // Obtener y validar vehículo
        Vehiculo vehiculo = servicioVehiculo.getById(vehiculoId);

        // Validar que el vehículo pertenece al conductor
        if (!vehiculo.getConductor().getId().equals(conductor.getId())) {
            throw new UsuarioNoAutorizadoException("El vehículo seleccionado no pertenece al conductor");
        }

        // Validar asientos disponibles (debe ser <= asientos totales - 1 para el conductor)
        int asientosMaximos = vehiculo.getAsientosTotales() - 1;
        if (viaje.getAsientosDisponibles() > asientosMaximos) {
            throw new AsientosDisponiblesMayorQueTotalesDelVehiculoException(
                "Los asientos disponibles no pueden ser mayores a " + asientosMaximos +
                " (total del vehículo menos el asiento del conductor)"
            );
        }

        // Validar que no exista un viaje duplicado en estado DISPONIBLE o COMPLETO
        List<EstadoDeViaje> estadosProhibidos = Arrays.asList(EstadoDeViaje.DISPONIBLE, EstadoDeViaje.COMPLETO);
        List<Viaje> viajesDuplicados = viajeRepository.findByOrigenYDestinoYConductorYEstadoIn(
            viaje.getOrigen(),
            viaje.getDestino(),
            conductor,
            estadosProhibidos
        );

        if (!viajesDuplicados.isEmpty()) {
            throw new ViajeDuplicadoException(
                "Ya tenés un viaje publicado con el mismo origen y destino. " +
                "Por favor, cancelá o finalizá el viaje existente antes de crear uno nuevo."
            );
        }

        // Setear conductor y vehículo
        viaje.setConductor(conductor);
        viaje.setVehiculo(vehiculo);

        // Guardar viaje
        viajeRepository.guardarViaje(viaje);
    }

    private void validarDatosObligatorios(Viaje viaje, Long conductorId, Long vehiculoId) throws DatoObligatorioException {
        if (conductorId == null) {
            throw new DatoObligatorioException("El ID del conductor es obligatorio");
        }
        if (vehiculoId == null) {
            throw new DatoObligatorioException("El vehículo es obligatorio");
        }
        if (viaje.getFechaHoraDeSalida() == null) {
            throw new DatoObligatorioException("La fecha y hora de salida es obligatoria");
        }
        if (viaje.getFechaHoraDeSalida().isBefore(LocalDateTime.now())) {
            throw new DatoObligatorioException("La fecha y hora de salida debe ser mayor a la fecha actual");
        }
        if (viaje.getPrecio() == null || viaje.getPrecio() <= 0) {
            throw new DatoObligatorioException("El precio debe ser mayor a 0");
        }
        if (viaje.getAsientosDisponibles() == null || viaje.getAsientosDisponibles() <= 0) {
            throw new DatoObligatorioException("Los asientos disponibles deben ser mayor a 0");
        }

        // Validar ciudades (origen y destino)
        if (viaje.getOrigen() == null) {
            throw new DatoObligatorioException("La ciudad de origen es obligatoria");
        }
        if (viaje.getDestino() == null) {
            throw new DatoObligatorioException("La ciudad de destino es obligatoria");
        }
        if (viaje.getOrigen().equals(viaje.getDestino())) {
            throw new DatoObligatorioException("La ciudad de origen y destino deben ser diferentes");
        }
    }
}
