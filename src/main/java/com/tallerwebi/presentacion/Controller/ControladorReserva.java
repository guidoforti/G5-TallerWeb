package com.tallerwebi.presentacion.Controller;

import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Reserva;
import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.Enums.EstadoDeViaje;
import com.tallerwebi.dominio.Enums.EstadoPago;
import com.tallerwebi.dominio.Enums.EstadoReserva;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import com.tallerwebi.dominio.IServicio.ServicioReserva;
import com.tallerwebi.dominio.IServicio.ServicioViaje;
import com.tallerwebi.dominio.IServicio.ServicioViajero;
import com.tallerwebi.dominio.IServicio.ServicioValoracion;
import com.tallerwebi.dominio.IServicio.ServicioNotificacion;
import com.tallerwebi.dominio.excepcion.*;
import com.tallerwebi.presentacion.DTO.InputsDTO.MarcarAsistenciaInputDTO;
import com.tallerwebi.presentacion.DTO.InputsDTO.RechazoReservaInputDTO;
import com.tallerwebi.presentacion.DTO.InputsDTO.SolicitudReservaInputDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ReservaActivaDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ReservaVistaDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ViajeConfirmadoViajeroDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ViajeReservaSolicitudDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ViajeroConfirmadoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reserva")
public class ControladorReserva {

    private final ServicioReserva servicioReserva;
    private final ServicioViaje servicioViaje;
    private final ServicioViajero servicioViajero;
    private final ServicioConductor servicioConductor;
    private final ServicioValoracion servicioValoracion;
    private final ServicioNotificacion servicioNotificacion;

    @Autowired
    public ControladorReserva(ServicioReserva servicioReserva,
                              ServicioViaje servicioViaje,
                              ServicioViajero servicioViajero,
                              ServicioConductor servicioConductor,
                              ServicioValoracion servicioValoracion,
                              ServicioNotificacion servicioNotificacion) {
        this.servicioReserva = servicioReserva;
        this.servicioViaje = servicioViaje;
        this.servicioViajero = servicioViajero;
        this.servicioConductor = servicioConductor;
        this.servicioValoracion = servicioValoracion;
        this.servicioNotificacion = servicioNotificacion;
    }

    /**
     * Muestra el formulario de confirmación para solicitar una reserva
     * GET /reserva/solicitar?viajeId={id}
     */
    @GetMapping("/solicitar")
    public ModelAndView mostrarFormularioSolicitud(@RequestParam("viajeId") Long viajeId,
                                                   HttpSession session,
                                                   RedirectAttributes redirectAttributes) {
        ModelMap model = new ModelMap();

        // Validar sesión
        Object usuarioId = session.getAttribute("idUsuario");
        String rol = (String) session.getAttribute("ROL");
        if (usuarioId == null) {
            return new ModelAndView("redirect:/login");
        }

        try {
            // Obtener detalles del viaje
            Viaje viaje = servicioViaje.obtenerViajePorId(viajeId);

            // Preparar el DTO con los IDs
            SolicitudReservaInputDTO solicitudDTO = new SolicitudReservaInputDTO();
            solicitudDTO.setViajeId(viajeId);
            solicitudDTO.setViajeroId((Long) usuarioId);

            // Crear un DTO para pasar fechas formateadas y otros datos al template
            ViajeReservaSolicitudDTO viajeDTO = new ViajeReservaSolicitudDTO(viaje);

            model.put("idUsuario", usuarioId);
            model.put("ROL", rol);
            model.put("viaje", viajeDTO);
            model.put("solicitud", solicitudDTO);
            return new ModelAndView("solicitarReserva", model);

        } catch (NotFoundException | ViajeNoEncontradoException | UsuarioNoAutorizadoException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return new ModelAndView("redirect:/viaje/buscar");
        }
    }

    /**
     * Procesa la solicitud de reserva
     * POST /reserva/solicitar
     */
    @PostMapping("/solicitar")
    public ModelAndView solicitarReserva(@ModelAttribute("solicitud") SolicitudReservaInputDTO solicitudDTO,
                                         HttpSession session) {
        ModelMap model = new ModelMap();

        // Validar sesión
        Object usuarioId = session.getAttribute("idUsuario");
        if (usuarioId == null) {
            return new ModelAndView("redirect:/login");
        }

        try {
            // Obtener entidades
            Viaje viaje = servicioViaje.obtenerViajePorId(solicitudDTO.getViajeId());
            Viajero viajero = servicioViajero.obtenerViajero(solicitudDTO.getViajeroId());

            // Solicitar reserva
            Reserva reserva = servicioReserva.solicitarReserva(viaje, viajero);

            model.put("mensaje", "Reserva solicitada exitosamente. Está en estado PENDIENTE y debe ser aprobada por el conductor.");
            model.put("reserva", new ReservaVistaDTO(reserva));
            return new ModelAndView("reservaExitosa", model);

        } catch (ReservaYaExisteException e) {
            model.put("error", "Ya tienes una reserva para este viaje");
        } catch (SinAsientosDisponiblesException e) {
            model.put("error", "No hay asientos disponibles para este viaje");
        } catch (ViajeYaIniciadoException e) {
            model.put("error", "El viaje ya ha iniciado, no se pueden solicitar reservas");
        } catch (DatoObligatorioException | NotFoundException | UsuarioInexistente | ViajeNoEncontradoException |
                 UsuarioNoAutorizadoException e) {
            model.put("error", e.getMessage());
        }

        // Si hubo error, volver a mostrar el formulario con el error
        try {
            Viaje viaje = servicioViaje.obtenerViajePorId(solicitudDTO.getViajeId());
            ViajeReservaSolicitudDTO viajeDTO = new ViajeReservaSolicitudDTO(viaje);
            model.put("viaje", viajeDTO);
            model.put("solicitud", solicitudDTO);
        } catch (Exception ex) {
            // Si no se puede obtener el viaje, redirigir a buscar
            return new ModelAndView("redirect:/viaje/buscar");
        }

        return new ModelAndView("solicitarReserva", model);
    }

    /**
     * Lista las reservas de un viaje específico (para el conductor)
     * GET /reserva/listar?viajeId={id}
     */
    @GetMapping("/listar")
    public ModelAndView listarReservasDeViaje(@RequestParam("viajeId") Long viajeId,
                                              HttpSession session,
                                              RedirectAttributes redirectAttributes) {
        ModelMap model = new ModelMap();

        // Validar sesión
        Object usuarioId = session.getAttribute("idUsuario");
        if (usuarioId == null) {
            return new ModelAndView("redirect:/login");
        }

        try {
            // Obtener el viaje
            Viaje viaje = servicioViaje.obtenerViajePorId(viajeId);

            // Obtener todas las reservas del viaje
            List<Reserva> reservas = servicioReserva.listarReservasPorViaje(viaje);

            // Convertir a DTOs
            List<ReservaVistaDTO> reservasDTO = reservas.stream()
                    .map(ReservaVistaDTO::new)
                    .collect(Collectors.toList());

            model.put("viaje", viaje);
            model.put("reservas", reservasDTO);
            return new ModelAndView("misReservas", model);

        } catch (NotFoundException | ViajeNoEncontradoException | UsuarioNoAutorizadoException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return new ModelAndView("redirect:/viaje/buscar");
        }
    }

    /**
     * Lista las reservas del viajero logueado
     * GET /reserva/mis-reservas
     */
    @GetMapping("/misReservas")
    public ModelAndView listarMisReservas(HttpSession session) {
        ModelMap model = new ModelMap();
        Object usuarioIdObj = session.getAttribute("idUsuario");
        Object rol = session.getAttribute("ROL");

        if (usuarioIdObj == null || !"CONDUCTOR".equals(rol)) {
            return new ModelAndView("redirect:/login", model);
        }

        try {
            Conductor conductor = servicioConductor.obtenerConductor((Long) usuarioIdObj);
            List<Viaje> viajes = servicioViaje.listarViajesPorConductor(conductor);
            model.put("contadorNotificaciones", servicioNotificacion.contarNoLeidas(conductor.getId()));
            // Obtener todas las reservas de los viajes
            List<ReservaVistaDTO> reservasDTO = new ArrayList<>();
            for (Viaje viaje : viajes) {
                List<Reserva> reservasDelViaje = servicioReserva.listarReservasPorViaje(viaje);
                reservasDTO.addAll(
                        reservasDelViaje.stream()
                                .map(ReservaVistaDTO::new)
                                .collect(Collectors.toList())
                );
            }

            model.put("reservas", reservasDTO);
            return new ModelAndView("misReservas", model);

        } catch (UsuarioInexistente | UsuarioNoAutorizadoException e) {
            model.put("error", e.getMessage());
            return new ModelAndView("error", model);
        }
    }

    /**
     * Confirma una reserva pendiente
     * POST /reserva/confirmar?reservaId={id}
     */
    @PostMapping("/confirmar")
    public ModelAndView confirmarReserva(@RequestParam("reservaId") Long reservaId,
                                         HttpSession session) {
        ModelMap model = new ModelMap();

        // Validar sesión
        Object usuarioIdObj = session.getAttribute("idUsuario");
        Object rol = session.getAttribute("ROL");

        if (usuarioIdObj == null || !"CONDUCTOR".equals(rol)) {
            return new ModelAndView("redirect:/login");
        }

        Long conductorId = (Long) usuarioIdObj;

        try {
            servicioReserva.confirmarReserva(reservaId, conductorId);
            model.put("mensaje", "Reserva confirmada exitosamente");
        } catch (NotFoundException e) {
            model.put("error", "No se encontró la reserva");
        } catch (UsuarioNoAutorizadoException e) {
            model.put("error", "No tienes permiso");
        } catch (SinAsientosDisponiblesException e) {
            model.put("error", "No hay asientos disponibles");
        } catch (ReservaYaExisteException | ViajeNoEncontradoException e) {
            model.put("error", e.getMessage());
        }

        // Recargar la vista con los datos actualizados
        try {
            Conductor conductor = servicioConductor.obtenerConductor(conductorId);
            List<Viaje> viajes = servicioViaje.listarViajesPorConductor(conductor);

            // Obtener todas las reservas de los viajes
            List<ReservaVistaDTO> reservasDTO = new ArrayList<>();
            for (Viaje viaje : viajes) {
                List<Reserva> reservasDelViaje = servicioReserva.listarReservasPorViaje(viaje);
                reservasDTO.addAll(
                        reservasDelViaje.stream()
                                .map(ReservaVistaDTO::new)
                                .collect(Collectors.toList())
                );
            }

            model.put("reservas", reservasDTO);
            return new ModelAndView("misReservas", model);

        } catch (UsuarioInexistente | UsuarioNoAutorizadoException e) {
            model.put("error", "Error al recargar datos: " + e.getMessage());
            return new ModelAndView("redirect:/reserva/misReservas");
        }
    }

    /**
     * Muestra el formulario para rechazar una reserva
     * GET /reserva/rechazar?reservaId={id}
     */
    @GetMapping("/rechazar")
    public ModelAndView mostrarFormularioRechazo(@RequestParam("reservaId") Long reservaId,
                                                 HttpSession session) {
        ModelMap model = new ModelMap();

        // Validar sesión
        Object usuarioId = session.getAttribute("idUsuario");
        if (usuarioId == null) {
            return new ModelAndView("redirect:/login");
        }

        // Preparar el DTO con el ID de la reserva
        RechazoReservaInputDTO rechazoDTO = new RechazoReservaInputDTO();
        rechazoDTO.setReservaId(reservaId);

        model.put("rechazoDTO", rechazoDTO);
        return new ModelAndView("rechazarReserva", model);
    }

    /**
     * Procesa el rechazo de una reserva
     * POST /reserva/rechazar
     */
    @PostMapping("/rechazar")
    public ModelAndView rechazarReserva(@ModelAttribute("rechazoDTO") RechazoReservaInputDTO rechazoDTO,
                                        HttpSession session) {
        ModelMap model = new ModelMap();

        // Validar sesión
        Object usuarioIdObj = session.getAttribute("idUsuario");
        Object rol = session.getAttribute("ROL");

        if (usuarioIdObj == null || !"CONDUCTOR".equals(rol)) {
            return new ModelAndView("redirect:/login");
        }

        Long conductorId = (Long) usuarioIdObj;

        try {
            servicioReserva.rechazarReserva(rechazoDTO.getReservaId(), conductorId, rechazoDTO.getMotivo());
            model.put("mensaje", "Reserva rechazada exitosamente");

        } catch (DatoObligatorioException e) {
            model.put("error", "El motivo del rechazo es obligatorio");
            model.put("rechazoDTO", rechazoDTO);
            return new ModelAndView("rechazarReserva", model);
        } catch (NotFoundException e) {
            model.put("error", "No se encontró la reserva");
            model.put("rechazoDTO", rechazoDTO);
            return new ModelAndView("rechazarReserva", model);
        } catch (UsuarioNoAutorizadoException e) {
            model.put("error", "No tienes permiso");
            model.put("rechazoDTO", rechazoDTO);
            return new ModelAndView("rechazarReserva", model);
        } catch (ReservaYaExisteException e) {
            model.put("error", e.getMessage());
            model.put("rechazoDTO", rechazoDTO);
            return new ModelAndView("rechazarReserva", model);
        }

        // Si fue exitoso, recargar la vista misReservas con los datos actualizados
        try {
            Conductor conductor = servicioConductor.obtenerConductor(conductorId);
            List<Viaje> viajes = servicioViaje.listarViajesPorConductor(conductor);

            // Obtener todas las reservas de los viajes
            List<ReservaVistaDTO> reservasDTO = new ArrayList<>();
            for (Viaje viaje : viajes) {
                List<Reserva> reservasDelViaje = servicioReserva.listarReservasPorViaje(viaje);
                reservasDTO.addAll(
                        reservasDelViaje.stream()
                                .map(ReservaVistaDTO::new)
                                .collect(Collectors.toList())
                );
            }

            model.put("reservas", reservasDTO);
            return new ModelAndView("misReservas", model);

        } catch (UsuarioInexistente | UsuarioNoAutorizadoException e) {
            model.put("error", "Error al recargar datos: " + e.getMessage());
            return new ModelAndView("redirect:/reserva/misReservas");
        }
    }

    /**
     * Lista los viajeros confirmados de un viaje
     * GET /reserva/viajerosConfirmados?viajeId={id}
     */
    @GetMapping("/viajerosConfirmados")
    public ModelAndView listarViajerosConfirmados(@RequestParam("viajeId") Long viajeId,
                                                  HttpSession session,
                                                  RedirectAttributes redirectAttributes) {
        ModelMap model = new ModelMap();

        // Validar sesión
        Object conductorIdObj = session.getAttribute("idUsuario");
        Object rol = session.getAttribute("ROL");

        if (conductorIdObj == null || !"CONDUCTOR".equals(rol)) {
            return new ModelAndView("redirect:/login");
        }

        Long conductorId = (Long) conductorIdObj;
        model.put("contadorNotificaciones", servicioNotificacion.contarNoLeidas(conductorId));
        try {
            // Obtener el viaje para mostrar información en la vista
            Viaje viaje = servicioViaje.obtenerViajePorId(viajeId);

            // Obtener reservas confirmadas
            List<Reserva> reservasConfirmadas = servicioReserva.listarViajerosConfirmados(viajeId, conductorId);

            // Convertir a DTOs
            List<ViajeroConfirmadoDTO> viajerosDTO = reservasConfirmadas.stream()
                    .map(ViajeroConfirmadoDTO::new)
                    .collect(Collectors.toList());

            // Agregar información del viaje al modelo (formateada para la vista)
            model.put("viajeId", viaje.getId());
            model.put("origenNombre", viaje.getOrigen() != null ? viaje.getOrigen().getNombre() : "N/A");
            model.put("destinoNombre", viaje.getDestino() != null ? viaje.getDestino().getNombre() : "N/A");
            model.put("fechaSalida", viaje.getFechaHoraDeSalida() != null
                    ? viaje.getFechaHoraDeSalida().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    : "N/A");
            model.put("precio", viaje.getPrecio() != null ? viaje.getPrecio() : 0.0);
            model.put("viajeros", viajerosDTO);
            return new ModelAndView("viajerosConfirmados", model);

        } catch (ViajeNoEncontradoException | NotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "No se encontró el viaje");
            return new ModelAndView("redirect:/viaje/listar");
        } catch (UsuarioNoAutorizadoException e) {
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para ver esta información");
            return new ModelAndView("redirect:/viaje/listar");
        }
    }

    /**
     * Marca la asistencia de un viajero
     * POST /reserva/marcarAsistencia
     */
    @PostMapping("/marcarAsistencia")
    public ModelAndView marcarAsistencia(@ModelAttribute MarcarAsistenciaInputDTO inputDTO,
                                         HttpSession session) {
        ModelMap model = new ModelMap();

        // Validar sesión
        Object conductorIdObj = session.getAttribute("idUsuario");
        if (conductorIdObj == null) {
            return new ModelAndView("redirect:/login");
        }

        Long conductorId = (Long) conductorIdObj;
        Long viajeId = null;

        try {
            // Primero obtenemos el viajeId antes de intentar marcar asistencia
            Reserva reserva = servicioReserva.obtenerReservaPorId(inputDTO.getReservaId());
            viajeId = reserva.getViaje().getId();

            // Intentar marcar asistencia
            servicioReserva.marcarAsistencia(inputDTO.getReservaId(), conductorId, inputDTO.getAsistencia());
            model.put("mensaje", "Asistencia marcada exitosamente");

        } catch (NotFoundException e) {
            model.put("error", "No se encontró la reserva");
        } catch (UsuarioNoAutorizadoException e) {
            model.put("error", "No tienes permiso");
        } catch (ReservaYaExisteException e) {
            model.put("error", "Solo se puede marcar asistencia en reservas confirmadas");
        } catch (AccionNoPermitidaException e) {
            model.put("error", e.getMessage());
        } catch (DatoObligatorioException e) {
            model.put("error", "Valor de asistencia inválido");
        }

        // Si no pudimos obtener el viajeId, redirigir a misReservas
        if (viajeId == null) {
            return new ModelAndView("redirect:/reserva/misReservas");
        }

        // Recargar la vista con los datos actualizados
        try {
            Viaje viaje = servicioViaje.obtenerViajePorId(viajeId);
            List<Reserva> reservasConfirmadas = servicioReserva.listarViajerosConfirmados(viajeId, conductorId);

            // Convertir a DTOs
            List<ViajeroConfirmadoDTO> viajerosDTO = reservasConfirmadas.stream()
                    .map(ViajeroConfirmadoDTO::new)
                    .collect(Collectors.toList());

            // Agregar información del viaje al modelo (formateada para la vista)
            model.put("viajeId", viaje.getId());
            model.put("origenNombre", viaje.getOrigen() != null ? viaje.getOrigen().getNombre() : "N/A");
            model.put("destinoNombre", viaje.getDestino() != null ? viaje.getDestino().getNombre() : "N/A");
            model.put("fechaSalida", viaje.getFechaHoraDeSalida() != null
                    ? viaje.getFechaHoraDeSalida().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    : "N/A");
            model.put("precio", viaje.getPrecio() != null ? viaje.getPrecio() : 0.0);
            model.put("viajeros", viajerosDTO);

            return new ModelAndView("viajerosConfirmados", model);

        } catch (ViajeNoEncontradoException | NotFoundException | UsuarioNoAutorizadoException e) {
            model.put("error", "Error al recargar datos: " + e.getMessage());
            return new ModelAndView("redirect:/reserva/misReservas");
        }
    }

    /**
     * Lista las reservas pendientes y rechazadas del viajero logueado
     * GET /reserva/misReservasPendientes
     */
    @GetMapping("/misReservasActivas")
    public ModelAndView listarReservasActivas(HttpSession session) {
        ModelMap model = new ModelMap();

        // Validar sesión
        Object usuarioIdObj = session.getAttribute("idUsuario");
        Object rol = session.getAttribute("ROL");

        if (usuarioIdObj == null || !"VIAJERO".equals(rol)) {
            return new ModelAndView("redirect:/login");
        }

        Long viajeroId = (Long) usuarioIdObj;
        model.put("idUsuario", viajeroId);
        model.put("ROL", rol);
        model.put("contadorNotificaciones", servicioNotificacion.contarNoLeidas(viajeroId));
        try {
            // Obtener todas las reservas pendientes y rechazadas del viajero
            List<Reserva> reservas = servicioReserva.listarReservasActivasPorViajero(viajeroId);

            // Separar en dos listas: pendientes y rechazadas
            List<Reserva> reservasPendientes = reservas.stream()
                    .filter(r -> r.getEstado() == EstadoReserva.PENDIENTE)
                    .collect(Collectors.toList());

            List<Reserva> reservasRechazadas = reservas.stream()
                    .filter(r -> r.getEstado() == EstadoReserva.RECHAZADA)
                    .collect(Collectors.toList());

            List<Reserva> reservasConfirmadasPendientesPago = reservas.stream()
                    .filter(r -> r.getEstado() == EstadoReserva.CONFIRMADA)
                    .collect(Collectors.toList());
            List<Reserva> reservasPagadas = servicioReserva.listarViajesConfirmadosPorViajero(viajeroId).stream()
                    .filter(r -> r.getEstadoPago() == EstadoPago.PAGADO)
                    .collect(Collectors.toList());
            // Convertir a DTOs
            List<ReservaActivaDTO> pendientesDTO = reservasPendientes.stream()
                    .map(ReservaActivaDTO::new)
                    .collect(Collectors.toList());

            List<ReservaActivaDTO> rechazadasDTO = reservasRechazadas.stream()
                    .map(ReservaActivaDTO::new)
                    .collect(Collectors.toList());

            List<ReservaActivaDTO> confirmadasDTO = reservasConfirmadasPendientesPago.stream()
                    .map(ReservaActivaDTO::new)
                    .collect(Collectors.toList());
            List<ReservaActivaDTO> pagadasDTO = reservasPagadas.stream()
                    .map(ReservaActivaDTO::new)
                    .collect(Collectors.toList());

            model.put("reservasPendientes", pendientesDTO);
            model.put("reservasRechazadas", rechazadasDTO);
            model.put("reservasConfirmadas", confirmadasDTO);
            model.put("reservasPagadas", pagadasDTO);

            return new ModelAndView("misReservasActivas", model);

        } catch (UsuarioInexistente e) {
            model.put("error", e.getMessage());
            return new ModelAndView("error", model);
        }
    }

    /**
     * Lista todos los viajes confirmados del viajero logueado
     * organizados por su estado temporal (Próximos, En curso, Finalizados)
     * GET /reserva/misViajes
     */
    @GetMapping("/misViajes")
    public ModelAndView listarMisViajes(HttpSession session) {
        ModelMap model = new ModelMap();

        // Validar sesión
        Object usuarioIdObj = session.getAttribute("idUsuario");
        Object rol = session.getAttribute("ROL");

        if (usuarioIdObj == null || !"VIAJERO".equals(rol)) {
            return new ModelAndView("redirect:/login");
        }

        Long viajeroId = (Long) usuarioIdObj;
        model.put("idUsuario", viajeroId);
        model.put("ROL", rol);
        model.put("contadorNotificaciones", servicioNotificacion.contarNoLeidas(viajeroId));
        try {
            // Obtener todas las reservas confirmadas del viajero
            List<Reserva> reservas = servicioReserva.listarViajesConfirmadosPorViajero(viajeroId);

            // Obtengo las reservas que fueron canceladas
            List<Reserva> reservasCanceladas = servicioReserva.listarViajesCanceladosPorViajero(viajeroId);

            // Categorizar por estado del viaje: (SIN CAMBIOS)
            List<Reserva> viajesProximos = reservas.stream()
                    .filter(r -> r.getViaje().getEstado() == EstadoDeViaje.DISPONIBLE ||
                            r.getViaje().getEstado() == EstadoDeViaje.COMPLETO)
                    .sorted((r1, r2) -> r1.getViaje().getFechaHoraDeSalida().compareTo(r2.getViaje().getFechaHoraDeSalida()))
                    .collect(Collectors.toList());

            List<Reserva> viajesEnCurso = reservas.stream()
                    .filter(r -> r.getViaje().getEstado() == EstadoDeViaje.EN_CURSO)
                    .sorted((r1, r2) -> r1.getViaje().getFechaHoraDeSalida().compareTo(r2.getViaje().getFechaHoraDeSalida()))
                    .collect(Collectors.toList());

            List<Reserva> viajesFinalizados = reservas.stream()
                    .filter(r -> r.getViaje().getEstado() == EstadoDeViaje.FINALIZADO)
                    .sorted((r1, r2) -> r2.getViaje().getFechaHoraDeSalida().compareTo(r1.getViaje().getFechaHoraDeSalida()))
                    .collect(Collectors.toList());

            List<Reserva> viajesCancelados = reservasCanceladas.stream()
                    .sorted((r1, r2) -> r2.getViaje().getFechaHoraDeSalida().compareTo(r1.getViaje().getFechaHoraDeSalida()))
                    .collect(Collectors.toList());

            List<ViajeConfirmadoViajeroDTO> proximosDTO = viajesProximos.stream()
                    .map(ViajeConfirmadoViajeroDTO::new)
                    .collect(Collectors.toList());

            List<ViajeConfirmadoViajeroDTO> enCursoDTO = viajesEnCurso.stream()
                    .map(ViajeConfirmadoViajeroDTO::new)
                    .collect(Collectors.toList());

            List<ViajeConfirmadoViajeroDTO> finalizadosDTO = viajesFinalizados.stream()
                    .map(reserva -> {
                        ViajeConfirmadoViajeroDTO dto = new ViajeConfirmadoViajeroDTO(reserva);

                        if (reserva.getViaje().getEstado() == EstadoDeViaje.FINALIZADO) {

                            Long conductorId = reserva.getViaje().getConductor().getId();
                            Long viajeId = reserva.getViaje().getId();

                            boolean yaExiste = servicioValoracion.yaHaValorado(viajeroId, conductorId, viajeId);

                            dto.setValoracionPendiente(!yaExiste);
                        }

                        return dto;
                    })
                    .collect(Collectors.toList());


            List<ViajeConfirmadoViajeroDTO> canceladosDTO = viajesCancelados.stream()
                    .map(ViajeConfirmadoViajeroDTO::new)
                    .collect(Collectors.toList());

            model.put("viajesProximos", proximosDTO);
            model.put("viajesEnCurso", enCursoDTO);
            model.put("viajesFinalizados", finalizadosDTO);
            model.put("viajesCancelados", canceladosDTO);

            return new ModelAndView("misViajes", model);

        } catch (UsuarioInexistente e) {
            model.put("error", e.getMessage());
            return new ModelAndView("error", model);
        }
    }


    @GetMapping("/pagar")
    public ModelAndView pagarReservar (HttpSession session, @RequestParam Long reservaId , RedirectAttributes redirectAttributes) throws UsuarioInexistente {
        ModelMap model = new ModelMap();
        System.out.println("\n--- [DEBUG] INICIO: GET /reserva/pagar ---");
        System.out.println("[DEBUG] Recibido reservaId: " + reservaId);
        // Validar sesión
        Object usuarioIdObj = session.getAttribute("idUsuario");
        Object rol = session.getAttribute("ROL");

        if (usuarioIdObj == null || !"VIAJERO".equals(rol)) {
            return new ModelAndView("redirect:/login");
        }
        try {
            System.out.println("[DEBUG] TRY: Llamando a servicioReserva.crearPreferenciaDePago...");
            Preference preferenciaDePago  = servicioReserva.crearPreferenciaDePago(reservaId, (Long) usuarioIdObj);
            System.out.println("[DEBUG] ÉXITO: Preferencia creada. Init Point: " + preferenciaDePago.getInitPoint());
            System.out.println("--- [DEBUG] FIN: GET /reserva/pagar (Redirigiendo a MP) ---");
            return new ModelAndView("redirect:" + preferenciaDePago.getInitPoint());
        } catch (MPApiException e) { // <-- ¡Catch específico para errores de API!

            System.err.println("************************************************************");
            System.err.println("[DEBUG] CATCH: ¡FALLÓ! MPApiException (Error de la API de MP)");
            System.err.println("STATUS CODE: " + e.getStatusCode());
            // ESTA LÍNEA ES LA CLAVE. NOS DIRÁ EL ERROR REAL:
            System.err.println("API RESPONSE: " + e.getApiResponse().getContent());
            System.err.println("************************************************************");

            // Le pasamos el error real al usuario para que lo veas en la pantalla
            redirectAttributes.addFlashAttribute("error", "Error de MP: " + e.getApiResponse().getContent());
            return new ModelAndView("redirect:/reserva/misReservasActivas");

        } catch (MPException e) { // <-- Catch para errores del SDK

            System.err.println("************************************************************");
            System.err.println("[DEBUG] CATCH: ¡FALLÓ! MPException (Error del SDK de MP)");
            System.err.println("MENSAJE: " + e.getMessage());
            System.err.println("************************************************************");

            redirectAttributes.addFlashAttribute("error", "Error del SDK de MP: " + e.getMessage());
            return new ModelAndView("redirect:/reserva/misReservasActivas");

        } catch (NotFoundException | UsuarioNoAutorizadoException | AccionNoPermitidaException e) { // Tus excepciones de negocio

            System.err.println("************************************************************");
            System.err.println("[DEBUG] CATCH: ¡FALLÓ! Excepción de negocio: " + e.getMessage());
            System.err.println("************************************************************");

            redirectAttributes.addFlashAttribute("error", "Error al iniciar el pago: " + e.getMessage());
            return new ModelAndView("redirect:/reserva/misReservasActivas");

        } catch (Exception e) { // Catch genérico

            System.err.println("************************************************************");
            System.err.println("[DEBUG] CATCH: ¡FALLÓ! Excepción genérica: " + e.getMessage());
            e.printStackTrace(); // Imprimir el stack trace completo
            System.err.println("************************************************************");

            redirectAttributes.addFlashAttribute("error", "Ocurrió un error inesperado. Intente más tarde.");
            return new ModelAndView("redirect:/reserva/misReservasActivas");
        }
    }

    @GetMapping("/pago/exitoso")
    public ModelAndView devolverPagoExitoso(HttpSession session, @RequestParam Long reservaId, RedirectAttributes redirectAttributes) {
        ModelMap model = new ModelMap();
        // Validar sesión
        Object usuarioIdObj = session.getAttribute("idUsuario");
        Object rol = session.getAttribute("ROL");

        if (usuarioIdObj == null || !"VIAJERO".equals(rol)) {
            return new ModelAndView("redirect:/login");
        }
        try {
            Reserva reserva = servicioReserva.confirmarPagoReserva(reservaId, (Long)usuarioIdObj);
            ReservaActivaDTO reservaActivaDTO = new ReservaActivaDTO(reserva);
            model.put("idUsuario", usuarioIdObj);
            model.put("ROL", rol);
            model.put("reserva" , reservaActivaDTO);
            model.put("pagoOK" , "El pago fue exitoso");
            return new ModelAndView("pagoExitoso" , model);
        } catch (NotFoundException | UsuarioNoAutorizadoException | AccionNoPermitidaException e){
            redirectAttributes.addFlashAttribute("error" , e.getMessage());
            return new ModelAndView("redirect:/reserva/misReservasActivas");
        }
    }


    @GetMapping("/pago/fallido")
    public ModelAndView devolverPagoFallido(HttpSession session,
                                            @RequestParam Long reservaId,
                                            RedirectAttributes redirectAttributes) {
        Object usuarioIdObj = session.getAttribute("idUsuario");
        Object rol = session.getAttribute("ROL");

        if (usuarioIdObj == null || !"VIAJERO".equals(rol)) {
            return new ModelAndView("redirect:/login");
        }
        ModelMap model = new ModelMap();
        try {
            Reserva reserva = servicioReserva.obtenerReservaPorId(reservaId);
            model.put("idUsuario", usuarioIdObj);
            model.put("ROL", rol);
            model.put("reserva", new ReservaActivaDTO(reserva));
            model.put("error", "Tu pago fue rechazado o cancelado.");
            return new ModelAndView("pagoFallido", model);

        } catch (NotFoundException e){

            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return new ModelAndView("redirect:/reserva/misReservasActivas");
        }
    }



    @GetMapping("/pago/pendiente")
    public ModelAndView devolverPagoPendiente(HttpSession session,
                                              @RequestParam Long reservaId,
                                              RedirectAttributes redirectAttributes) {
        Object usuarioIdObj = session.getAttribute("idUsuario");
        Object rol = session.getAttribute("ROL");

        if (usuarioIdObj == null || !"VIAJERO".equals(rol)) {
            return new ModelAndView("redirect:/login");
        }
        ModelMap model = new ModelMap();
        try {
            model.put("idUsuario", usuarioIdObj);
            model.put("ROL", rol);
            Reserva reserva = servicioReserva.obtenerReservaPorId(reservaId);
            model.put("reserva", new ReservaActivaDTO(reserva));
            model.put("error", "Tu pago esta pendiente");
            return new ModelAndView("pagoPendiente", model);

        } catch (NotFoundException e){

            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return new ModelAndView("redirect:/reserva/misReservasActivas");
        }
    }

    @GetMapping("/cancelar/{idReserva}")
    public ModelAndView irACancelarReserva(@PathVariable Long idReserva, HttpSession session) {
        ModelMap model = new ModelMap();
        Object usuarioIdObj = session.getAttribute("idUsuario");
        String rol = (String) session.getAttribute("ROL");

        if (usuarioIdObj == null || !"VIAJERO".equalsIgnoreCase(rol)) {
            return new ModelAndView("redirect:/login");
        }

        Long viajeroId = (Long) usuarioIdObj;
        try {
            Reserva reserva = servicioReserva.obtenerReservaPorId(idReserva);

            if (!reserva.getViajero().getId().equals(viajeroId)) {
                throw new UsuarioNoAutorizadoException("No puede acceder a esta reserva.");
            }

            model.put("reserva", reserva);
            model.put("viaje", reserva.getViaje());
            return new ModelAndView("cancelarReserva", model);

        } catch (Exception e) {
            model.put("error", "No se pudo acceder a la reserva.");
            return new ModelAndView("errorCancelarReserva", model);
        }
    }

    @PostMapping("/cancelar/{id}")
    public ModelAndView cancelarReservaViajero(@PathVariable Long id, HttpSession session) {
        Long viajeroId = (Long) session.getAttribute("idUsuario");
        ModelAndView mav = new ModelAndView("cancelarReservaViajero");

        try {
            Usuario usuarioEnSesion = servicioViajero.obtenerViajero(viajeroId);
            Reserva reserva = servicioReserva.cancelarReservaPorViajero(id, usuarioEnSesion);

            mav.addObject("reserva", reserva);
            mav.addObject("viaje", reserva.getViaje());
            mav.addObject("exito", true);
            mav.addObject("mensaje", "Tu reserva fue cancelada exitosamente.");
        } catch (Exception e) {
            mav.addObject("exito", false);
            mav.addObject("mensaje", e.getMessage());
        }

        return mav;
    }
}