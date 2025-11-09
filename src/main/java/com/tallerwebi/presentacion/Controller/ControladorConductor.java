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
            return new ModelAndView("homeConductor", model);

        } catch (UsuarioInexistente | NotFoundException e) {
            session.invalidate();
            model.addAttribute("error", "Su sesión no es válida. Por favor, inicie sesión nuevamente.");
            return new ModelAndView("redirect:/login", model);
        }
    }

    @GetMapping("/verPerfilConductor/{conductorId}")
    public ModelAndView verPerfilConductor(@PathVariable Long conductorId, HttpSession session) {
        String rol = (String) session.getAttribute("ROL");
        if (rol == null || !rol.equals("VIAJERO")) {
            return new ModelAndView("redirect:/login");
        }

        try {
            ConductorPerfilOutPutDTO perfil = servicioConductor.obtenerPerfilDeConductor(conductorId);
            ModelMap modelo = new ModelMap();
            modelo.put("perfil", perfil);
            return new ModelAndView("perfilConductor", modelo);
        } catch (UsuarioInexistente e) {
            ModelMap modelo = new ModelMap();
            modelo.put("error", e.getMessage());
            return new ModelAndView("error", modelo);
        }
    }

    @GetMapping("/perfil")
    public ModelAndView verPerfilConductor(HttpSession session) throws UsuarioInexistente {
        Long idConductor = (Long) session.getAttribute("idUsuario");

        if (idConductor == null) {
            return new ModelAndView("redirect:/login");
        }

        ConductorPerfilOutPutDTO perfil = servicioConductor.obtenerPerfilDeConductor(idConductor);

        ModelMap model = new ModelMap();
        model.put("perfil", perfil);

        return new ModelAndView("perfilConductor", model);
    }
}