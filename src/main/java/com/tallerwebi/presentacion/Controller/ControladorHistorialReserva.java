package com.tallerwebi.presentacion.Controller;

import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.IServicio.ServicioHistorialReserva;
import com.tallerwebi.presentacion.DTO.OutputsDTO.HistorialReservaDTO;
import com.tallerwebi.dominio.excepcion.UsuarioNoAutorizadoException;
import com.tallerwebi.dominio.excepcion.ViajeNoEncontradoException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/historial")
public class ControladorHistorialReserva {

    private ServicioHistorialReserva servicioHistorialReserva;
    private static final String ATTRIBUTE_USUARIO = "usuario";
    private static final String ROLES_CONDUCTOR = "CONDUCTOR";

    public ControladorHistorialReserva(ServicioHistorialReserva servicioHistorialReserva) {
        this.servicioHistorialReserva = servicioHistorialReserva;
    }

    /**
     * Muestra el historial de decisiones sobre reservas para un viaje específico.
     * Solo accesible por el conductor del viaje.
     *
     * @param idViaje El ID del viaje a consultar.
     * @param session La sesión HTTP para obtener el usuario autenticado.
     * @return ModelAndView con la lista de HistorialReservaDTO o un mensaje de error.
     */
    @GetMapping("/viaje")
    public ModelAndView verHistorialPorViaje(@RequestParam("idViaje") Long idViaje, HttpSession session) {
        ModelAndView modelAndView = new ModelAndView("historial-reservas");
        Usuario usuarioEnSesion = (Usuario) session.getAttribute(ATTRIBUTE_USUARIO);

        // 1. Verificar autenticación
        if (usuarioEnSesion == null || !ROLES_CONDUCTOR.equals(usuarioEnSesion.getRol())) {
            // Asumiendo que solo los CONDUCTORES pueden acceder a esta funcionalidad
            return new ModelAndView("redirect:/login"); 
        }

        try {
            // 2. Llamada al servicio que contiene la lógica de autorización
            List<HistorialReservaDTO> historial = 
                    servicioHistorialReserva.obtenerHistorialPorViaje(idViaje, usuarioEnSesion);

            // 3. Éxito: Agregar datos al modelo
            modelAndView.addObject("historialReservas", historial);
            modelAndView.addObject("idViaje", idViaje);
            modelAndView.addObject("mensajeExito", "Historial cargado correctamente para el viaje ID: " + idViaje);
            
        } catch (ViajeNoEncontradoException e) {
            // 4. Manejo de excepciones
            modelAndView.addObject("error", "Error: " + e.getMessage());
            modelAndView.setViewName("error"); // O redirigir a una página de listado de viajes
            
        } catch (UsuarioNoAutorizadoException e) {
            // 4. Manejo de excepciones (Criterio de Aceptación: solo conductor puede ver)
            modelAndView.addObject("error", "Error de Acceso: " + e.getMessage());
            modelAndView.setViewName("error"); 
            
        } catch (Exception e) {
            // 4. Manejo de excepciones genéricas
            modelAndView.addObject("error", "Ocurrió un error inesperado al cargar el historial.");
            modelAndView.setViewName("error");
        }

        return modelAndView;
    }
}