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

    /**
     * Muestra el perfil de valoraciones de un usuario y el formulario para dejar una valoración.
     */
    @GetMapping("/{receptorId}")
    public ModelAndView verValoraciones(
            @PathVariable Long receptorId, 
            @ModelAttribute("success") String successMessage, 
            @ModelAttribute("error") String errorMessage) {
        
        ModelMap model = new ModelMap();

        try {
            // 1. Obtener la información del usuario receptor para mostrar los detalles
            Optional<Usuario> receptorOpt = repositorioUsuario.buscarPorId(receptorId);
            if (!receptorOpt.isPresent()) {
                throw new UsuarioInexistente("Usuario no encontrado.");
            }

            // 2. Obtener las valoraciones y el promedio
            List<ValoracionOutputDTO> valoraciones = servicioValoracion.obtenerValoracionesDeUsuario(receptorId);
            Double promedio = servicioValoracion.calcularPromedioValoraciones(receptorId);

            // 3. Preparar el modelo
            model.put("receptorId", receptorId);
            model.put("valoraciones", valoraciones);
            model.put("promedio", promedio);
            
            // 4. Agregar mensajes flash (si existen)
            if (!successMessage.isEmpty()) {
                model.put("success", successMessage);
            }
            if (!errorMessage.isEmpty()) {
                model.put("error", errorMessage);
            }

            return new ModelAndView("verValoraciones", model);

        } catch (UsuarioInexistente e) {
            model.put("error", e.getMessage());
            return new ModelAndView("error", model);
        } catch (Exception e) {
            model.put("error", "Error al cargar las valoraciones: " + e.getMessage());
            return new ModelAndView("error", model);
        }
    }

    /**
     * Procesa el envío de una nueva valoración.
     */
    @PostMapping("/nueva")
    public ModelAndView enviarValoracion(
            @ModelAttribute("valoracionDto")  ValoracionNuevaInputDTO valoracionDto,
            HttpSession session, 
            RedirectAttributes redirectAttributes) {

        // 1. Validar sesión del emisor
        Long emisorId = (Long) session.getAttribute("idUsuario");
        if (emisorId == null) {
            return new ModelAndView("redirect:/login");
        }
        
        // 2. Obtener el usuario emisor
        Optional<Usuario> emisorOpt = repositorioUsuario.buscarPorId(emisorId);
        if (!emisorOpt.isPresent()) {
            session.invalidate(); 
            return new ModelAndView("redirect:/login");
        }
        Usuario emisor = emisorOpt.get();

        try {
            // 3. Llamar al servicio que valida y guarda la valoración
            // El servicio contiene las validaciones:
            // a) No autovaloración.
            // b) Puntuación y comentario obligatorio.
            // c) Existe viaje concluido y pendiente de valoración entre emisor y receptor.
            servicioValoracion.valorarUsuario(emisor, valoracionDto);
            
            redirectAttributes.addFlashAttribute("success", "¡Valoración enviada con éxito!");

        } catch (DatoObligatorioException | UsuarioInexistente e) {
            // Error de validación o negocio, redirigir al perfil del receptor con el mensaje de error
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
             // Error inesperado
            redirectAttributes.addFlashAttribute("error", "Error inesperado al enviar la valoración.");
        }

        // Redirigir siempre al perfil del usuario valorado para ver la actualización
        return new ModelAndView("redirect:/valoraciones/" + valoracionDto.getReceptorId());
    }
}