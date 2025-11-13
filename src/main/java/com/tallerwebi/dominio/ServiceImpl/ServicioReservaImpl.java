package com.tallerwebi.dominio.ServiceImpl;

import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import com.tallerwebi.dominio.Entity.Reserva;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.Enums.EstadoAsistencia;
import com.tallerwebi.dominio.Enums.EstadoPago;
import com.tallerwebi.dominio.Enums.EstadoReserva;
import com.tallerwebi.dominio.Enums.TipoNotificacion;
import com.tallerwebi.dominio.IRepository.ReservaRepository;
import com.tallerwebi.dominio.IServicio.ServicioNotificacion;
import com.tallerwebi.dominio.IServicio.ServicioReserva;
import com.tallerwebi.dominio.IServicio.ServicioViaje;
import com.tallerwebi.dominio.IServicio.ServicioViajero;
import com.tallerwebi.dominio.excepcion.*;
import com.tallerwebi.presentacion.DTO.OutputsDTO.NotificacionOutputDTO;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tallerwebi.dominio.Entity.HistorialReserva;
import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.IRepository.RepositorioHistorialReserva;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("servicioReserva")
@Transactional
public class ServicioReservaImpl implements ServicioReserva {

    private final ReservaRepository reservaRepository;
    private final ServicioViaje servicioViaje;
    private final ServicioViajero servicioViajero;
    private final RepositorioHistorialReserva repositorioHistorialReserva;
    private final PreferenceClient preferenceClient;
    private final ServicioNotificacion servicioNotificacion;

    @Autowired
    public ServicioReservaImpl(ReservaRepository reservaRepository, ServicioViaje servicioViaje, ServicioViajero servicioViajero,
                               RepositorioHistorialReserva repositorioHistorialReserva, PreferenceClient preferenceClient, ServicioNotificacion servicioNotificacion) {
        this.reservaRepository = reservaRepository;
        this.servicioViaje = servicioViaje;
        this.servicioViajero = servicioViajero;
        this.repositorioHistorialReserva = repositorioHistorialReserva;
        this.preferenceClient = preferenceClient;
        this.servicioNotificacion = servicioNotificacion;
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
        Reserva reservaGuardada = reservaRepository.save(reserva);

        registrarHistorial(reservaGuardada, null, viajeroManaged);
        try {
            Usuario conductor = reservaGuardada.getViaje().getConductor();
            // 1. Mensaje personalizado:
            String nombreViajero = reservaGuardada.getViajero().getNombre();
            String destinoViaje = reservaGuardada.getViaje().getDestino().getNombre();
            String mensaje = String.format("¡%s ha solicitado una reserva para tu viaje a %s!", nombreViajero, destinoViaje);
            Long viajeId = reservaGuardada.getViaje().getId();
            // 2. URL de redirección (Deep Link):
            String urlDestino = "/reserva/listar?viajeId=" + viajeId;

            // 3. Llamada al servicio genérico
            servicioNotificacion.crearYEnviar(
                    conductor,
                    TipoNotificacion.RESERVA_SOLICITADA,
                    mensaje,
                    urlDestino
            );
        } catch (Exception e) {
            // Manejo de errores de notificación no debe bloquear la reserva
            System.err.println("Error al enviar notificación de reserva: " + e.getMessage());
        }
        return reservaGuardada;
    }

    @Override
    public List<Reserva> listarReservasPorViaje(Viaje viaje) {
        List<Reserva> reservas = reservaRepository.findByViaje(viaje);

        // Inicializar viajeros lazy para evitar LazyInitializationException en la capa de presentación
        reservas.forEach(reserva -> org.hibernate.Hibernate.initialize(reserva.getViajero()));

        return reservas;
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
        List<Reserva> reservas = reservaRepository.findByViaje(viajeConfirmado);

        // Inicializar viajeros lazy para evitar LazyInitializationException
        reservas.forEach(reserva -> org.hibernate.Hibernate.initialize(reserva.getViajero()));

        return reservas.stream()
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
        EstadoReserva estadoAnterior = reserva.getEstado();

        // Obtener el viaje completo para asegurar que tenga todos los campos (incluido version)
        Viaje viaje = servicioViaje.obtenerViajePorId(reserva.getViaje().getId());

        // Decrementar asientos disponibles
        viaje.setAsientosDisponibles(viaje.getAsientosDisponibles() - 1);

        // Cambiar estado a CONFIRMADA
        reserva.setEstado(EstadoReserva.CONFIRMADA);

        Usuario conductor = reserva.getViaje().getConductor();
        registrarHistorial(reserva, estadoAnterior, conductor);
        Usuario viajero = reserva.getViajero();
        String mensaje = String.format("¡Tu reserva para el viaje a %s ha sido APROBADA!", reserva.getViaje().getDestino().getNombre());
        String url = "/reserva/misViajes"; // Redirigir a sus viajes confirmados

        servicioNotificacion.crearYEnviar(viajero, TipoNotificacion.RESERVA_APROBADA, mensaje, url);
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

        EstadoReserva estadoAnterior = reserva.getEstado();

        // Cambiar estado a RECHAZADA y setear motivo
        reserva.setEstado(EstadoReserva.RECHAZADA);
        reserva.setMotivoRechazo(motivo);

        Usuario conductor = reserva.getViaje().getConductor(); // El conductor que realiza la acción
        registrarHistorial(reserva, estadoAnterior, conductor);
        Usuario viajero = reserva.getViajero();
        String mensaje = String.format("Tu reserva para el viaje a %s ha sido RECHAZADA. Motivo: %s", reserva.getViaje().getDestino().getNombre(), motivo);
        String url = "/reserva/misReservasActivas"; // Redirigir a sus reservas pendientes/rechazadas

        servicioNotificacion.crearYEnviar(viajero, TipoNotificacion.RESERVA_RECHAZADA, mensaje, url);
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
        EstadoReserva estadoAnterior = reserva.getEstado();


        // Marcar asistencia
        reserva.setAsistencia(estadoAsistencia);
        Usuario conductor = reserva.getViaje().getConductor();
        // Guardar cambios
        registrarHistorial(reserva, estadoAnterior, conductor);
        reservaRepository.update(reserva);
    }

    private void registrarHistorial(Reserva reserva, EstadoReserva estadoAnterior, Usuario usuarioQueRealizaLaAccion) {
        HistorialReserva historial = new HistorialReserva();
        historial.setReserva(reserva);
        historial.setViaje(reserva.getViaje());
        historial.setViajero(reserva.getViajero());
        historial.setConductor(usuarioQueRealizaLaAccion);
        historial.setFechaEvento(LocalDateTime.now());
        historial.setEstadoAnterior(estadoAnterior);
        historial.setEstadoNuevo(reserva.getEstado()); // El estado nuevo ya está seteado en la Reserva

        repositorioHistorialReserva.save(historial);
    }

    @Override
    public List<Reserva> listarReservasActivasPorViajero(Long viajeroId) throws UsuarioInexistente {
        // Obtener el viajero y validar que existe
        Viajero viajero = servicioViajero.obtenerViajero(viajeroId);

        // Definir los estados a buscar
        List<EstadoReserva> estados = List.of(EstadoReserva.PENDIENTE, EstadoReserva.RECHAZADA , EstadoReserva.CONFIRMADA);

        // Obtener las reservas filtradas y ordenadas por fecha de salida del viaje
        List<Reserva> reservas = reservaRepository.findByViajeroAndEstadoInOrderByViajesFechaSalidaAsc(viajero, estados);

        List<Reserva> reservasFiltradas = reservas.stream()
                .filter(r -> {
                    // Caso 1: Si la reserva está CONFIRMADA...
                    if (r.getEstado() == EstadoReserva.CONFIRMADA) {
                        // ...solo la queremos si AÚN NO está pagada.
                        return r.getEstadoPago() != EstadoPago.PAGADO;
                    }

                    // Caso 2: Si no está confirmada (es PENDIENTE o RECHAZADA)...
                    // ...la queremos siempre.
                    return true;
                })
                .collect(Collectors.toList());

        // Inicializar lazy loads para evitar LazyInitializationException
        reservasFiltradas.forEach(reserva -> {
            if (reserva.getViaje() != null) {
                org.hibernate.Hibernate.initialize(reserva.getViaje().getOrigen());
                org.hibernate.Hibernate.initialize(reserva.getViaje().getDestino());
                org.hibernate.Hibernate.initialize(reserva.getViaje().getConductor());
            }
        });

        return reservasFiltradas;
    }

    @Override
    public List<Reserva> listarViajesConfirmadosPorViajero(Long viajeroId) throws UsuarioInexistente {
        // Obtener el viajero y validar que existe
        Viajero viajero = servicioViajero.obtenerViajero(viajeroId);

        // Obtener todas las reservas confirmadas del viajero
        List<Reserva> reservas = reservaRepository.findViajesConfirmadosPorViajero(viajero);

        // Inicializar lazy loads para evitar LazyInitializationException
        reservas.forEach(reserva -> {
            if (reserva.getViaje() != null) {
                org.hibernate.Hibernate.initialize(reserva.getViaje().getOrigen());
                org.hibernate.Hibernate.initialize(reserva.getViaje().getDestino());
                org.hibernate.Hibernate.initialize(reserva.getViaje().getConductor());
                org.hibernate.Hibernate.initialize(reserva.getViaje().getVehiculo());
            }
        });

        return reservas;
    }

    @Override
    public Preference crearPreferenciaDePago(Long reservaId, Long viajeroId) throws UsuarioInexistente, NotFoundException, UsuarioNoAutorizadoException, BadRequestException, MPException, MPApiException, AccionNoPermitidaException {

        Viajero viajero = servicioViajero.obtenerViajero(viajeroId);
        Optional <Reserva> reservaOpt = reservaRepository.findById(reservaId);
        if (reservaOpt.isEmpty()){
            throw new NotFoundException("la reserva con id : " + reservaId + " no existe");
        }
        if (reservaOpt.get().getViajero().getId() != viajero.getId()) {
            throw new UsuarioNoAutorizadoException("la reserva que esta intenando abonar no pertenece al usuario id " + viajeroId);
        }
        if (!reservaOpt.get().getEstado().equals(EstadoReserva.CONFIRMADA)) {
            throw new AccionNoPermitidaException("solo pueden ser abonadas las reservas con estado CONFIRMADO");
        }
        if (reservaOpt.get().getEstadoPago().equals(EstadoPago.PAGADO)) {
            throw new AccionNoPermitidaException("la reserva ya se encuentra abonada");
        }

        Reserva reserva = reservaOpt.get();
        Viaje viaje = reserva.getViaje();


        PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                .id(reserva.getId().toString())
                .title("Reserva UnRumbo: " + viaje.getOrigen().getNombre() + " a " + viaje.getDestino().getNombre())
                .description("Asiento para el viaje del " + viaje.getFechaHoraDeSalida().toLocalDate().toString())
                .quantity(1)
                .currencyId("ARS")
                .unitPrice(new BigDecimal(viaje.getPrecio()))
                .build();

        List<PreferenceItemRequest> items = new ArrayList<>();
        items.add(itemRequest);

        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success("localhost:8080/spring/reserva/pago/exitoso?reservaId=" + reserva.getId())
                .failure("localhost:8080/spring/reserva/pago/fallido?reservaId=" + reserva.getId())
                .pending("localhost:8080/spring/reserva/pago/pendiente?reservaId=" + reserva.getId())
                .build();


        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(items)
                .backUrls(backUrls)
                .autoReturn("approved")
                .build();

        return preferenceClient.create(preferenceRequest);
    }

    @Override
    public Reserva confirmarPagoReserva(Long reservaId, Long viajeroId) throws NotFoundException, UsuarioNoAutorizadoException, AccionNoPermitidaException {

        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new NotFoundException("La reserva " + reservaId + " no existe."));

        if (!reserva.getViajero().getId().equals(viajeroId)) {
            throw new UsuarioNoAutorizadoException("No tienes permiso para ver esta confirmación de pago.");
        }

        // 3. Validar estado (defensa extra)
        if (reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new AccionNoPermitidaException("El pago solo puede confirmarse para reservas APROBADAS.");
        }
        reserva.setEstadoPago(EstadoPago.PAGADO);
        reservaRepository.update(reserva);
        Usuario conductor = reserva.getViaje().getConductor();
        String nombreViajero = reserva.getViajero().getNombre();
        String destinoViaje = reserva.getViaje().getDestino().getNombre();
        Long viajeId = reserva.getViaje().getId();

        String mensajeConductor = String.format("¡Pago recibido! El viajero %s ha abonado su asiento para el viaje a %s.", nombreViajero, destinoViaje);
        String urlConductor = "/reserva/viajerosConfirmados?viajeId=" + viajeId; // Redirigir a la lista de viajeros confirmados

        servicioNotificacion.crearYEnviar(conductor, TipoNotificacion.PAGO_RECIBIDO, mensajeConductor, urlConductor);

        return reserva;
    }
    @Override
    public Boolean tieneReservaActiva(Long viajeroId, Long viajeId) {
        // Definimos qué estados se consideran "activos"
        // PENDIENTE (esperando confirmación) o CONFIRMADA (esperando pago/viaje)
        List<EstadoReserva> estadosActivos = Arrays.asList(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA);

        // Si la búsqueda devuelve algún resultado, la reserva ya existe y está activa.
        return reservaRepository.findByViajeroIdAndViajeIdAndEstadoIn(viajeroId, viajeId, estadosActivos).isPresent();
    }

     @Override
    public List<Reserva> listarViajesCanceladosPorViajero(Long viajeroId) throws UsuarioInexistente {
        
    Viajero viajero = servicioViajero.obtenerViajero(viajeroId);

    List<Reserva> reservas = reservaRepository.findCanceladasByViajero(viajero);

    reservas.forEach(reserva -> {
        if (reserva.getViaje() != null) {
            org.hibernate.Hibernate.initialize(reserva.getViaje().getOrigen());
            org.hibernate.Hibernate.initialize(reserva.getViaje().getDestino());
            org.hibernate.Hibernate.initialize(reserva.getViaje().getConductor());
            org.hibernate.Hibernate.initialize(reserva.getViaje().getVehiculo());
        }
    });

    return reservas;
    }

    @Override
public Reserva cancelarReservaPorViajero(Long idReserva, Usuario usuarioEnSesion)
        throws UsuarioNoAutorizadoException, ReservaNoEncontradaException {

    // Validar que sea viajero
    if (usuarioEnSesion.getRol() == null || !usuarioEnSesion.getRol().equalsIgnoreCase("VIAJERO")) {
        throw new UsuarioNoAutorizadoException("Solo los viajeros pueden cancelar sus reservas.");
    }

    Optional<Reserva> reservaOpt = reservaRepository.findById(idReserva);
    if (reservaOpt.isEmpty()) {
        throw new ReservaNoEncontradaException("No se encontró la reserva especificada.");
    }

    Reserva reserva = reservaOpt.get();
    Viaje viaje = reserva.getViaje();

    // Verificar que la reserva pertenezca al viajero en sesión
    if (!reserva.getViajero().getId().equals(usuarioEnSesion.getId())) {
        throw new UsuarioNoAutorizadoException("No puede cancelar una reserva que no le pertenece.");
    }

    // Verificar estado cancelable
    if (!(reserva.getEstado() == EstadoReserva.CONFIRMADA || reserva.getEstado() == EstadoReserva.PENDIENTE)) {
        throw new IllegalStateException("La reserva no puede cancelarse en este estado.");
    }

    // Guardar estado anterior para el historial
    EstadoReserva estadoAnterior = reserva.getEstado();

    // Actualizar estado
    reserva.setEstado(EstadoReserva.CANCELADA_POR_VIAJERO);

    // Cambiar estado de pago
    if (reserva.getEstadoPago() == EstadoPago.PAGADO) {
        reserva.setEstadoPago(EstadoPago.REEMBOLSO_PENDIENTE);
    }

    reservaRepository.update(reserva);

    // Crear historial
    HistorialReserva historial = new HistorialReserva();
    historial.setReserva(reserva);
    historial.setViaje(viaje);
    historial.setViajero((Viajero) usuarioEnSesion);
    historial.setConductor(viaje.getConductor());
    historial.setFechaEvento(LocalDateTime.now());
    historial.setEstadoAnterior(estadoAnterior);
    historial.setEstadoNuevo(reserva.getEstado());
    repositorioHistorialReserva.save(historial);

    // Enviar notificación al conductor
    try {
        String mensaje = String.format(
                "El viajero %s ha cancelado su reserva en el viaje hacia %s.",
                usuarioEnSesion.getNombre(),
                viaje.getDestino().getNombre()
        );

        String url = "/reserva/misReservas";
        servicioNotificacion.crearYEnviar(
                viaje.getConductor(),
                TipoNotificacion.VIAJE_CANCELADO,
                mensaje,
                url
        );
    } catch (Exception e) {
        System.err.println("Error al enviar notificación al conductor: " + e.getMessage());
    }

    // 🔹 Retornar la reserva actualizada
    return reserva;
}
}
