package com.tallerwebi.presentacion.Controller;
import com.tallerwebi.dominio.IServicio.ServicioUbicacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/viajes")
public class ControladorMapa {

    private ServicioUbicacion  servicioUbicacion;
    @Autowired
    public ControladorMapa(ServicioUbicacion servicioUbicacion){
        this.servicioUbicacion  = servicioUbicacion;
    }

    @GetMapping("/publicarViaje")
    public ModelAndView mostrarMapa() {
        ModelAndView mav = new ModelAndView("publicarViaje");
        mav.addObject("ubicaciones", servicioUbicacion.listarTodas());

        return mav;
    }
}
