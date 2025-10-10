package com.tallerwebi.presentacion.Controller;


import com.tallerwebi.dominio.Entity.Vehiculo;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.IServicio.ServicioVehiculo;
import com.tallerwebi.dominio.IServicio.ServicioViaje;
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


}
