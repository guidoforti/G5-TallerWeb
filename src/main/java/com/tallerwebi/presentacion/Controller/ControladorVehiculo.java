package com.tallerwebi.presentacion.Controller;


import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Vehiculo;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import com.tallerwebi.dominio.IServicio.ServicioVehiculo;
import com.tallerwebi.dominio.excepcion.NotFoundException;
import com.tallerwebi.dominio.excepcion.PatenteDuplicadaException;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
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
    private ServicioConductor servicioConductor;

    @Autowired
    public ControladorVehiculo(ServicioVehiculo servicioVehiculo , ServicioConductor servicioConductor) {
        this.servicioVehiculo = servicioVehiculo;
        this.servicioConductor = servicioConductor;
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
            Conductor conductor = servicioConductor.obtenerConductor((Long) session.getAttribute("usuarioId"));

            Vehiculo vehiculo = servicioVehiculo.guardarVehiculo(vehiculoInputDTO.toEntity(conductor));
            VehiculoOutputDTO vehiculoOutputDTO = new VehiculoOutputDTO(vehiculo);
            model.put("vehiculoOutPutDTO", vehiculoOutputDTO);
            return new ModelAndView("redirect:/conductor/home", model);
        } catch (PatenteDuplicadaException | UsuarioInexistente | NotFoundException e) {
            model.put("error", e.getMessage());
            return new ModelAndView("registrarVehiculo", model);
        }
    }
}
