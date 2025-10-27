package com.tallerwebi.dominio.ServiceImpl;

import com.tallerwebi.dominio.Entity.Reserva;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.Enums.EstadoAsistencia;
import com.tallerwebi.dominio.Enums.EstadoReserva;
import com.tallerwebi.dominio.IRepository.ReservaRepository;
import com.tallerwebi.dominio.IServicio.ServicioReserva;
import com.tallerwebi.dominio.IServicio.ServicioViaje;
import com.tallerwebi.dominio.IServicio.ServicioViajero;
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
    private final ServicioViaje servicioViaje;
    private final ServicioViajero servicioViajero;

    @Autowired
    public ServicioReservaImpl(ReservaRepository reservaRepository, ServicioViaje servicioViaje, ServicioViajero servicioViajero) {
        this.reservaRepository = reservaRepository;
        this.servicioViaje = servicioViaje;
        this.servicioViajero = servicioViajero;
    }

    @Override
    public Reserva solicitarReserva(Viaje viaje, Viajero viajero) throws ReservaYaExisteException, SinAsientosDisponiblesException, ViajeYaIniciadoException, DatoObligatorioException, ViajeNoEncontradoException, NotFoundException, UsuarioNoAutorizadoException, UsuarioInexistente {
        // Validar datos obligatorios
        validarDatosObligatorios(viaje, viajero);

        // Validar que no exista una reserva previa
        validarReservaNoExistente(viaje, viajero);

        // Validar que haya asientos disponibles
        validarAsientosDisponibles(viaje);

        // Validar que el viaje no haya iniciado
        validarViajeNoIniciado(viaje);

        // Recargar entidades para asegurar que estén managed en la sesión actual
        Viaje viajeManaged = servicioViaje.obtenerViajePorId(viaje.getId());
        Viajero viajeroManaged = servicioViajero.obtenerViajero(viajero.getId());

        // Crear y guardar la reserva con entidades managed
        Reserva reserva = new Reserva();
        reserva.setViaje(viajeManaged);
        reserva.setViajero(viajeroManaged);
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
    public List<Viajero> obtenerViajerosConfirmados(Viaje viaje) throws ViajeNoEncontradoException, NotFoundException, UsuarioNoAutorizadoException {
        Viaje viajeConfirmado  = servicioViaje.obtenerViajePorId(viaje.getId());
        return reservaRepository.findByViaje(viajeConfirmado).stream()
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

    @Override
    public void confirmarReserva(Long reservaId, Long conductorId) throws NotFoundException, UsuarioNoAutorizadoException, ReservaYaExisteException, SinAsientosDisponiblesException, ViajeNoEncontradoException {
        // Obtener la reserva
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new NotFoundException("No se encontró la reserva con id: " + reservaId));

        // Validar que el conductor sea el dueño del viaje
        if (!reserva.getViaje().getConductor().getId().equals(conductorId)) {
            throw new UsuarioNoAutorizadoException("No tienes permiso para confirmar esta reserva");
        }

        // Validar que la reserva esté en estado PENDIENTE
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new ReservaYaExisteException("La reserva no está en estado PENDIENTE");
        }

        // Validar que haya asientos disponibles
        if (reserva.getViaje().getAsientosDisponibles() == null || reserva.getViaje().getAsientosDisponibles() <= 0) {
            throw new SinAsientosDisponiblesException("No hay asientos disponibles");
        }

        // Obtener el viaje completo para asegurar que tenga todos los campos (incluido version)
        Viaje viaje = servicioViaje.obtenerViajePorId(reserva.getViaje().getId());

        // Decrementar asientos disponibles
        viaje.setAsientosDisponibles(viaje.getAsientosDisponibles() - 1);

        // Cambiar estado a CONFIRMADA
        reserva.setEstado(EstadoReserva.CONFIRMADA);

        // No es necesario llamar a update/modificar porque Hibernate detectará los cambios automáticamente
        // gracias a @Transactional y dirty checking. El viaje se actualizará automáticamente.
        reservaRepository.update(reserva);
    }

    @Override
    public void rechazarReserva(Long reservaId, Long conductorId, String motivo) throws NotFoundException, UsuarioNoAutorizadoException, ReservaYaExisteException, DatoObligatorioException {
        // Validar que el motivo no esté vacío
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new DatoObligatorioException("El motivo del rechazo es obligatorio");
        }

        // Obtener la reserva
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new NotFoundException("No se encontró la reserva con id: " + reservaId));

        // Validar que el conductor sea el dueño del viaje
        if (!reserva.getViaje().getConductor().getId().equals(conductorId)) {
            throw new UsuarioNoAutorizadoException("No tienes permiso para rechazar esta reserva");
        }

        // Validar que la reserva esté en estado PENDIENTE
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new ReservaYaExisteException("La reserva no está en estado PENDIENTE");
        }

        // Cambiar estado a RECHAZADA y setear motivo
        reserva.setEstado(EstadoReserva.RECHAZADA);
        reserva.setMotivoRechazo(motivo);

        // Guardar cambios
        reservaRepository.update(reserva);
    }

    @Override
    public List<Reserva> listarViajerosConfirmados(Long viajeId, Long conductorId) throws ViajeNoEncontradoException, UsuarioNoAutorizadoException, NotFoundException {
        // Obtener el viaje
        Viaje viaje = servicioViaje.obtenerViajePorId(viajeId);

        // Validar que el conductor sea el dueño del viaje
        if (!viaje.getConductor().getId().equals(conductorId)) {
            throw new UsuarioNoAutorizadoException("No tienes permiso para ver los viajeros de este viaje");
        }

        // Obtener reservas confirmadas
        List<Reserva> reservasConfirmadas = reservaRepository.findConfirmadasByViaje(viaje);

        // Inicializar viajeros lazy para evitar LazyInitializationException en la capa de presentación
        reservasConfirmadas.forEach(reserva -> org.hibernate.Hibernate.initialize(reserva.getViajero()));

        return reservasConfirmadas;
    }

    @Override
    public void marcarAsistencia(Long reservaId, Long conductorId, String asistencia) throws NotFoundException, UsuarioNoAutorizadoException, ReservaYaExisteException, AccionNoPermitidaException, DatoObligatorioException {
        // Validar que asistencia sea un valor válido
        EstadoAsistencia estadoAsistencia;
        try {
            estadoAsistencia = EstadoAsistencia.valueOf(asistencia);
            if (estadoAsistencia == EstadoAsistencia.NO_MARCADO) {
                throw new DatoObligatorioException("El valor de asistencia debe ser PRESENTE o AUSENTE");
            }
        } catch (IllegalArgumentException e) {
            throw new DatoObligatorioException("El valor de asistencia debe ser PRESENTE o AUSENTE");
        }

        // Obtener la reserva
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new NotFoundException("No se encontró la reserva con id: " + reservaId));

        // Validar que el conductor sea el dueño del viaje
        if (!reserva.getViaje().getConductor().getId().equals(conductorId)) {
            throw new UsuarioNoAutorizadoException("No tienes permiso para marcar asistencia en este viaje");
        }

        // Validar que la reserva esté confirmada
        if (reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new ReservaYaExisteException("Solo se puede marcar asistencia en reservas confirmadas");
        }

        // Validar que estemos dentro de la ventana de tiempo (30 minutos antes de la salida en adelante)
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime fechaSalida = reserva.getViaje().getFechaHoraDeSalida();
        LocalDateTime ventanaPermitida = fechaSalida.minusMinutes(30);

        if (ahora.isBefore(ventanaPermitida)) {
            throw new AccionNoPermitidaException("Solo se puede marcar asistencia desde 30 minutos antes de la salida del viaje");
        }

        // Marcar asistencia
        reserva.setAsistencia(estadoAsistencia);

        // Guardar cambios
        reservaRepository.update(reserva);
    }
}
