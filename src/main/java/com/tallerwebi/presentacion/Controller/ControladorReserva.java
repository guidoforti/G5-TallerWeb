package com.tallerwebi.presentacion.Controller;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Reserva;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import com.tallerwebi.dominio.IServicio.ServicioReserva;
import com.tallerwebi.dominio.IServicio.ServicioViaje;
import com.tallerwebi.dominio.IServicio.ServicioViajero;
import com.tallerwebi.dominio.excepcion.*;
import com.tallerwebi.presentacion.DTO.InputsDTO.SolicitudReservaInputDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ReservaVistaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

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
                                                   HttpSession session) {
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

            model.put("viaje", viaje);
            model.put("solicitud", solicitudDTO);
            return new ModelAndView("solicitarReserva", model);

        } catch (NotFoundException | ViajeNoEncontradoException | UsuarioNoAutorizadoException e) {
            model.put("error", e.getMessage());
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
            return new ModelAndView("redirect:/viaje/buscar");
        } catch (SinAsientosDisponiblesException e) {
            model.put("error", "No hay asientos disponibles para este viaje");
            return new ModelAndView("redirect:/viaje/buscar");
        } catch (ViajeYaIniciadoException e) {
            model.put("error", "El viaje ya ha iniciado, no se pueden solicitar reservas");
            return new ModelAndView("redirect:/viaje/buscar");
        } catch (DatoObligatorioException | NotFoundException | UsuarioInexistente | ViajeNoEncontradoException |
                 UsuarioNoAutorizadoException e) {
            model.put("error", e.getMessage());
            return new ModelAndView("redirect:/viaje/buscar");
        }
    }

    /**
     * Lista las reservas de un viaje específico (para el conductor)
     * GET /reserva/listar?viajeId={id}
     */
    @GetMapping("/listar")
    public ModelAndView listarReservasDeViaje(@RequestParam("viajeId") Long viajeId,
                                              HttpSession session) {
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
            model.put("error", e.getMessage());
            return new ModelAndView("redirect:/viaje/buscar");
        }
    }

    /**
     * Lista las reservas del viajero logueado
     * GET /reserva/mis-reservas
     */
    @GetMapping("/misReservas")
    @Transactional(readOnly = true)  // Importante para evitar LazyInitializationException
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
}
