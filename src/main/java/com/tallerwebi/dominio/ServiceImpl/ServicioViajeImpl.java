package com.tallerwebi.dominio.ServiceImpl;

import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Vehiculo;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Enums.EstadoDeViaje;
import com.tallerwebi.dominio.IRepository.ViajeRepository;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import com.tallerwebi.dominio.IServicio.ServicioVehiculo;
import com.tallerwebi.dominio.IServicio.ServicioViaje;
import com.tallerwebi.presentacion.DTO.InputsDTO.ViajeInputDTO;
import com.tallerwebi.dominio.excepcion.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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


    @Override
    public void cancelarViaje(Long id, Usuario usuarioEnSesion) throws ViajeNoEncontradoException, UsuarioNoAutorizadoException, ViajeNoCancelableException {
        // valido que el rol sea de conductor primero que todo
        if(usuarioEnSesion.getRol() == null || !usuarioEnSesion.getRol().equalsIgnoreCase("CONDUCTOR")){
            throw new UsuarioNoAutorizadoException("Solo los conductores pueden cancelar viajes");
        }

        //busco viaje por id
        Optional<Viaje> viajeOptional = viajeRepository.findById(id);
        if (viajeOptional.isEmpty()) {
        throw new ViajeNoEncontradoException("No se encontró un viaje con ese ID");
        }

        //Esta linea obtiene el objeto viaje
          Viaje viaje = viajeOptional.get();

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

    @Override
    public List<Viaje> listarViajesPorConductor(Conductor conductor) throws UsuarioNoAutorizadoException {
   
        if (conductor == null) {
            throw new UsuarioNoAutorizadoException("El conductor es nulo, la sesión no es válida.");
        }

        // obtener viajes del conductor
        return this.viajeRepository.findByConductorId(conductor.getId());
    }

}