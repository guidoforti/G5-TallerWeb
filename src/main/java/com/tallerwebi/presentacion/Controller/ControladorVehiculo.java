package com.tallerwebi.presentacion.Controller;


import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Vehiculo;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import com.tallerwebi.dominio.IServicio.ServicioNotificacion;
import com.tallerwebi.dominio.IServicio.ServicioVehiculo;
import com.tallerwebi.dominio.excepcion.NotFoundException;
import com.tallerwebi.dominio.excepcion.PatenteDuplicadaException;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.dominio.excepcion.UsuarioNoAutorizadoException;
import com.tallerwebi.dominio.excepcion.VehiculoConViajesActivosException;
import com.tallerwebi.presentacion.DTO.InputsDTO.VehiculoInputDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.VehiculoOutputDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/vehiculos")
public class ControladorVehiculo {

    private final ServicioVehiculo servicioVehiculo;
    private final ServicioConductor servicioConductor;
    private final ServicioNotificacion servicioNotificacion;

    @Autowired
    public ControladorVehiculo(ServicioVehiculo servicioVehiculo , ServicioConductor servicioConductor, ServicioNotificacion servicioNotificacion) {
        this.servicioVehiculo = servicioVehiculo;
        this.servicioConductor = servicioConductor;
        this.servicioNotificacion = servicioNotificacion;
    }

    private String encodeUrl(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            // Esto no debería suceder con UTF-8
            return value;
        }
    }

    @GetMapping("/listarVehiculos")
    public ModelAndView listarVehiculosRegistrados(HttpSession session,
                                                   @RequestParam(value = "error", required = false) String errorMessage,
                                                   @RequestParam(value = "mensaje", required = false) String successMessage) {
        ModelMap model = new ModelMap();

        // 1. Pasar mensajes de URL al Modelo
        if (errorMessage != null) {
            model.put("error", errorMessage);
        }
        if (successMessage != null) {
            model.put("mensaje", successMessage);
        }

        Object rol = (session != null) ? session.getAttribute("ROL") : null;
        Object usuarioId = (session != null) ? session.getAttribute("idUsuario") : null;
        if (rol == null || !rol.equals("CONDUCTOR") || usuarioId == null) {
            Exception e = new UsuarioNoAutorizadoException("no tienes permisos para acceder a este recurso");
            model.put("error", e.getMessage());
            return new ModelAndView("usuarioNoAutorizado", model);
        }
        Long conductorId = (Long) usuarioId;
        model.put("contadorNotificaciones", servicioNotificacion.contarNoLeidas(conductorId));
        try {
            // Usamos obtenerTodosLosVehiculosDeConductor para mostrar la lista completa (incluidos DESACTIVADO)
            List<Vehiculo> ListaDeVehiculos = servicioVehiculo.obtenerTodosLosVehiculosDeConductor(conductorId);

            List<VehiculoOutputDTO> vehiculoOutputDTOList = new ArrayList<>();
            for(Vehiculo vehiculo : ListaDeVehiculos) {
                VehiculoOutputDTO dto = new VehiculoOutputDTO(vehiculo);
                vehiculoOutputDTOList.add(dto);
            }

            model.put("listaVehiculos", vehiculoOutputDTOList);

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
        Long conductorId = (Long) usuarioId;
        model.put("contadorNotificaciones", servicioNotificacion.contarNoLeidas(conductorId));

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
            Conductor conductor = servicioConductor.obtenerConductor(conductorId);

            Vehiculo vehiculo = servicioVehiculo.guardarVehiculo(vehiculoInputDTO.toEntity(conductor));

            VehiculoOutputDTO vehiculoOutputDTO = new VehiculoOutputDTO(vehiculo);
            model.put("vehiculoOutPutDTO", vehiculoOutputDTO);

            String mensajeExito = "¡Vehículo '" + vehiculo.getPatente() + "' registrado con éxito!";
            String encodedMensaje = encodeUrl(mensajeExito);

            return new ModelAndView("redirect:/vehiculos/listarVehiculos?mensaje=" + encodedMensaje);

        } catch (PatenteDuplicadaException | UsuarioInexistente | NotFoundException  e) {
            model.put("error", e.getMessage());
            model.put("vehiculoInputDTO", vehiculoInputDTO);
            return new ModelAndView("registrarVehiculo", model);
        }
    }

    @GetMapping("/desactivar/{id}")
    public ModelAndView mostrarConfirmacionDesactivar(@PathVariable Long id, HttpSession session) {
        ModelMap model = new ModelMap();
        Long conductorId = (Long) session.getAttribute("idUsuario");

        if (conductorId == null || !("CONDUCTOR").equals(session.getAttribute("ROL"))) {
            return new ModelAndView("redirect:/login");
        }

        String urlRedireccion = "redirect:/vehiculos/listarVehiculos";

        try {
            Vehiculo vehiculo = servicioVehiculo.getById(id);

            if (!vehiculo.getConductor().getId().equals(conductorId)) {
                model.put("error", "No tienes permiso para desactivar este vehículo.");
                return new ModelAndView("usuarioNoAutorizado", model);
            }

            servicioVehiculo.verificarViajesActivos(id);

            model.put("vehiculo", new VehiculoOutputDTO(vehiculo));
            return new ModelAndView("desactivarVehiculo", model);

        } catch (NotFoundException | VehiculoConViajesActivosException e) {
            String mensajeError = encodeUrl(e.getMessage());
            return new ModelAndView(urlRedireccion + "?error=" + mensajeError);
        }
    }

    @PostMapping("/desactivar/{id}")
    public ModelAndView desactivarVehiculo(@PathVariable("id") Long vehiculoId, HttpSession session) {

        Long conductorId = (Long) session.getAttribute("idUsuario");
        if (conductorId == null || !("CONDUCTOR").equals(session.getAttribute("ROL"))) {
            return new ModelAndView("redirect:/login");
        }

        String urlRedireccion = "redirect:/vehiculos/listarVehiculos";
        String mensaje = "";

        try {
            servicioVehiculo.desactivarVehiculo(vehiculoId);
            mensaje = "Vehículo desactivado correctamente.";
            urlRedireccion += "?mensaje=" + encodeUrl(mensaje);

        } catch (NotFoundException e) {
            mensaje = "Error: Vehículo no encontrado.";
            urlRedireccion += "?error=" + encodeUrl(mensaje);

        } catch (VehiculoConViajesActivosException e) {
            mensaje = e.getMessage();
            urlRedireccion += "?error=" + encodeUrl(mensaje);

        } catch (Exception e) {
            mensaje = "Error inesperado al intentar desactivar el vehículo.";
            urlRedireccion += "?error=" + encodeUrl(mensaje);
        }

        return new ModelAndView(urlRedireccion);
    }
}