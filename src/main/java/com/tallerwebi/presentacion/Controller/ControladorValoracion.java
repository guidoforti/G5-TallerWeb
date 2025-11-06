package com.tallerwebi.presentacion.Controller;

import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.IRepository.RepositorioUsuario;
import com.tallerwebi.dominio.IServicio.ServicioValoracion;
import com.tallerwebi.dominio.excepcion.DatoObligatorioException;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.presentacion.DTO.InputsDTO.ValoracionNuevaInputDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ValoracionOutputDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/valoraciones")
public class ControladorValoracion {

    private final ServicioValoracion servicioValoracion;
    private final RepositorioUsuario repositorioUsuario;

    @Autowired
    public ControladorValoracion(ServicioValoracion servicioValoracion, RepositorioUsuario repositorioUsuario) {
        this.servicioValoracion = servicioValoracion;
        this.repositorioUsuario = repositorioUsuario;
    }

    @GetMapping("/{receptorId}")
    public ModelAndView verValoraciones(@PathVariable Long receptorId, HttpSession session) {
        ModelMap model = new ModelMap();

        Object usuarioIdObj = session.getAttribute("idUsuario");
        if (usuarioIdObj == null) {
            return new ModelAndView("redirect:/login");
        }

        try {
            // Buscar al usuario receptor (no se puede valorar un usuario inexistente)
            Optional<Usuario> receptorOpt = repositorioUsuario.buscarPorId(receptorId);
            if (!receptorOpt.isPresent()) {
                throw new UsuarioInexistente("El usuario que intentás ver no existe.");
            }

            // Obtener valoraciones y promedio
            List<ValoracionOutputDTO> valoraciones = servicioValoracion.obtenerValoracionesDeUsuario(receptorId);
            Double promedio = servicioValoracion.calcularPromedioValoraciones(receptorId);

            // Armar modelo para la vista
            model.put("receptor", receptorOpt.get());
            model.put("valoraciones", valoraciones);
            model.put("promedio", promedio);

            // DTO vacío para el formulario de nueva valoración
            model.put("valoracionDto", new ValoracionNuevaInputDTO());
            return new ModelAndView("verValoraciones", model);

        } catch (UsuarioInexistente e) {
            model.put("error", e.getMessage());
            return new ModelAndView("error", model);

        } catch (Exception e) {
            model.put("error", "Error al cargar las valoraciones: " + e.getMessage());
            return new ModelAndView("error", model);
        }
    }

    @PostMapping("/nueva")
    public ModelAndView enviarValoracion(@ModelAttribute("valoracionDto") ValoracionNuevaInputDTO valoracionDto, HttpSession session) {
        ModelMap model = new ModelMap();

        Object usuarioIdObj = session.getAttribute("idUsuario");
        if (usuarioIdObj == null) {
            return new ModelAndView("redirect:/login");
        }

        Long emisorId = (Long) usuarioIdObj;

        try {
            //  Obtener emisor desde la base
            Optional<Usuario> emisorOpt = repositorioUsuario.buscarPorId(emisorId);
            if (!emisorOpt.isPresent()) {
                session.invalidate();
                return new ModelAndView("redirect:/login");
            }

            Usuario emisor = emisorOpt.get();

            // Intentar guardar la valoración
            servicioValoracion.valorarUsuario(emisor, valoracionDto);

            // 4Redirigir al perfil del receptor actualizado
            return new ModelAndView("redirect:/valoraciones/" + valoracionDto.getReceptorId());

        } catch (DatoObligatorioException | UsuarioInexistente e) {
            model.put("error", e.getMessage());
            model.put("valoracionDto", valoracionDto);
            return new ModelAndView("verValoraciones", model);

        } catch (Exception e) {
            model.put("error", "Error al enviar la valoración: " + e.getMessage());
            return new ModelAndView("error", model);
        }
    }
}
