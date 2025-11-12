package com.tallerwebi.presentacion.Controller;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Reserva;
import com.tallerwebi.dominio.Entity.Usuario;
import com.tallerwebi.dominio.Entity.Viajero;
import com.tallerwebi.dominio.Enums.EstadoDeViaje;
import com.tallerwebi.dominio.IServicio.ServicioViajero;
import com.tallerwebi.dominio.IServicio.ServicioNotificacion;
import com.tallerwebi.dominio.IServicio.ServicioReserva;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.dominio.excepcion.NotFoundException;

import com.tallerwebi.dominio.excepcion.UsuarioNoAutorizadoException;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ViajeConfirmadoViajeroDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ViajeroPerfilOutPutDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/viajero")
public class ControladorViajero {

    private final ServicioViajero servicioViajero;
    private final ServicioNotificacion servicioNotificacion;
    private final ServicioReserva servicioReserva;

    @Autowired
    public ControladorViajero(ServicioViajero servicioViajero, ServicioNotificacion servicioNotificacion, ServicioReserva servicioReserva) {
        this.servicioViajero = servicioViajero;
        this.servicioNotificacion = servicioNotificacion;
        this.servicioReserva = servicioReserva;
    }

    @GetMapping("/home")
    public ModelAndView irAHome(HttpSession session) {
        ModelMap model = new ModelMap();
        Object usuarioId = session.getAttribute("idUsuario");
        String rol = (String) session.getAttribute("ROL");

        if (usuarioId == null || !"VIAJERO".equals(rol)) {
            return new ModelAndView("redirect:/login", model);
        }

        try {
            Long viajeroId = (Long) usuarioId;
            Viajero viajero = servicioViajero.obtenerViajero(viajeroId);
            Long contador = servicioNotificacion.contarNoLeidas(viajeroId);
            List<Reserva> reservasConfirmadas = servicioReserva.listarViajesConfirmadosPorViajero(viajeroId);

            List<Reserva> viajesProximos = reservasConfirmadas.stream()
                    .filter(r -> r.getViaje().getEstado() == EstadoDeViaje.DISPONIBLE ||
                                r.getViaje().getEstado() == EstadoDeViaje.COMPLETO)
                    .sorted((r1, r2) -> r1.getViaje().getFechaHoraDeSalida().compareTo(r2.getViaje().getFechaHoraDeSalida()))
                    .limit(3) 
                    .collect(Collectors.toList());
            List<ViajeConfirmadoViajeroDTO> proximosDTO = viajesProximos.stream()
                    .map(ViajeConfirmadoViajeroDTO::new)
                    .collect(Collectors.toList());
            model.put("viajesProximos", proximosDTO);
            model.put("idUsuario", viajeroId);
            model.put("ROL", rol);
            model.put("ROL_ACTUAL", rol);
            model.put("contadorNotificaciones", contador.intValue());

            model.put("nombreViajero", viajero.getNombre());
            return new ModelAndView("homeViajero", model);

        } catch (UsuarioInexistente e) {
            session.invalidate();
            model.addAttribute("error", "Su sesión no es válida. Por favor, inicie sesión nuevamente.");
            return new ModelAndView("redirect:/login", model);
        }
    }

    @GetMapping("/perfil")
    public ModelAndView verMiPerfil(HttpSession session) throws UsuarioInexistente {
        ModelMap model = new ModelMap();
        Long viajeroId = (Long) session.getAttribute("idUsuario");
        String rol = (String) session.getAttribute("ROL");

        if (viajeroId == null) {
            return new ModelAndView("redirect:/login");
        }

        try {
            Long contador = servicioNotificacion.contarNoLeidas(viajeroId);
            model.put("contadorNotificaciones", contador.intValue());
        } catch (NotFoundException e) {
            model.put("contadorNotificaciones", 0);
        }
        model.put("idUsuario", viajeroId);
        model.put("ROL", rol);
        model.put("ROL_ACTUAL", rol);
        model.put("userRole", rol);

        try {
            ViajeroPerfilOutPutDTO perfilDTO = servicioViajero.obtenerPerfilViajero(viajeroId);
            model.put("perfil", perfilDTO);
            return new ModelAndView("perfilViajero", model);

        } catch (UsuarioInexistente e) {
            model.put("error", "Su perfil no existe.");
            return new ModelAndView("errorPerfilViajero", model);
        }
    }

    @GetMapping("/perfil/{id}")
    public ModelAndView verPerfilViajeroPorId(@PathVariable Long id, HttpSession session) {
        ModelMap model = new ModelMap();
        Long usuarioEnSesionId = (Long) session.getAttribute("idUsuario");
        String rol = (String) session.getAttribute("ROL");

        if (usuarioEnSesionId == null) {
            return new ModelAndView("redirect:/login");
        }

        try {
            Long contador = servicioNotificacion.contarNoLeidas(usuarioEnSesionId);
            model.put("contadorNotificaciones", contador.intValue());
        } catch (NotFoundException e) {
            model.put("contadorNotificaciones", 0);
        }
        model.put("idUsuario", usuarioEnSesionId);
        model.put("ROL", rol);
        model.put("ROL_ACTUAL", rol);
        model.put("userRole", rol);

        if (!"CONDUCTOR".equals(rol)) {
            model.put("error", "Solo los conductores pueden ver perfiles de otros viajeros.");
            return new ModelAndView("errorAutorizacion", model);
        }

        try {
            ViajeroPerfilOutPutDTO perfil = servicioViajero.obtenerPerfilViajero(id);
            model.put("perfil", perfil);
            return new ModelAndView("perfilViajero", model);

        } catch (UsuarioInexistente e) {
            model.put("error", "El perfil solicitado no existe.");
            return new ModelAndView("errorPerfilViajero", model);
        }
    }
}