package com.tallerwebi.dominio.ServiceImpl;

import com.tallerwebi.dominio.Entity.Reserva;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.Enums.EstadoReserva;
import com.tallerwebi.dominio.IRepository.ReservaRepository;
import com.tallerwebi.dominio.IServicio.ServicioReserva;
import com.tallerwebi.dominio.excepcion.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service("servicioReserva")
@Transactional
public class ServicioReservaImpl implements ServicioReserva {

    private final ReservaRepository reservaRepository;

    @Autowired
    public ServicioReservaImpl(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    @Override
    public Reserva solicitarReserva(Viaje viaje, Viajero viajero) throws ReservaYaExisteException, SinAsientosDisponiblesException, ViajeYaIniciadoException, DatoObligatorioException {
        // Validar datos obligatorios
        validarDatosObligatorios(viaje, viajero);

        // Validar que no exista una reserva previa
        validarReservaNoExistente(viaje, viajero);

        // Validar que haya asientos disponibles
        validarAsientosDisponibles(viaje);

        // Validar que el viaje no haya iniciado
        validarViajeNoIniciado(viaje);

        // Crear y guardar la reserva
        Reserva reserva = new Reserva();
        reserva.setViaje(viaje);
        reserva.setViajero(viajero);
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setFechaSolicitud(LocalDateTime.now());

        return reservaRepository.save(reserva);
    }

    @Override
    public List<Reserva> listarReservasPorViaje(Viaje viaje) {
        return reservaRepository.findByViaje(viaje);
    }

    @Override
    public List<Reserva> listarReservasPorViajero(Viajero viajero) {
        return reservaRepository.findByViajero(viajero);
    }

    @Override
    public Reserva obtenerReservaPorId(Long id) throws NotFoundException {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No se encontró la reserva con id: " + id));
    }

    @Override
    public List<Viajero> obtenerViajerosConfirmados(Viaje viaje) {
        return reservaRepository.findByViaje(viaje).stream()
                .filter(reserva -> reserva.getEstado() == EstadoReserva.CONFIRMADA)
                .map(Reserva::getViajero)
                .collect(Collectors.toList());
    }

    // --- MÉTODOS DE VALIDACIÓN PRIVADOS ---

    private void validarDatosObligatorios(Viaje viaje, Viajero viajero) throws DatoObligatorioException {
        if (viaje == null) {
            throw new DatoObligatorioException("El viaje es obligatorio");
        }
        if (viajero == null) {
            throw new DatoObligatorioException("El viajero es obligatorio");
        }
    }

    private void validarReservaNoExistente(Viaje viaje, Viajero viajero) throws ReservaYaExisteException {
        if (reservaRepository.findByViajeAndViajero(viaje, viajero).isPresent()) {
            throw new ReservaYaExisteException("Ya existe una reserva para este viajero en este viaje");
        }
    }

    private void validarAsientosDisponibles(Viaje viaje) throws SinAsientosDisponiblesException {
        if (viaje.getAsientosDisponibles() == null || viaje.getAsientosDisponibles() <= 0) {
            throw new SinAsientosDisponiblesException("No hay asientos disponibles para este viaje");
        }
    }

    private void validarViajeNoIniciado(Viaje viaje) throws ViajeYaIniciadoException {
        if (viaje.getFechaHoraDeSalida() != null && viaje.getFechaHoraDeSalida().isBefore(LocalDateTime.now())) {
            throw new ViajeYaIniciadoException("El viaje ya ha iniciado, no se pueden solicitar reservas");
        }
    }
}
