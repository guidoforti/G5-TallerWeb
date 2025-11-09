package com.tallerwebi.presentacion.Controller;

import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.IServicio.ServicioValoracion;
import com.tallerwebi.dominio.excepcion.DatoObligatorioException;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.dominio.excepcion.ViajeNoEncontradoException;
import com.tallerwebi.presentacion.DTO.InputsDTO.ValoracionIndividualInputDTO;
import com.tallerwebi.presentacion.DTO.InputsDTO.ValoracionViajeInputDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ViajeroParaValorarOutputDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/valoraciones")
public class ControladorValoracion {

    private final ServicioValoracion servicioValoracion;

    @Autowired
    public ControladorValoracion(ServicioValoracion servicioValoracion) {
        this.servicioValoracion = servicioValoracion;
    }


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

            List<ViajeroParaValorarOutputDTO> viajerosDTO = viajeros.stream()
                    .map(ViajeroParaValorarOutputDTO::new)
                    .collect(Collectors.toList());

            List<ValoracionIndividualInputDTO> listaValoraciones = viajerosDTO.stream()
                    .map(viajero -> new ValoracionIndividualInputDTO(viajero.getId(), null, null)) // receptorId, puntuacion, comentario
                    .collect(Collectors.toList());

            ValoracionViajeInputDTO formularioValoracion = new ValoracionViajeInputDTO(listaValoraciones);

            if (viajerosDTO.isEmpty()) {
                model.put("error", "No hay viajeros para valorar en este viaje.");
                return new ModelAndView("error", model);
            }

            // 2. Armar modelo para la vista
            model.put("viajeros", viajerosDTO);
            model.put("formularioValoracion", formularioValoracion);
            model.put("viajeId", viajeId); 
            model.put("conductorId", conductorId); // Puede ser útil para la vista

            return new ModelAndView("valorarViajero", model); 

        } catch (ViajeNoEncontradoException e) {
            model.put("error", e.getMessage());
            return new ModelAndView("error", model);

        } catch (Exception e) {
            model.put("error", "Error al cargar la lista de viajeros: " + e.getMessage());
            return new ModelAndView("error", model);
        }
    }

    // ControladorValoracion.java

    @PostMapping("/enviar") // Nuevo endpoint para recibir el formulario completo
    public ModelAndView enviarTodasLasValoraciones(@ModelAttribute("formularioValoracion") ValoracionViajeInputDTO formulario,
                                                   @RequestParam("viajeId") Long viajeId,
                                                   HttpSession session) {
        ModelMap model = new ModelMap();
        Long emisorId = (Long) session.getAttribute("idUsuario");

        Object usuarioIdObj = session.getAttribute("idUsuario");
        if (usuarioIdObj == null) {
            return new ModelAndView("redirect:/login");
        }

        try {
            Usuario emisor = servicioValoracion.obtenerUsuario(emisorId);

            for (ValoracionIndividualInputDTO dtoIndividual : formulario.getValoraciones()) {
                servicioValoracion.valorarUsuario(emisor, dtoIndividual);
            }

            model.put("mensaje", "¡Valoraciones enviadas con éxito!");
            return new ModelAndView("redirect:/conductor/home", model);

        } catch (DatoObligatorioException | UsuarioInexistente e) {
            model.put("error", "Error al enviar valoraciones: " + e.getMessage());
            return new ModelAndView("error", model);
        } catch (Exception e) {
            model.put("error", "Ocurrió un error inesperado al enviar las valoraciones.");
            return new ModelAndView("error", model);
        }
    }
}
