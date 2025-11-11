package com.tallerwebi.presentacion.Controller;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.IServicio.ServicioAlmacenamientoFoto;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

import javax.servlet.http.HttpSession;

@Controller
public class ControladorRegistro {

    private final ServicioConductor servicioConductor;
    private final ServicioViajero servicioViajero;
    private final ServicioAlmacenamientoFoto servicioAlmacenamientoFoto;

    @Autowired
    public ControladorRegistro(ServicioConductor servicioConductor, ServicioViajero servicioViajero, ServicioAlmacenamientoFoto servicioAlmacenamientoFoto) {
        this.servicioConductor = servicioConductor;
        this.servicioViajero = servicioViajero;
        this.servicioAlmacenamientoFoto = servicioAlmacenamientoFoto;
    }

    @GetMapping("/registrarme")
    public ModelAndView irARegistroUnificado() {
        ModelMap model = new ModelMap();
        model.put("datosRegistro", new RegistroInputDTO());
        return new ModelAndView("registro", model);
    }

    @PostMapping("/validar-registro")
    public ModelAndView registrar(@ModelAttribute("datosRegistro") RegistroInputDTO registroDTO,
                                  @RequestParam(value = "fotoPerfil", required = false) MultipartFile foto,
                                  HttpSession session) {
        ModelMap model = new ModelMap();
        String vistaRetorno = "registro";
        model.put("datosRegistro", registroDTO);

        if (registroDTO.getRolSeleccionado() == null || registroDTO.getRolSeleccionado().isEmpty()) {
            model.put("error", "Debes seleccionar un rol para registrarte.");
            return new ModelAndView(vistaRetorno, model);
        }
        if (registroDTO.getFechaNacimiento() == null) {
            model.put("error", "La fecha de nacimiento es obligatoria.");
            return new ModelAndView(vistaRetorno, model);
        }

        String fotoPerfilUrl = null;

        try {
            if (foto != null && !foto.isEmpty()) {
                fotoPerfilUrl = servicioAlmacenamientoFoto.guardarArchivo(foto);
            }

            if ("CONDUCTOR".equals(registroDTO.getRolSeleccionado())) {
                Conductor nuevoConductor = registroDTO.toConductorEntity();
                nuevoConductor.setFotoPerfilUrl(fotoPerfilUrl);
                Conductor conductorRegistrado = servicioConductor.registrar(nuevoConductor);
                session.setAttribute("idUsuario", conductorRegistrado.getId());
                session.setAttribute("ROL", "CONDUCTOR");
                return new ModelAndView("redirect:/conductor/home");

            } else if ("VIAJERO".equals(registroDTO.getRolSeleccionado())) {
                Viajero nuevoViajero = registroDTO.toViajeroEntity();
                nuevoViajero.setFotoPerfilUrl(fotoPerfilUrl);
                Viajero viajeroRegistrado = servicioViajero.registrar(nuevoViajero);
                session.setAttribute("idUsuario", viajeroRegistrado.getId());
                session.setAttribute("ROL", "VIAJERO");
                return new ModelAndView("redirect:/viajero/home");
            } else {
                model.put("error", "Rol seleccionado no válido.");
                return new ModelAndView(vistaRetorno, model);
            }

        } catch (IOException e) {
            model.addAttribute("error", "Error interno al guardar la foto de perfil. Inténtelo de nuevo.");
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