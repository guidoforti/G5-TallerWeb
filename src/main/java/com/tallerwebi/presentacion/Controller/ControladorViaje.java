package com.tallerwebi.presentacion.Controller;


import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.Entity.Vehiculo;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.IServicio.ServicioVehiculo;
import com.tallerwebi.dominio.IServicio.ServicioViaje;
import com.tallerwebi.dominio.excepcion.UsuarioNoAutorizadoException;
import com.tallerwebi.dominio.excepcion.ViajeNoCancelableException;
import com.tallerwebi.dominio.excepcion.ViajeNoEncontradoException;
import com.tallerwebi.presentacion.DTO.InputsDTO.ViajeInputDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/viaje")
public class ControladorViaje {

    private ServicioViaje servicioViaje;
    private ServicioVehiculo servicioVehiculo;

    @Autowired
    public ControladorViaje(ServicioViaje servicioViaje, ServicioVehiculo servicioVehiculo) {
        this.servicioViaje = servicioViaje;
        this.servicioVehiculo = servicioVehiculo;
    }


    @GetMapping("/buscarViaje")
    public ModelAndView buscarViaje() {
        ModelMap model = new ModelMap();
        return new ModelAndView("buscarViajePorId", model);
    }

    @GetMapping("")
    public ModelAndView getViajeById(@RequestParam Long id) {
        return null;
    }

    @GetMapping("/publicar")
    public ModelAndView irAPublicarViaje(HttpSession session) {
        ModelMap model = new ModelMap();

        // Verificar que el usuario esté logueado
        Object usuarioId = session.getAttribute("usuarioId");
        Object rol = session.getAttribute("rol");

        if (usuarioId == null || !"CONDUCTOR".equals(rol)) {
            return new ModelAndView("redirect:/conductor/login");
        }

        Long conductorId = (Long) usuarioId;

        // Obtener los vehículos del conductor
        List<Vehiculo> vehiculos = servicioVehiculo.obtenerVehiculosParaConductor(conductorId);

        // Crear un DTO vacío para el formulario
        ViajeInputDTO viajeInputDTO = new ViajeInputDTO();
        viajeInputDTO.setConductorId(conductorId);

        model.put("viaje", viajeInputDTO);
        model.put("vehiculos", vehiculos);

        return new ModelAndView("publicarViaje", model);
    }

    @PostMapping("/publicar")
    public ModelAndView publicarViaje(@ModelAttribute("viaje") ViajeInputDTO viajeInputDTO, HttpSession session) {
        ModelMap model = new ModelMap();

        // Verificar que el usuario esté logueado
        Object usuarioId = session.getAttribute("usuarioId");
        Object rol = session.getAttribute("rol");

        if (usuarioId == null || !"CONDUCTOR".equals(rol)) {
            return new ModelAndView("redirect:/conductor/login");
        }

        Long conductorId = (Long) usuarioId;

        try {
            // Convertir DTO a Entity
            Viaje viaje = viajeInputDTO.toEntity();

            // Publicar el viaje (pasando el conductor ID de la sesión por seguridad)
            servicioViaje.publicarViaje(viaje, conductorId, viajeInputDTO.getIdVehiculo());

            // Redirigir al home del conductor con mensaje de éxito
            return new ModelAndView("redirect:/conductor/home");

        } catch (Exception e) {
            // Si hay error, volver a mostrar el formulario con el mensaje de error
            List<Vehiculo> vehiculos = servicioVehiculo.obtenerVehiculosParaConductor(conductorId);
            model.put("viaje", viajeInputDTO);
            model.put("vehiculos", vehiculos);
            model.put("error", e.getMessage());
            return new ModelAndView("publicarViaje", model);
        }
    }

    @PostMapping("/cancelar")
    public ModelAndView cancelarViaje(@RequestParam Long id, HttpSession session) {
    ModelMap model = new ModelMap();

    // primer valido sesion y rol
    Object usuarioId = session.getAttribute("usuarioId");
    Object rol = session.getAttribute("rol");

    if (usuarioId == null || !"CONDUCTOR".equals(rol)) {
        return new ModelAndView("redirect:/conductor/login");
    }

    // creo usuarioEnSesion y seteo id y rol
    Usuario usuarioEnSesion = new Usuario();
    usuarioEnSesion.setId((Long) usuarioId);
    usuarioEnSesion.setRol((String) rol);

    try {
        // cancelo viaje y en caso de que no funcione se accede a los catch de excpecion
        servicioViaje.cancelarViaje(id, usuarioEnSesion);

        // si fue exitoso, se devuelve a la home con mensaje de exito
        model.put("exito", "El viaje fue cancelado exitosamente.");
        return new ModelAndView("redirect:/conductor/home", model);

        // se utilizan las excecpiones en caso de error
    } catch (ViajeNoEncontradoException e) {
        model.put("error", "No se encontró el viaje especificado.");
    } catch (UsuarioNoAutorizadoException e) {
        model.put("error", "No tiene permisos para cancelar este viaje.");
    } catch (ViajeNoCancelableException e) {
        model.put("error", "El viaje no se puede cancelar en este estado.");
    } catch (Exception e) {
        model.put("error", "Ocurrió un error al intentar cancelar el viaje.");
    }

    // mensaje de error y vuelta a la vista correspondiente
    return new ModelAndView("errorCancelarViaje", model);
}

}
