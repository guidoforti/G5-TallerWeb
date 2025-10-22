package com.tallerwebi.presentacion.Controller;

import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.IServicio.ServicioViajero;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/viajero")
public class ControladorViajero {

    private final ServicioViajero servicioViajero;


    @Autowired
    public ControladorViajero(ServicioViajero servicioViajero) {
        this.servicioViajero = servicioViajero;

    }

    @GetMapping("/home")
    public ModelAndView irAHome(HttpSession session) {
        ModelMap model = new ModelMap();
        Object usuarioId = session.getAttribute("usuarioId");
        String rol = (String) session.getAttribute("rol");
        if (usuarioId == null || !"VIAJERO".equals(rol)) {
            return new ModelAndView("redirect:/login", model);
        }

        try {
            Long viajeroId = (Long) usuarioId;
            Viajero viajero = servicioViajero.obtenerViajero(viajeroId);

            model.put("nombreConductor", viajero.getNombre());
            model.put("rol", rol);
            return new ModelAndView("homeViajero", model);

        } catch (UsuarioInexistente e) {
            session.invalidate();
            model.addAttribute("error", "Su sesión no es válida. Por favor, inicie sesión nuevamente.");
            return new ModelAndView("redirect:/login", model);
        }
    }


}