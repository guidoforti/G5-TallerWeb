package com.tallerwebi.presentacion.Controller;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.IServicio.ServicioViajero;
import com.tallerwebi.dominio.excepcion.CredencialesInvalidas;
import com.tallerwebi.dominio.excepcion.DatoObligatorioException;
import com.tallerwebi.dominio.excepcion.EdadInvalidaException;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.presentacion.DTO.InputsDTO.ViajeroLoginInputDTO;
import com.tallerwebi.presentacion.DTO.InputsDTO.ViajeroRegistroInputDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
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

    @GetMapping("/registrar")
    public ModelAndView irARegistro() {
        ModelMap model = new ModelMap();
        model.put("datosViajero", new Viajero()); 
        return new ModelAndView("registroViajero", model);
    }

    @PostMapping("/validar-registro")
    public ModelAndView registrar(@ModelAttribute("datosViajero") ViajeroRegistroInputDTO viajero, HttpSession session) {
        ModelMap model = new ModelMap();
        try {
            Viajero viajeroARegistrar = viajero.toEntity();

            Viajero viajeroRegistrado = servicioViajero.registrar(viajeroARegistrar);

            session.setAttribute("usuarioId", viajeroRegistrado.getId());
            session.setAttribute("rol", "VIAJERO");
            return new ModelAndView("redirect:/viajero/home", model);
        } catch (UsuarioExistente | EdadInvalidaException | DatoObligatorioException e) {
            model.addAttribute("error", e.getMessage());
            return new ModelAndView("registroViajero", model);
        }
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