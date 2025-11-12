package com.tallerwebi.presentacion.Controller;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import com.tallerwebi.dominio.IServicio.ServicioNotificacion;
import com.tallerwebi.dominio.excepcion.NotFoundException;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ConductorPerfilOutPutDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/conductor")
public class ControladorConductor {

    private final ServicioConductor servicioConductor;
    private final ServicioNotificacion servicioNotificacion;

    @Autowired
    public ControladorConductor(ServicioConductor servicioConductor, ServicioNotificacion servicioNotificacion) {
        this.servicioConductor = servicioConductor;
        this.servicioNotificacion = servicioNotificacion;
    }

    @GetMapping("/home")
    public ModelAndView irAHome(HttpSession session) {
        ModelMap model = new ModelMap();
        // CLAVES CORREGIDAS: Usar "idUsuario" y "ROL" (mayúsculas)
        Object usuarioId = session.getAttribute("idUsuario");
        String rol = (String) session.getAttribute("ROL"); // Usar "ROL"

        if (usuarioId == null || !"CONDUCTOR".equals(rol)) {
            return new ModelAndView("redirect:/login", model);
        }

        try {
            Long conductorId = (Long) usuarioId;
            Conductor conductor = servicioConductor.obtenerConductor(conductorId);
            Long contador = servicioNotificacion.contarNoLeidas(conductorId);
            model.put("contadorNotificaciones", contador.intValue());
            model.put("idConductor", conductorId);
            model.put("nombreConductor", conductor.getNombre());
            model.put("rol", rol);
            model.put("ROL_ACTUAL", rol);
            return new ModelAndView("homeConductor", model);

        } catch (UsuarioInexistente | NotFoundException e) {
            session.invalidate();
            model.addAttribute("error", "Su sesión no es válida. Por favor, inicie sesión nuevamente.");
            return new ModelAndView("redirect:/login", model);
        }
    }

    @GetMapping("/perfil")
    public ModelAndView verMiPerfil(HttpSession session) throws UsuarioInexistente, NotFoundException {
        ModelMap model = new ModelMap();
        Long idConductor = (Long) session.getAttribute("idUsuario");
        String rol = (String) session.getAttribute("ROL");

        if (idConductor == null) {
            return new ModelAndView("redirect:/login");
        }

        Long contador = servicioNotificacion.contarNoLeidas(idConductor);
        model.put("contadorNotificaciones", contador.intValue());
        model.put("idUsuario", idConductor);
        model.put("ROL", rol);
        model.put("ROL_ACTUAL", rol);
        model.put("userRole", rol);

        try {
            ConductorPerfilOutPutDTO perfil = servicioConductor.obtenerPerfilDeConductor(idConductor);
            model.put("perfil", perfil);
            return new ModelAndView("perfilConductor", model);

        } catch (UsuarioInexistente e) {
            model.put("error", "Su perfil no existe.");
            return new ModelAndView("errorPerfilConductor", model);
        }
    }

    /**
     * Muestra el perfil de OTRO Conductor (por ID).
     * URL: /conductor/perfil/{id}
     */
    @GetMapping("/perfil/{id}")
    public ModelAndView verPerfilConductorPorId(@PathVariable Long id, HttpSession session) {
        ModelMap model = new ModelMap();
        Long usuarioEnSesionId = (Long) session.getAttribute("idUsuario");
        String rol = (String) session.getAttribute("ROL");

        if (usuarioEnSesionId == null) {
            return new ModelAndView("redirect:/login");
        }

        try {
            Long contador = servicioNotificacion.contarNoLeidas(usuarioEnSesionId);
            model.put("contadorNotificaciones", contador.intValue());
        } catch (NotFoundException e) {
            model.put("contadorNotificaciones", 0);
        }
        model.put("idUsuario", usuarioEnSesionId);
        model.put("ROL", rol);
        model.put("ROL_ACTUAL", rol);
        model.put("userRole", rol);

        if (!"VIAJERO".equals(rol)) {
            model.put("error", "Solo los viajeros pueden ver perfiles de otros conductores.");
            return new ModelAndView("errorAutorizacion", model);
        }

        try {
            ConductorPerfilOutPutDTO perfil = servicioConductor.obtenerPerfilDeConductor(id);
            model.put("perfil", perfil);
            return new ModelAndView("perfilConductor", model);

        } catch (UsuarioInexistente e) {
            model.put("error", "El perfil solicitado no existe.");
            return new ModelAndView("errorPerfilConductor", model);
        }
    }
}