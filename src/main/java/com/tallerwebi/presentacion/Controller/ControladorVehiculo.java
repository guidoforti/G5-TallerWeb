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
import java.util.ArrayList;
import java.util.List;

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

    @GetMapping("/listarVehiculos")
    public ModelAndView listarVehiculosRegistrados(HttpSession session) {
        ModelMap model = new ModelMap();
        Object rol = (session != null) ? session.getAttribute("ROL") : null;
        Object usuarioId = (session != null) ? session.getAttribute("idUsuario") : null;
        if (rol == null || !rol.equals("CONDUCTOR") || usuarioId == null) {
            Exception e = new UsuarioNoAutorizadoException("no tienes permisos para acceder a este recurso");
            model.put("error", e.getMessage());
            return new ModelAndView("usuarioNoAutorizado", model);
        }
        Long conductorId = (Long) usuarioId;
        try {
            List<Vehiculo> ListaDeVehiculos = servicioVehiculo.obtenerVehiculosParaConductor(conductorId);

            List<VehiculoOutputDTO> vehiculoOutputDTOList = new ArrayList<>();
            for(Vehiculo vehiculo : ListaDeVehiculos) {
                VehiculoOutputDTO dto = new VehiculoOutputDTO(vehiculo);
                vehiculoOutputDTOList.add(dto);
            }

            model.put("listaVehiculos", vehiculoOutputDTOList); // Pasa la lista de Vehiculos al modelo

        } catch (Exception e) {
            model.put("error", "Error al cargar vehículos: " + e.getMessage());
            return new ModelAndView("errorGeneral", model);
        }

        return new ModelAndView("listarVehiculos", model);
    }


    @GetMapping("/registrar")
    public ModelAndView mostrarFormularioDeRegistroVehiculo(HttpSession session) {
        ModelMap model = new ModelMap();
        Object rol = (session != null) ? session.getAttribute("ROL") : null;
        Object usuarioId = (session != null) ? session.getAttribute("idUsuario") : null;

        if (rol == null || !rol.equals("CONDUCTOR") || usuarioId == null) {
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

        Object rol = (session != null) ? session.getAttribute("ROL") : null;
        Object usuarioIdObj = (session != null) ? session.getAttribute("idUsuario") : null;

        if (rol == null || !rol.equals("CONDUCTOR") || usuarioIdObj == null) {
            Exception e = new UsuarioNoAutorizadoException("no tienes permisos para acceder a este recurso");
            model.put("error", e.getMessage());
            return new ModelAndView("usuarioNoAutorizado", model);
        }

        Long conductorId = (Long) usuarioIdObj; // ID del conductor en sesión

        try {
            // CORREGIDO: Usamos la variable 'conductorId' que proviene de "idUsuario"
            Conductor conductor = servicioConductor.obtenerConductor(conductorId);

            Vehiculo vehiculo = servicioVehiculo.guardarVehiculo(vehiculoInputDTO.toEntity(conductor));

            VehiculoOutputDTO vehiculoOutputDTO = new VehiculoOutputDTO(vehiculo);
            model.put("vehiculoOutPutDTO", vehiculoOutputDTO);
            // NOTA: Se recomienda devolver a /vehiculos/listar (o similar) para ver la lista de vehículos, o /conductor/home
            return new ModelAndView("redirect:/conductor/home", model);
        } catch (PatenteDuplicadaException | UsuarioInexistente | NotFoundException  e) {
            model.put("error", e.getMessage());
            model.put("vehiculoInputDTO", vehiculoInputDTO); // Mantener datos para el reintento
            return new ModelAndView("registrarVehiculo", model);
        }
    }
}