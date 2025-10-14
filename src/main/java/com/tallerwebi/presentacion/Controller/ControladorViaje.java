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
import com.tallerwebi.presentacion.DTO.OutputsDTO.ViajeVistaDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping("/listar")
    public ModelAndView listarViajes(HttpSession session) {
        ModelMap model = new ModelMap();

        Object usuarioId = session.getAttribute("usuarioId");
        Object rol = session.getAttribute("rol");

        if (usuarioId == null || !"CONDUCTOR".equals(rol)) {
        model.put("error", "Debés iniciar sesión como conductor");
        return new ModelAndView("errorAcceso", model);
    }

        Usuario usuarioEnSesion = new Usuario();
        usuarioEnSesion.setId((Long) usuarioId);
        usuarioEnSesion.setRol((String) rol);

        try {
        List<Viaje> listaViajes = servicioViaje.listarViajesPorConductor(usuarioEnSesion);

        //dtos para la vista
        List<ViajeVistaDTO> listaViajesDTO = listaViajes.stream()
                .map(ViajeVistaDTO::new)
                .collect(Collectors.toList());

        model.put("listaViajes", listaViajesDTO);
        return new ModelAndView("listarViajesConductor", model);

        } catch (UsuarioNoAutorizadoException e) {
        model.put("error", "No tenés permisos para ver los viajes");
        return new ModelAndView("errorAcceso", model);
    }
}

    @GetMapping("/cancelarViaje/{id}")
    public ModelAndView irACancelarViaje(@PathVariable Long id, HttpSession session) {
        ModelMap model = new ModelMap();

        Object usuarioId = session.getAttribute("usuarioId");
        Object rol = session.getAttribute("rol");

        // Validación de sesión
        if (usuarioId == null || !"CONDUCTOR".equals(rol)) {
            return new ModelAndView("redirect:/conductor/login");
        }

        Usuario usuarioEnSesion = new Usuario();
        usuarioEnSesion.setId((Long) usuarioId);
        usuarioEnSesion.setRol((String) rol);

        try {
        Viaje viaje = servicioViaje.obtenerViajePorId(id);
        ViajeVistaDTO viajeDTO = new ViajeVistaDTO(viaje);

        model.put("viaje", viajeDTO);
        return new ModelAndView("cancelarViaje", model);
    
    } catch (ViajeNoEncontradoException e) {
        model.put("error", "No se encontró el viaje especificado.");
        return new ModelAndView("errorCancelarViaje", model);
    } catch (UsuarioNoAutorizadoException e) {
        model.put("error", "No tiene permisos para acceder a este viaje.");
        return new ModelAndView("errorCancelarViaje", model);
    }
}

    @PostMapping("/cancelarViaje")
    public ModelAndView cancelarViaje(@RequestParam Long id, HttpSession session) {
        ModelMap model = new ModelMap();

        Object usuarioId = session.getAttribute("usuarioId");
        Object rol = session.getAttribute("rol");

        if (usuarioId == null || !"CONDUCTOR".equals(rol)) {
        return new ModelAndView("redirect:/conductor/login");
        }

        Usuario usuarioEnSesion = new Usuario();
        usuarioEnSesion.setId((Long) usuarioId);
        usuarioEnSesion.setRol((String) rol);

        try {
        servicioViaje.cancelarViaje(id, usuarioEnSesion);
        model.put("exito", "El viaje fue cancelado exitosamente.");
        return new ModelAndView("redirect:/viaje/listar");

    } catch (ViajeNoEncontradoException e) {
        model.put("error", "No se encontró el viaje especificado.");
    } catch (UsuarioNoAutorizadoException e) {
        model.put("error", "No tiene permisos para cancelar este viaje.");
    } catch (ViajeNoCancelableException e) {
        model.put("error", "El viaje no se puede cancelar en este estado.");
    } catch (Exception e) {
        model.put("error", "Ocurrió un error al intentar cancelar el viaje.");
    }

    return new ModelAndView("errorCancelarViaje", model);
    }
}
