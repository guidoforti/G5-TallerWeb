package com.tallerwebi.presentacion.Controller;


import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.IServicio.ServicioViaje;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/viaje")
public class ControladorViaje {


    private ServicioViaje servicioViaje;

    public ControladorViaje(ServicioViaje servicioViaje) {
        this.servicioViaje = servicioViaje;
    }


    @GetMapping("/buscarViaje")
    public ModelAndView buscarViaje (){
        ModelMap model = new ModelMap();
        return new ModelAndView("buscarViajePorId" , model);
    }

    @GetMapping("")
    public ModelAndView getViajeById(@RequestParam Long id) {
    return null;
    }


}
