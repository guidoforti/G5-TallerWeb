package com.tallerwebi.presentacion.Controller;

import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.Entity.Valoracion;
import com.tallerwebi.dominio.IServicio.ServicioValoracion;
import com.tallerwebi.dominio.excepcion.DatoObligatorioException;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.dominio.excepcion.ViajeNoEncontradoException;
import com.tallerwebi.presentacion.DTO.InputsDTO.ValoracionNuevaInputDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ValoracionOutputDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/valoraciones")
public class ControladorValoracion {

    private final ServicioValoracion servicioValoracion;

    @Autowired
    public ControladorValoracion(ServicioValoracion servicioValoracion) {
        this.servicioValoracion = servicioValoracion;
    }


    /* 
    @GetMapping("/{receptorId}")
    public ModelAndView verValoraciones(@PathVariable Long receptorId, HttpSession session) {
        ModelMap model = new ModelMap();

        Object usuarioIdObj = session.getAttribute("idUsuario");
        if (usuarioIdObj == null) {
            return new ModelAndView("redirect:/login");
        }

        try {
            // Buscar al viajero receptor usando el servicio
            Viajero receptor = servicioValoracion.obtenerViajero(receptorId);

            // Armar modelo para la vista (solo formulario de nueva valoración)
            model.put("receptor", receptor);
            model.put("valoracionDto", new ValoracionNuevaInputDTO());
            return new ModelAndView("valorarViajero", model);

        } catch (UsuarioInexistente e) {
            model.put("error", e.getMessage());
            return new ModelAndView("error", model);

        } catch (Exception e) {
            model.put("error", "Error al cargar el perfil: " + e.getMessage());
            return new ModelAndView("error", model);
        }
    }

    */

    @GetMapping("/viaje/{viajeId}")
    public ModelAndView verViajerosParaValorar(@PathVariable Long viajeId, HttpSession session) {
        ModelMap model = new ModelMap();

        Object usuarioIdObj = session.getAttribute("idUsuario");
        if (usuarioIdObj == null) {
            return new ModelAndView("redirect:/login");
        }
        
        // Asumiendo que el usuario logueado es el conductor:
        Long conductorId = (Long) usuarioIdObj;

        try {
            // 1. Obtener la lista de viajeros usando el nuevo método del servicio
            List<Viajero> viajeros = servicioValoracion.obtenerViajeros(viajeId);

            if (viajeros.isEmpty()) {
                model.put("error", "No hay viajeros para valorar en este viaje.");
                return new ModelAndView("error", model);
            }

            // 2. Armar modelo para la vista
            model.put("viajeros", viajeros);
            model.put("viajeId", viajeId); 
            model.put("conductorId", conductorId); // Puede ser útil para la vista
            
            // 🔥 Nueva vista: Necesitas crear una vista (HTML) que itere sobre la lista 'viajeros'
            return new ModelAndView("valorarViajero", model); 

        } catch (ViajeNoEncontradoException e) {
            model.put("error", e.getMessage());
            return new ModelAndView("error", model);

        } catch (Exception e) {
            model.put("error", "Error al cargar la lista de viajeros: " + e.getMessage());
            return new ModelAndView("error", model);
        }
    }

    @PostMapping("/nueva")
    public ModelAndView enviarValoracion(@ModelAttribute("valoracionDto") ValoracionNuevaInputDTO valoracionDto, HttpSession session) {
        ModelMap model = new ModelMap();

        Object usuarioIdObj = session.getAttribute("idUsuario");
        if (usuarioIdObj == null) {
            return new ModelAndView("redirect:/login");
        }

        Long emisorId = (Long) usuarioIdObj;

        try {
            // Obtener emisor (puede ser Viajero o Conductor) desde el servicio
            Usuario emisor = servicioValoracion.obtenerUsuario(emisorId);

            // Intentar guardar la valoración
            servicioValoracion.valorarUsuario(emisor, valoracionDto);

            // Redirigir a la home o a una vista de confirmación
            return new ModelAndView("redirect:/home");

        } catch (DatoObligatorioException | UsuarioInexistente e) {
            model.put("error", e.getMessage());
            model.put("valoracionDto", valoracionDto);

            // Cargar receptor para volver a mostrar la vista
            try {
                Viajero receptor = servicioValoracion.obtenerViajero(valoracionDto.getReceptorId());
                model.put("receptor", receptor);
            } catch (UsuarioInexistente ex) {
                model.put("error", "Error: " + ex.getMessage());
            }

            return new ModelAndView("valorarViajero", model);

        } catch (Exception e) {
            model.put("error", "Error al enviar la valoración: " + e.getMessage());
            return new ModelAndView("error", model);
        }
    }
}
