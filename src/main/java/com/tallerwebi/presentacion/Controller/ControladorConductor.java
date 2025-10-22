package com.tallerwebi.presentacion.Controller;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import com.tallerwebi.dominio.excepcion.CredencialesInvalidas;
import com.tallerwebi.dominio.excepcion.FechaDeVencimientoDeLicenciaInvalida;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.presentacion.DTO.InputsDTO.ConductorRegistroInputDTO;
import com.tallerwebi.presentacion.DTO.ConductorLoginDTO;
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
@RequestMapping("/conductor")
public class ControladorConductor {

    private final ServicioConductor servicioConductor;


    @Autowired
    public ControladorConductor(ServicioConductor servicioConductor) {
        this.servicioConductor = servicioConductor;

    }

    @GetMapping("/registrar")
    public ModelAndView irARegistro() {
        ModelMap model = new ModelMap();
        model.put("datosConductor", new Conductor()); // objeto vacío para el form
        return new ModelAndView("registroConductor", model);
    }

    @PostMapping("/validar-registro")
    public ModelAndView registrar(@ModelAttribute("datosConductor") ConductorRegistroInputDTO conductor, HttpSession session) {
        ModelMap model = new ModelMap();
        try {
            Conductor conductorARegistrar = conductor.toEntity();

            Conductor conductorRegistrado = servicioConductor.registrar(conductorARegistrar);

            session.setAttribute("usuarioId", conductorRegistrado.getId());
            session.setAttribute("rol", "CONDUCTOR");
            return new ModelAndView("redirect:/conductor/home", model);
        } catch (UsuarioExistente | FechaDeVencimientoDeLicenciaInvalida e) {
            model.addAttribute("error", e.getMessage());
            return new ModelAndView("registroConductor", model);
        }
    }

    @GetMapping("/home")
    public ModelAndView irAHome(HttpSession session) {
        ModelMap model = new ModelMap();
        Object usuarioId = session.getAttribute("usuarioId");
        String rol = (String) session.getAttribute("rol");
        if (usuarioId == null || !"CONDUCTOR".equals(rol)) {
            return new ModelAndView("redirect:/login", model);
        }

        try {
            Long conductorId = (Long) usuarioId;
            Conductor conductor = servicioConductor.obtenerConductor(conductorId);

            model.put("nombreConductor", conductor.getNombre());
            model.put("rol", rol);
            return new ModelAndView("homeConductor", model);

        } catch (UsuarioInexistente e) {
            session.invalidate();
            model.addAttribute("error", "Su sesión no es válida. Por favor, inicie sesión nuevamente.");
            return new ModelAndView("redirect:/login", model);
        }
    }


}
