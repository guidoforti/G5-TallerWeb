package com.tallerwebi.dominio.ServiceImpl;

import com.tallerwebi.dominio.Entity.*;
import com.tallerwebi.dominio.Enums.EstadoDeViaje;
import com.tallerwebi.dominio.Enums.EstadoPago;
import com.tallerwebi.dominio.Enums.EstadoReserva;
import com.tallerwebi.dominio.Enums.TipoNotificacion;
import com.tallerwebi.dominio.IRepository.RepositorioHistorialReserva;
import com.tallerwebi.dominio.IRepository.RepositorioParada;
import com.tallerwebi.dominio.IRepository.ReservaRepository;
import com.tallerwebi.dominio.IRepository.ViajeRepository;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import com.tallerwebi.dominio.IServicio.ServicioNotificacion;
import com.tallerwebi.dominio.IServicio.ServicioVehiculo;
import com.tallerwebi.dominio.IServicio.ServicioViaje;
import com.tallerwebi.dominio.excepcion.*;
import com.tallerwebi.dominio.util.CalculadoraDistancia;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Duration;
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
    private RepositorioParada repositorioParada;
    private ReservaRepository reservaRepository;
    private ServicioNotificacion servicioNotificacion;
    private RepositorioHistorialReserva repositorioHistorialReserva;

    @Autowired
    public ServicioViajeImpl(ViajeRepository viajeRepository, ServicioConductor servicioConductor,
                             ServicioVehiculo servicioVehiculo, RepositorioParada repositorioParada,
                             ReservaRepository reservaRepository, ServicioNotificacion servicioNotificacion, RepositorioHistorialReserva repositorioHistorialReserva) {
        this.viajeRepository = viajeRepository;
        this.servicioConductor = servicioConductor;
        this.servicioVehiculo = servicioVehiculo;
        this.repositorioParada = repositorioParada;
        this.reservaRepository = reservaRepository;
        this.servicioNotificacion = servicioNotificacion;
        this.repositorioHistorialReserva = repositorioHistorialReserva;
    }

    @Override
    public Viaje obtenerViajePorId(Long id) throws NotFoundException {
        return viajeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No se encontró el viaje"));
    }

    @Override
    public Viaje obtenerDetalleDeViaje(Long id) throws NotFoundException {
        Viaje viaje = viajeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No se encontró el viaje con id: " + id));


        Hibernate.initialize(viaje.getOrigen());
        Hibernate.initialize(viaje.getDestino());
        Hibernate.initialize(viaje.getVehiculo());


        Hibernate.initialize(viaje.getReservas());
        Hibernate.initialize(viaje.getParadas());


        for (Parada parada : viaje.getParadas()) {
            Hibernate.initialize(parada.getCiudad());
        }

        return viaje;
    }

    @Override
    @Transactional
    public void modificarViaje(Viaje viaje, List<Parada> paradas) throws BadRequestException {
        // 1. Obtener el viaje existente
        Viaje viajeExistente = viajeRepository.findById(viaje.getId())
                .orElseThrow(() -> new BadRequestException("El viaje no existe"));

        if (!viajeExistente.getEstado().equals(EstadoDeViaje.DISPONIBLE)) {
            throw new BadRequestException("El viaje debe estar DISPONIBLE para ser modificado.");
        }

        // 2. Actualizar campos básicos
        viajeExistente.setFechaHoraDeSalida(viaje.getFechaHoraDeSalida());
        viajeExistente.setPrecio(viaje.getPrecio());

        if (viaje.getOrigen() != null) {
            viajeExistente.setOrigen(viaje.getOrigen());
        }
        if (viaje.getDestino() != null) {
            viajeExistente.setDestino(viaje.getDestino());
        }

        if (viaje.getVehiculo() != null) {
            int asientosMaximos = viaje.getVehiculo().getAsientosTotales() - 1;
            if (viaje.getAsientosDisponibles() > asientosMaximos) {
                throw new BadRequestException("Los asientos disponibles (" + viaje.getAsientosDisponibles() + ") no pueden ser mayores a " + asientosMaximos +
                        " (total del vehículo menos el asiento del conductor)");
            }
            viajeExistente.setVehiculo(viaje.getVehiculo());
        }
        if (viaje.getAsientosDisponibles() != null) {
            viajeExistente.setAsientosDisponibles(viaje.getAsientosDisponibles());
        }

        // 3. Limpiar paradas existentes
        viajeExistente.getParadas().clear();

        // 4. Agregar nuevas paradas
        if (paradas != null) {
            for (Parada parada : paradas) {
                // Crear nueva instancia para evitar problemas de referencia
                Parada nuevaParada = new Parada();
                nuevaParada.setCiudad(parada.getCiudad());
                nuevaParada.setOrden(parada.getOrden());
                nuevaParada.setViaje(viajeExistente);  // Establecer la relación
                viajeExistente.agregarParada(nuevaParada);
            }
        }

        Hibernate.initialize(viajeExistente.getReservas());
        List<Reserva> reservasAfectadas = viajeExistente.getReservas();

        // 5. Guardar cambios
        viajeRepository.modificarViaje(viajeExistente);

        for (Reserva reserva : reservasAfectadas) {
            if (reserva.getEstado() == EstadoReserva.CONFIRMADA || reserva.getEstado() == EstadoReserva.PENDIENTE) {
                Usuario viajero = reserva.getViajero();
                String mensaje = String.format("El viaje a %s fue modificado. Revisá los cambios.",
                        viajeExistente.getDestino().getNombre());
                String url = "/viaje/detalle?id=" + viajeExistente.getId();

                try {
                    servicioNotificacion.crearYEnviar(viajero, TipoNotificacion.VIAJE_EDITADO, mensaje, url);
                } catch (Exception e) {
                    System.err.println("Fallo al notificar edición: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public Viaje obtenerViajeConParadas(Long id) throws NotFoundException {
        Viaje viaje = viajeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No se encontró el viaje"));

        // Inicializar explícitamente las relaciones necesarias
        Hibernate.initialize(viaje.getParadas());
        if (viaje.getParadas() != null) {
            for (Parada parada : viaje.getParadas()) {
                Hibernate.initialize(parada.getCiudad());
            }
        }

        return viaje;
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

        // Calcular duración estimada basada en distancia
        double distanciaKm = CalculadoraDistancia.calcularDistanciaHaversine(
            viaje.getOrigen().getLatitud(),
            viaje.getOrigen().getLongitud(),
            viaje.getDestino().getLatitud(),
            viaje.getDestino().getLongitud()
        );
        int duracionMinutos = CalculadoraDistancia.calcularDuracionEstimadaMinutos(distanciaKm);
        viaje.setDuracionEstimadaMinutos(duracionMinutos);

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
        if (usuarioEnSesion.getRol() == null || !usuarioEnSesion.getRol().equalsIgnoreCase("CONDUCTOR")) {
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
        if (!viaje.getConductor().getId().equals(usuarioEnSesion.getId())) {
            throw new UsuarioNoAutorizadoException("El viaje debe pertenecer al conductor");
        }

        //valido el estado del viaje
        if (!(viaje.getEstado() == EstadoDeViaje.DISPONIBLE || viaje.getEstado() == EstadoDeViaje.COMPLETO)) {
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

    @Override
    public List<Viaje> buscarViajesDisponibles(Ciudad origen, Ciudad destino, LocalDateTime fechaSalida, Double precioMin, Double precioMax) throws DatoObligatorioException {
        // Validate mandatory fields
        if (origen == null) {
            throw new DatoObligatorioException("El origen es obligatorio");
        }
        if (destino == null) {
            throw new DatoObligatorioException("El destino es obligatorio");
        }

        // Business rule: Define which estados are "disponibles"
        List<EstadoDeViaje> estadosPermitidos = Arrays.asList(EstadoDeViaje.DISPONIBLE, EstadoDeViaje.COMPLETO);

        // Business rule: No past trips - use current date/time if not provided
        LocalDateTime fechaDesde = (fechaSalida != null) ? fechaSalida : LocalDateTime.now();

        // Call repository with all parameters
        List<Viaje> viajes = viajeRepository.buscarViajesPorFiltros(
                origen,
                destino,
                estadosPermitidos,
                fechaDesde,
                precioMin,
                precioMax
        );

        // Initialize lazy collections for each viaje
        for (Viaje viaje : viajes) {
            Hibernate.initialize(viaje.getConductor());
            Hibernate.initialize(viaje.getVehiculo());
            Hibernate.initialize(viaje.getOrigen());
            Hibernate.initialize(viaje.getDestino());
        }

        return viajes;
    }

    @Override
    public void iniciarViaje(Long viajeId, Long conductorId)
        throws ViajeNoEncontradoException, UsuarioNoAutorizadoException, ViajeYaIniciadoException, VentanaHorariaException {

        // Obtener viaje
        Viaje viaje = viajeRepository.findById(viajeId)
            .orElseThrow(() -> new ViajeNoEncontradoException("Viaje no encontrado"));

        // Verificar que el conductor es el dueño del viaje
        if (!viaje.getConductor().getId().equals(conductorId)) {
            throw new UsuarioNoAutorizadoException("No eres el conductor de este viaje");
        }

        // Verificar que el viaje no esté ya iniciado o finalizado
        if (viaje.getEstado() == EstadoDeViaje.EN_CURSO || viaje.getEstado() == EstadoDeViaje.FINALIZADO) {
            throw new ViajeYaIniciadoException("El viaje ya fue iniciado");
        }

        // Verificar que el viaje no esté cancelado
        if (viaje.getEstado() == EstadoDeViaje.CANCELADO) {
            throw new ViajeYaIniciadoException("El viaje está cancelado");
        }

        LocalDateTime ahora = LocalDateTime.now();

        // Verificar que el viaje puede iniciarse (solo a partir de la hora programada)
        if (ahora.isBefore(viaje.getFechaHoraDeSalida())) {
            throw new VentanaHorariaException("No puedes iniciar el viaje antes de la hora programada: " +
                viaje.getFechaHoraDeSalida().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }

        // Calcular minutos de retraso (puede ser negativo si inicia antes)
        int minutosRetraso = (int) Duration.between(viaje.getFechaHoraDeSalida(), ahora).toMinutes();

        Hibernate.initialize(viaje.getReservas());
        List<Reserva> reservasAfectadas = viaje.getReservas();
        // Iniciar viaje
        viaje.setEstado(EstadoDeViaje.EN_CURSO);
        viaje.setFechaHoraInicioReal(ahora);
        viaje.setMinutosDeRetraso(minutosRetraso);
        viajeRepository.modificarViaje(viaje);
        for (Reserva reserva : reservasAfectadas) {
            if (reserva.getEstado() == EstadoReserva.CONFIRMADA) {
                Usuario viajero = reserva.getViajero();
                String mensaje = String.format("¡Tu viaje a %s ha comenzado!", viaje.getDestino().getNombre());
                String url = "/reserva/misViajes";

                try {
                    servicioNotificacion.crearYEnviar(viajero, TipoNotificacion.VIAJE_INICIADO, mensaje, url);
                } catch (Exception e) {
                    //DEJAMOS ESTO ASI PARA DEBBUGEAR
                    System.err.println("Fallo al notificar inicio al viajero: " + viajero.getId());
                }
            }
        }
    }

    @Override
    public void finalizarViaje(Long viajeId, Long conductorId)
        throws ViajeNoEncontradoException, UsuarioNoAutorizadoException, ViajeYaFinalizadoException {

        // Obtener viaje
        Viaje viaje = viajeRepository.findById(viajeId)
            .orElseThrow(() -> new ViajeNoEncontradoException("Viaje no encontrado"));

        // Verificar que el conductor es el dueño
        if (!viaje.getConductor().getId().equals(conductorId)) {
            throw new UsuarioNoAutorizadoException("No eres el conductor de este viaje");
        }

        // Verificar que esté en curso
        if (viaje.getEstado() != EstadoDeViaje.EN_CURSO) {
            throw new ViajeYaFinalizadoException("El viaje no está en curso");
        }

        Hibernate.initialize(viaje.getReservas());
        List<Reserva> reservasAfectadas = viaje.getReservas();

        // Finalizar viaje
        viaje.setEstado(EstadoDeViaje.FINALIZADO);
        viaje.setFechaHoraFinReal(LocalDateTime.now());
        viaje.setCierreAutomatico(false);
        viajeRepository.modificarViaje(viaje);
        for (Reserva reserva : reservasAfectadas) {
            if (reserva.getEstado() == EstadoReserva.CONFIRMADA) {
                Usuario viajero = reserva.getViajero();
                String mensaje = String.format("El viaje a %s finalizó. ¡Dejanos tu valoración al conductor!", viaje.getDestino().getNombre());
                String url = "/reserva/misViajes"; // El botón de valoración aparecerá aquí

                try {
                    servicioNotificacion.crearYEnviar(viajero, TipoNotificacion.VALORACION_PENDIENTE, mensaje, url);
                } catch (Exception e) {
                    System.err.println("Fallo al notificar valoración pendiente: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void cerrarViajesOlvidados() {
        LocalDateTime ahora = LocalDateTime.now();

        // Buscar viajes en curso que excedieron duracionEstimada + 2 horas
        List<Viaje> viajesOlvidados = viajeRepository.findViajesEnCursoExcedidos(
            ahora.minusHours(2)
        );

        for (Viaje viaje : viajesOlvidados) {
            // Verificar si realmente excedió el tiempo
            if (viaje.getFechaHoraInicioReal() != null && viaje.getDuracionEstimadaMinutos() != null) {
                LocalDateTime finEstimado = viaje.getFechaHoraInicioReal()
                    .plusMinutes(viaje.getDuracionEstimadaMinutos())
                    .plusHours(2);

                if (ahora.isAfter(finEstimado)) {
                    // Cerrar automáticamente (sin penalización)
                    viaje.setEstado(EstadoDeViaje.FINALIZADO);
                    viaje.setFechaHoraFinReal(finEstimado);
                    viaje.setCierreAutomatico(true);
                    viajeRepository.modificarViaje(viaje);
                }
            }
        }
    }

    @Override
    public void iniciarViajesAtrasados() {
        LocalDateTime ahora = LocalDateTime.now();

        // Buscar viajes que debieron iniciar hace más de 15 minutos
        List<Viaje> viajesAtrasados = viajeRepository.findViajesNoIniciadosFueraDePlazo(
            ahora.minusMinutes(15)
        );

        for (Viaje viaje : viajesAtrasados) {
            try {
                // Intentar iniciar el viaje automáticamente
                iniciarViaje(viaje.getId(), viaje.getConductor().getId());
            } catch (Exception e) {
                // Si falla el inicio (ej: ya fue cancelado), continuar con el siguiente
                // Log error but don't stop processing other trips
                System.err.println("Error al iniciar viaje " + viaje.getId() + ": " + e.getMessage());
            }
        }
    }

    private void cancelarTodasLasReservas(Viaje viaje) {
        Hibernate.initialize(viaje.getReservas());
        for (Reserva reserva : viaje.getReservas()) {
            if (reserva.getEstado() == EstadoReserva.CONFIRMADA) {
                // Cambiar a rechazada cuando el viaje se cancela por el sistema
                reserva.setEstado(EstadoReserva.RECHAZADA);
                reserva.setMotivoRechazo("Viaje cancelado automáticamente por el sistema");
                reservaRepository.update(reserva);
            }
        }
    }

    @Override
    public void cancelarViajeConReservasPagadas(Long id, Usuario usuarioEnSesion)
        throws ViajeNoEncontradoException, UsuarioNoAutorizadoException, ViajeNoCancelableException {
    
    // Valida que el usuario sea conductor
    if (usuarioEnSesion.getRol() == null || !usuarioEnSesion.getRol().equalsIgnoreCase("CONDUCTOR")) {
        throw new UsuarioNoAutorizadoException("Solo los conductores pueden cancelar viajes.");
    }

    // Buscar viaje
    Optional<Viaje> viajeOpt = viajeRepository.findById(id);
    if (viajeOpt.isEmpty()) {
        throw new ViajeNoEncontradoException("No se encontró un viaje con ese ID.");
    }

    Viaje viaje = viajeOpt.get();

    // valida que el viaje pertenezca al conductor
    if (!viaje.getConductor().getId().equals(usuarioEnSesion.getId())) {
        throw new UsuarioNoAutorizadoException("El viaje debe pertenecer al conductor.");
    }

    if (!(viaje.getEstado() == EstadoDeViaje.DISPONIBLE || viaje.getEstado() == EstadoDeViaje.COMPLETO)) {
        throw new ViajeNoCancelableException("El viaje no puede cancelarse en este estado.");
    }

    // obitnen las reservas asociadas
    List<Reserva> reservas = reservaRepository.findByViaje(viaje);

    for (Reserva reserva : reservas) {
        // Solo cancelar si está CONFIRMADA o PENDIENTE (y pagada o no)
        if (reserva.getEstado() == EstadoReserva.CONFIRMADA || reserva.getEstado() == EstadoReserva.PENDIENTE) {
            EstadoReserva estadoAnterior = reserva.getEstado();
            reserva.setEstado(EstadoReserva.CANCELADA_POR_CONDUCTOR);

            // Si estaba pagada, mantenemos el estado de pago o lo marcamos como “reembolso pendiente”
            if (reserva.getEstadoPago() == EstadoPago.PAGADO) {
                reserva.setEstadoPago(EstadoPago.REEMBOLSO_PENDIENTE);
            }

            // guardar cambios
            reservaRepository.update(reserva);

            // registrar historial
            HistorialReserva historial = new HistorialReserva();
            historial.setReserva(reserva);
            historial.setViaje(viaje);
            historial.setViajero(reserva.getViajero());
            historial.setConductor(usuarioEnSesion);
            historial.setFechaEvento(LocalDateTime.now());
            historial.setEstadoAnterior(estadoAnterior);
            historial.setEstadoNuevo(reserva.getEstado());
            repositorioHistorialReserva.save(historial);

            // Enviar notificación al viajero
            try {
                String mensaje = String.format(
                        "El viaje a %s ha sido CANCELADO por el conductor. Tu reserva fue cancelada%s.",
                        viaje.getDestino().getNombre(),
                        (reserva.getEstadoPago() == EstadoPago.REEMBOLSO_PENDIENTE)
                                ? " y el reembolso será procesado próximamente" : ""
                );

                String url = "/reserva/misReservasActivas";
                servicioNotificacion.crearYEnviar(reserva.getViajero(), TipoNotificacion.VIAJE_CANCELADO, mensaje, url);
            } catch (Exception e) {
                System.err.println("Error al enviar notificación de cancelación: " + e.getMessage());
            }
        }
    }

    //cancela el viaje
    viaje.setEstado(EstadoDeViaje.CANCELADO);
    viajeRepository.modificarViaje(viaje);
}

}