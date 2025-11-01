package com.tallerwebi.presentacion.Controller;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Reserva;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.Enums.EstadoDeViaje;
import com.tallerwebi.dominio.Enums.EstadoReserva;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import com.tallerwebi.dominio.IServicio.ServicioReserva;
import com.tallerwebi.dominio.IServicio.ServicioViaje;
import com.tallerwebi.dominio.IServicio.ServicioViajero;
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

    @Autowired
    public ControladorReserva(ServicioReserva servicioReserva,
                              ServicioViaje servicioViaje,
                              ServicioViajero servicioViajero,
                              ServicioConductor servicioConductor) {
        this.servicioReserva = servicioReserva;
        this.servicioViaje = servicioViaje;
        this.servicioViajero = servicioViajero;
        this.servicioConductor = servicioConductor;
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
            return new ModelAndView("listarReservasViaje", model);

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
    @GetMapping("/misReservasPendientes")
    public ModelAndView listarReservasActivas(HttpSession session) {
        ModelMap model = new ModelMap();

        // Validar sesión
        Object usuarioIdObj = session.getAttribute("idUsuario");
        Object rol = session.getAttribute("ROL");

        if (usuarioIdObj == null || !"VIAJERO".equals(rol)) {
            return new ModelAndView("redirect:/login");
        }

        Long viajeroId = (Long) usuarioIdObj;

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

            List<Reserva> reservasConfirmadas = reservas.stream()
                    .filter(r -> r.getEstado() == EstadoReserva.CONFIRMADA)
                    .collect(Collectors.toList());
            // Convertir a DTOs
            List<ReservaActivaDTO> pendientesDTO = reservasPendientes.stream()
                    .map(ReservaActivaDTO::new)
                    .collect(Collectors.toList());

            List<ReservaActivaDTO> rechazadasDTO = reservasRechazadas.stream()
                    .map(ReservaActivaDTO::new)
                    .collect(Collectors.toList());

            List<ReservaActivaDTO> confirmadasDTO = reservasConfirmadas.stream()
                    .map(ReservaActivaDTO::new)
                    .collect(Collectors.toList());

            model.put("reservasPendientes", pendientesDTO);
            model.put("reservasRechazadas", rechazadasDTO);
            model.put("reservasConfirmadas", confirmadasDTO);

            return new ModelAndView("misReservasPendientes", model);

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

        try {
            // Obtener todas las reservas confirmadas del viajero
            List<Reserva> reservas = servicioReserva.listarViajesConfirmadosPorViajero(viajeroId);

            // Categorizar por estado del viaje:
            // Próximos: DISPONIBLE, COMPLETO
            // En curso: EN_CURSO
            // Finalizados: FINALIZADO, CANCELADO
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
                    .filter(r -> r.getViaje().getEstado() == EstadoDeViaje.FINALIZADO ||
                                 r.getViaje().getEstado() == EstadoDeViaje.CANCELADO)
                    .sorted((r1, r2) -> r2.getViaje().getFechaHoraDeSalida().compareTo(r1.getViaje().getFechaHoraDeSalida()))
                    .collect(Collectors.toList());

            // Convertir a DTOs
            List<ViajeConfirmadoViajeroDTO> proximosDTO = viajesProximos.stream()
                    .map(ViajeConfirmadoViajeroDTO::new)
                    .collect(Collectors.toList());

            List<ViajeConfirmadoViajeroDTO> enCursoDTO = viajesEnCurso.stream()
                    .map(ViajeConfirmadoViajeroDTO::new)
                    .collect(Collectors.toList());

            List<ViajeConfirmadoViajeroDTO> finalizadosDTO = viajesFinalizados.stream()
                    .map(ViajeConfirmadoViajeroDTO::new)
                    .collect(Collectors.toList());

            model.put("viajesProximos", proximosDTO);
            model.put("viajesEnCurso", enCursoDTO);
            model.put("viajesFinalizados", finalizadosDTO);

            return new ModelAndView("misViajes", model);

        } catch (UsuarioInexistente e) {
            model.put("error", e.getMessage());
            return new ModelAndView("error", model);
        }
    }
}
