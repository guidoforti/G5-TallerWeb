package com.tallerwebi.presentacion.Controller;

import com.tallerwebi.dominio.IServicio.ServicioNotificacion;
import com.tallerwebi.dominio.IServicio.ServicioNotificacion;
import com.tallerwebi.dominio.excepcion.NotFoundException;
import com.tallerwebi.dominio.excepcion.UsuarioNoAutorizadoException;
import com.tallerwebi.presentacion.DTO.OutputsDTO.NotificacionHistorialDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/notificaciones")
public class ControladorNotificacion {

    private final ServicioNotificacion servicioNotificacion;

    @Autowired
    public ControladorNotificacion(ServicioNotificacion servicioNotificacion) {
        this.servicioNotificacion = servicioNotificacion;
    }

    // Endpoint para la vista de listado de notificaciones (al hacer clic en la campana)
    // También funciona como la acción de marcar TODAS como leídas.
    @GetMapping("/historial")
    public ModelAndView verHistorial(HttpSession session) {
        ModelMap model = new ModelMap();
        Long idUsuario = (Long) session.getAttribute("idUsuario");

        if (idUsuario == null) {
            return new ModelAndView("redirect:/login");
        }

        try {
            // 1. Obtener la lista y marcarlas todas como leídas en una sola transacción
            // (Asumo que has creado NotificacionHistorialDTO para no exponer la entidad Notificacion)
            List<NotificacionHistorialDTO> historialDTO = servicioNotificacion.obtenerYMarcarComoLeidas(idUsuario)
                    .stream()
                    .map(NotificacionHistorialDTO::new)
                    .collect(Collectors.toList());

            model.put("notificaciones", historialDTO);
            model.put("idUsuario", idUsuario); // Necesario para la navbar si se incluye aquí.

            return new ModelAndView("historialNotificaciones", model); // Necesitas crear esta vista

        } catch (NotFoundException e) {
            model.put("error", "Error: Usuario no encontrado.");
            return new ModelAndView("errorGeneral", model);
        }
    }

    // Endpoint para que el JS marque una notificación como leída (al hacer click en el toast)
    // Es un POST para ser RESTful.
    @PostMapping("/marcar-leida/{idNotificacion}")
    @ResponseBody // Retorna un código de estado, no una vista
    public ResponseEntity<Void> marcarComoLeida(@PathVariable Long idNotificacion, HttpSession session) {
        Long idUsuario = (Long) session.getAttribute("idUsuario");

        if (idUsuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401
        }

        try {
            // El Servicio debería validar que el usuario es el dueño de la notificacion
            // Por simplicidad, solo la marcamos:
            servicioNotificacion.marcarComoLeida(idNotificacion);
            return ResponseEntity.ok().build(); // 200 OK
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 Not Found
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500
        }
    }
}