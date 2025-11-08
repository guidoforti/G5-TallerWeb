package com.tallerwebi.presentacion.Controller;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.IServicio.ServicioViajero;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.dominio.excepcion.UsuarioNoAutorizadoException;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ViajeroPerfilOutPutDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
        // CLAVES CORREGIDAS: Usar "idUsuario" y "ROL" (mayúsculas)
        Object usuarioId = session.getAttribute("idUsuario");
        String rol = (String) session.getAttribute("ROL"); // Usar "ROL"

        if (usuarioId == null || !"VIAJERO".equals(rol)) {
            return new ModelAndView("redirect:/login", model);
        }

        try {
            Long viajeroId = (Long) usuarioId;
            Viajero viajero = servicioViajero.obtenerViajero(viajeroId);

            model.put("nombreViajero", viajero.getNombre());
            model.put("rol", rol);
            return new ModelAndView("homeViajero", model);

        } catch (UsuarioInexistente e) {
            session.invalidate();
            model.addAttribute("error", "Su sesión no es válida. Por favor, inicie sesión nuevamente.");
            return new ModelAndView("redirect:/login", model);
        }
    }

   @GetMapping("/{id}/perfil")
    public ModelAndView verPerfilViajero(@PathVariable Long id, HttpSession session) {
    ModelAndView mav = new ModelAndView();
    Usuario usuarioEnSesion = (Usuario) session.getAttribute("usuario");

    // Validación de permisos, ya que solo los conductores pueden ver el perfil de viajero
    if (!(usuarioEnSesion instanceof Conductor)) {
        mav.setViewName("errorAutorizacion");
        mav.addObject("error", "Solo los conductores pueden ver perfiles de viajeros");
        return mav;
    }

    try {
        ViajeroPerfilOutPutDTO perfil = servicioViajero.obtenerPerfilViajero(id);
        mav.setViewName("perfilViajero");
        mav.addObject("perfil", perfil);
        return mav;

    } catch (UsuarioInexistente e) {
        mav.setViewName("errorPerfilViajero");
        mav.addObject("error", "El viajero que intentás visualizar no existe");
        return mav;
    }
}

    @GetMapping("/perfil")
    public String verPerfil(HttpSession session, Model model) throws UsuarioInexistente {
        Long viajeroId = (Long) session.getAttribute("idUsuario");
        if (viajeroId == null) {
            return "redirect:/login";
        }

        ViajeroPerfilOutPutDTO perfilDTO = servicioViajero.obtenerPerfilViajero(viajeroId);
        model.addAttribute("perfil", perfilDTO);
        return "perfilViajero"; 
    }

}