package com.tallerwebi.presentacion.Controller;


import com.tallerwebi.dominio.IServicio.ServicioVehiculo;
import com.tallerwebi.dominio.excepcion.NotFoundException;
import com.tallerwebi.dominio.excepcion.PatenteDuplicadaException;
import com.tallerwebi.dominio.excepcion.UsuarioNoAutorizadoException;
import com.tallerwebi.presentacion.DTO.InputsDTO.VehiculoInputDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.VehiculoOutputDTO;
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
@RequestMapping("/vehiculos")
public class ControladorVehiculo {

    private ServicioVehiculo servicioVehiculo;

    @Autowired
    public ControladorVehiculo(ServicioVehiculo servicioVehiculo) {
        this.servicioVehiculo = servicioVehiculo;
    }


    @GetMapping("/registrar")
    public ModelAndView mostrarFormularioDeRegistroVehiculo(HttpSession session) throws UsuarioNoAutorizadoException {
        ModelMap model = new ModelMap();
        Object rol = (session != null) ? session.getAttribute("rol") : null;
        if (rol == null || !rol.equals("CONDUCTOR")) {
            Exception e = new UsuarioNoAutorizadoException("no tienes permisos para acceder a este recurso");
            model.put("error", e.getMessage());
            return new ModelAndView("usuarioNoAutorizado", model);
        }

        model.put("vehiculoInputDTO", new VehiculoInputDTO());
        return new ModelAndView("registrarVehiculo", model);
    }

    @PostMapping("/registrar")
    public ModelAndView registrarVehiculo(@ModelAttribute("vehiculoInputDTO") VehiculoInputDTO vehiculoInputDTO, HttpSession session) {
        ModelMap model = new ModelMap();
        Object rol = (session != null) ? session.getAttribute("rol") : null;
        if (rol == null || !rol.equals("CONDUCTOR")) {
            Exception e = new UsuarioNoAutorizadoException("no tienes permisos para acceder a este recurso");
            model.put("error", e.getMessage());
            return new ModelAndView("usuarioNoAutorizado", model);
        }
        try {
            VehiculoOutputDTO vehiculoOutPutDTO = servicioVehiculo.guardarVehiculo(vehiculoInputDTO, (Long) session.getAttribute("usuarioId"));
            model.put("vehiculoOutPutDTO", vehiculoOutPutDTO);
            return new ModelAndView("redirect:/home", model);
        } catch (PatenteDuplicadaException | NotFoundException e) {
            model.put("error", e.getMessage());
            return new ModelAndView("registrarVehiculo", model);
        }
    }
}
