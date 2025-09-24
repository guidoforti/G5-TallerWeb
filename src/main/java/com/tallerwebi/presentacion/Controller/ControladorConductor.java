package com.tallerwebi.presentacion.Controller;

import com.tallerwebi.config.ManualModelMapper;
import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import com.tallerwebi.dominio.excepcion.CredencialesInvalidas;
import com.tallerwebi.dominio.excepcion.FechaDeVencimientoDeLicenciaInvalida;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.presentacion.DTO.ConductorDTO;
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
    private final ManualModelMapper manualModelMapper;

    @Autowired
    public ControladorConductor(ServicioConductor servicioConductor, ManualModelMapper manualModelMapper) {
        this.servicioConductor = servicioConductor;
        this.manualModelMapper = manualModelMapper;
    }

    @GetMapping("/login")
    public ModelAndView irALogin(HttpSession session) {
        if (session != null && session.getAttribute("usuarioId") != null) {
            return new ModelAndView("redirect:/home");
        }
        ModelMap model = new ModelMap();
        model.put("datosLogin", new ConductorLoginDTO());
        return new ModelAndView("loginConductor", model);
    }

    @PostMapping("/validar-login")
    public ModelAndView validarLogin(@ModelAttribute("datosLogin") ConductorLoginDTO loginDTO, HttpSession session) {
        ModelMap model = new ModelMap();
        try {
            ConductorDTO conductor = servicioConductor.login(loginDTO.getEmail(), loginDTO.getContrasenia());
            session.setAttribute("usuarioId", conductor.getId());
            session.setAttribute("rol", "CONDUCTOR");
            return new ModelAndView("redirect:/conductor/home", model);
        } catch (CredencialesInvalidas e) {
            model.addAttribute("error", e.getMessage());
            return new ModelAndView("loginConductor", model);
        }
    }

    @GetMapping("/registrar")
    public ModelAndView irARegistro() {
        ModelMap model = new ModelMap();
        model.put("datosConductor", new Conductor()); // objeto vacío para el form
        return new ModelAndView("registroConductor", model);
    }

    @PostMapping("/validar-registro")
    public ModelAndView registrar(@ModelAttribute("datosConductor") ConductorDTO conductor, HttpSession session) {
        ModelMap model = new ModelMap();
        try {
            Conductor conductorARegistrar = this.manualModelMapper.toConductor(conductor);
            ConductorDTO conductorRegistrado = servicioConductor.registrar(conductorARegistrar);
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
        if (usuarioId == null) {
            return new ModelAndView("redirect:/conductor/login", model);
        }

        try {
            ConductorDTO conductorDTO = servicioConductor.obtenerConductor((Long) usuarioId);
            model.put("nombreConductor", conductorDTO.getNombre());
            return new ModelAndView("homeConductor", model);
        } catch (UsuarioInexistente e) {
            model.addAttribute("error", e.getMessage());
            return new ModelAndView("redirect:/conductor/login", model);
        }
    }

    @GetMapping("/logout")
    public ModelAndView logout(HttpSession session) {
        ModelMap model = new ModelMap();
        session.invalidate();
        return new ModelAndView("redirect:/conductor/login", model);
    }

}
