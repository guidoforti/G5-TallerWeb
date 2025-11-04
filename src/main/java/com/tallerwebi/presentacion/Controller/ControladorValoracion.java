package com.tallerwebi.presentacion.Controller;

import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.IServicio.ServicioValoracion;
import com.tallerwebi.dominio.excepcion.DatoObligatorioException;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ValoracionOutputDTO; 
import com.tallerwebi.presentacion.DTO.InputsDTO.ValoracionNuevaInputDTO; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/valoraciones")
public class ControladorValoracion {

    private final ServicioValoracion servicioValoracion;

    @Autowired
    public ControladorValoracion(ServicioValoracion servicioValoracion) {
        this.servicioValoracion = servicioValoracion;
    }

    // --- GET: Ver Valoraciones (Chequeo OK) ---
    @GetMapping("/{usuarioId}")
    public String verValoraciones(@PathVariable Long usuarioId, Model model) {
        
        // El servicio ya devuelve los valores mapeados y un 0.0 seguro para el promedio.
        List<ValoracionOutputDTO> valoraciones = servicioValoracion.obtenerValoracionesDeUsuario(usuarioId);
        Double promedio = servicioValoracion.calcularPromedioValoraciones(usuarioId);

        model.addAttribute("valoraciones", valoraciones);
        model.addAttribute("promedio", promedio);
        model.addAttribute("receptorId", usuarioId); // Es útil tener el ID en el modelo

        return "valoraciones/verValoraciones";
    }

    // --- POST: Valorar Usuario (Correcciones Implementadas) ---
    @PostMapping("/nueva")
    public String valorarUsuario(@ModelAttribute("dto") ValoracionNuevaInputDTO dto, 
                                 HttpSession session, 
                                 RedirectAttributes attributes) {
        
        Usuario emisor = (Usuario) session.getAttribute("usuario");

        // 1. Validar Sesión: Chequea si el usuario está logueado
        if (emisor == null || emisor.getId() == null) {
            // Si no está logueado, redirige al login o a una página de error de sesión
            attributes.addFlashAttribute("error", "Debes iniciar sesión para valorar.");
            return "redirect:/login"; 
        }

        try {
            servicioValoracion.valorarUsuario(emisor, dto);
            attributes.addFlashAttribute("success", "¡Valoración guardada con éxito!");
            // Redirige al perfil del usuario valorado para ver el resultado
            return "redirect:/valoraciones/" + dto.getReceptorId(); 

        // 2. Manejo Específico de Excepciones de Negocio
        } catch (DatoObligatorioException | UsuarioInexistente e) {
            // Usa FlashAttributes para enviar el error una única vez a la página de destino
            attributes.addFlashAttribute("error", e.getMessage());
            
            // Si falla la valoración, redirige a la vista del receptor, 
            // manteniendo el patrón PRG (Post/Redirect/Get)
            return "redirect:/valoraciones/" + dto.getReceptorId();
            
        } catch (Exception e) {
             // Catch-all para errores inesperados (loguear e informar)
            attributes.addFlashAttribute("error", "Ocurrió un error inesperado al guardar la valoración.");
            System.err.println("Error no controlado en ValoracionController: " + e.getMessage());
            return "redirect:/home"; 
        }
    }
}
