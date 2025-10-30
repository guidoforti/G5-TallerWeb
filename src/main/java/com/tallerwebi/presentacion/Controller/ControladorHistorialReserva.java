package com.tallerwebi.presentacion.Controller;

import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.IServicio.ServicioHistorialReserva;
import com.tallerwebi.presentacion.DTO.OutputsDTO.HistorialReservaDTO;
import com.tallerwebi.dominio.excepcion.UsuarioNoAutorizadoException;
import com.tallerwebi.dominio.excepcion.ViajeNoEncontradoException;
import org.springframework.stereotype.Controller;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/historial")
public class ControladorHistorialReserva {

    private ServicioHistorialReserva servicioHistorialReserva;
    private final ServicioConductor servicioConductor;
    private static final String ATTRIBUTE_ID_USUARIO = "idUsuario";
    private static final String ATTRIBUTE_ROL = "ROL";
    private static final String ROLES_CONDUCTOR = "CONDUCTOR";

    public ControladorHistorialReserva(ServicioHistorialReserva servicioHistorialReserva,
                                       ServicioConductor servicioConductor) {
        this.servicioHistorialReserva = servicioHistorialReserva;
        this.servicioConductor = servicioConductor;
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
        ModelAndView modelAndView = new ModelAndView("historialReservas");
        Object usuarioIdObj = session.getAttribute(ATTRIBUTE_ID_USUARIO);
        String rol = (String) session.getAttribute(ATTRIBUTE_ROL);

        // 1. Verificar autenticación
        if (usuarioIdObj == null || !ROLES_CONDUCTOR.equals(rol)) {
            return new ModelAndView("redirect:/login");
        }

        try {
            Long usuarioId = (Long) usuarioIdObj;
            Usuario usuarioEnSesion = servicioConductor.obtenerConductor(usuarioId);
            // 2. Llamada al servicio que contiene la lógica de autorización
            List<HistorialReservaDTO> historial = 
                    servicioHistorialReserva.obtenerHistorialPorViaje(idViaje, usuarioEnSesion);

            // 3. Éxito: Agregar datos al modelo
            modelAndView.addObject("historialReservas", historial);
            modelAndView.addObject("idViaje", idViaje);
            modelAndView.addObject("mensajeExito", "Historial cargado correctamente para el viaje ID: " + idViaje);
            
        } catch (UsuarioInexistente e) {
            session.invalidate();
            return new ModelAndView("redirect:/login");
        }catch (ViajeNoEncontradoException e) {
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