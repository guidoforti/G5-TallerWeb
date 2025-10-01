package com.tallerwebi.presentacion.Controller;

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

    @GetMapping("/login")
    public ModelAndView irALogin(HttpSession session) {
        if (session != null && session.getAttribute("usuarioId") != null) {
            return new ModelAndView("redirect:/viajero/home");
        }
        ModelMap model = new ModelMap();
        model.put("datosLogin", new ViajeroLoginInputDTO());
        return new ModelAndView("loginViajero", model);
    }

    @PostMapping("/validar-login")
    public ModelAndView validarLogin(@ModelAttribute("datosLogin") ViajeroLoginInputDTO loginDTO, HttpSession session) {
        ModelMap model = new ModelMap();
        try {
            Viajero viajero = servicioViajero.login(loginDTO.getEmail(), loginDTO.getContrasenia());
            session.setAttribute("usuarioId", viajero.getId());
            session.setAttribute("rol", "VIAJERO");
            return new ModelAndView("redirect:/viajero/home", model);
        } catch (CredencialesInvalidas e) {
            model.addAttribute("error", e.getMessage());
            return new ModelAndView("loginViajero", model);
        }
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
        if (usuarioId == null) {
            return new ModelAndView("redirect:/viajero/login", model);
        }

        try {
            Viajero viajero = servicioViajero.obtenerViajero((Long) usuarioId);
            model.put("nombreViajero", viajero.getNombre());
            return new ModelAndView("homeViajero", model);
        } catch (UsuarioInexistente e) {
            model.addAttribute("error", e.getMessage());
            return new ModelAndView("redirect:/viajero/login", model);
        }
    }

    @GetMapping("/logout")
    public ModelAndView logout(HttpSession session) {
        ModelMap model = new ModelMap();
        session.invalidate();
        return new ModelAndView("redirect:/viajero/login", model);
    }

}