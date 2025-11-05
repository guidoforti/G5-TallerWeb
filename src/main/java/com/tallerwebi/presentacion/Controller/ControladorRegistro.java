package com.tallerwebi.presentacion.Controller;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import com.tallerwebi.dominio.IServicio.ServicioViajero;
import com.tallerwebi.dominio.excepcion.FechaDeVencimientoDeLicenciaInvalida;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.excepcion.EdadInvalidaException;
import com.tallerwebi.dominio.excepcion.DatoObligatorioException;
import com.tallerwebi.presentacion.DTO.InputsDTO.RegistroInputDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

@Controller
public class ControladorRegistro {

    private final ServicioConductor servicioConductor;
    private final ServicioViajero servicioViajero;

    @Autowired
    public ControladorRegistro(ServicioConductor servicioConductor, ServicioViajero servicioViajero) {
        this.servicioConductor = servicioConductor;
        this.servicioViajero = servicioViajero;
    }

    @GetMapping("/registrarme")
    public ModelAndView irARegistroUnificado() {
        ModelMap model = new ModelMap();
        model.put("datosRegistro", new RegistroInputDTO());
        return new ModelAndView("registro", model);
    }

    @PostMapping("/validar-registro")
    public ModelAndView registrar(@ModelAttribute("datosRegistro") RegistroInputDTO registroDTO, HttpSession session) {
        ModelMap model = new ModelMap();
        String vistaRetorno = "registro";
        model.put("datosRegistro", registroDTO);

        if (registroDTO.getRolSeleccionado() == null || registroDTO.getRolSeleccionado().isEmpty()) {
            model.put("error", "Debes seleccionar un rol para registrarte.");
            return new ModelAndView(vistaRetorno, model);
        }

        try {
            if ("CONDUCTOR".equals(registroDTO.getRolSeleccionado())) {
                Conductor nuevoConductor = registroDTO.toConductorEntity();
                Conductor conductorRegistrado = servicioConductor.registrar(nuevoConductor);
                session.setAttribute("idUsuario", conductorRegistrado.getId());
                session.setAttribute("ROL", "CONDUCTOR");
                return new ModelAndView("redirect:/conductor/home");

            } else if ("VIAJERO".equals(registroDTO.getRolSeleccionado())) {
                Viajero nuevoViajero = registroDTO.toViajeroEntity();
                nuevoViajero.setFumador(registroDTO.getFumador() != null ? registroDTO.getFumador() : false);
                String discapacidadIngresada = registroDTO.getDiscapacitado(); 
                // Si el usuario ingresó algo, guárdalo. Si no, guarda null (o cadena vacía "")
                if (discapacidadIngresada != null && !discapacidadIngresada.trim().isEmpty()) {
                nuevoViajero.setDiscapacitado(discapacidadIngresada.trim());
                } else {
                nuevoViajero.setDiscapacitado(null); // O deja que el DTO.toViajeroEntity() lo asigne
                }
                Viajero viajeroRegistrado = servicioViajero.registrar(nuevoViajero);
                session.setAttribute("idUsuario", viajeroRegistrado.getId());
                session.setAttribute("ROL", "VIAJERO");
                return new ModelAndView("redirect:/viajero/home");

            } else {
                model.put("error", "Rol seleccionado no válido.");
                return new ModelAndView(vistaRetorno, model);
            }

        } catch (UsuarioExistente e) {
            model.addAttribute("error", "Error de registro: Ya existe un usuario con ese email.");
        } catch (FechaDeVencimientoDeLicenciaInvalida e) {
            model.addAttribute("error", e.getMessage());
        } catch (EdadInvalidaException | DatoObligatorioException e) {
            model.addAttribute("error", e.getMessage());
        } catch (Exception e) {
            System.err.println("Error inesperado en el registro: " + e.getMessage());
            model.addAttribute("error", "Error desconocido durante el registro. Inténtalo de nuevo.");
        }
        return new ModelAndView(vistaRetorno, model);
    }
}