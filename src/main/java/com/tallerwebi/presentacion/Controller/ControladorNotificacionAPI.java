package com.tallerwebi.presentacion.Controller;

import com.tallerwebi.dominio.Entity.Notificacion;
import com.tallerwebi.dominio.IServicio.ServicioNotificacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
public class ControladorNotificacionAPI {

    private final ServicioNotificacion servicioNotificacion;

    @Autowired
    public ControladorNotificacionAPI(ServicioNotificacion servicioNotificacion) {
        this.servicioNotificacion = servicioNotificacion;
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<Notificacion>> getNotificacionesPendientes(HttpSession session) {
        Long userId = (Long) session.getAttribute("idUsuario");

        // 1. Verificar Autenticación (Solo usuarios logueados pueden hacer polling)
        if (userId == null) {
            // Devolver 401 Unauthorized si no hay sesión para que el JS detenga el polling.
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // 2. Obtener notificaciones y marcarlas como vistas en una sola transacción
        List<Notificacion> notificaciones = servicioNotificacion.obtenerNoVistasYMarcarComoVistas(userId);

        // 3. Devolver la lista como JSON (HttpStatus.OK 200)
        return ResponseEntity.ok(notificaciones);
    }
}