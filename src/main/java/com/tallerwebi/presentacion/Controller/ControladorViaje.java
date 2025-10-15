package com.tallerwebi.presentacion.Controller;

import com.tallerwebi.dominio.Entity.Conductor;
import com.tallerwebi.dominio.Entity.Usuario;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.tallerwebi.dominio.Entity.Ciudad;
import com.tallerwebi.dominio.Entity.Parada;
import com.tallerwebi.dominio.Entity.Vehiculo;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.IServicio.ServicioCiudad;
import com.tallerwebi.dominio.IServicio.ServicioNominatim;
import com.tallerwebi.dominio.IServicio.ServicioVehiculo;
import com.tallerwebi.dominio.IServicio.ServicioViaje;
import com.tallerwebi.dominio.IServicio.ServicioConductor;
import com.tallerwebi.dominio.IServicio.ServicioVehiculo;
import com.tallerwebi.dominio.IServicio.ServicioViaje;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.dominio.excepcion.UsuarioNoAutorizadoException;
import com.tallerwebi.dominio.excepcion.ViajeNoCancelableException;
import com.tallerwebi.dominio.excepcion.ViajeNoEncontradoException;
import com.tallerwebi.presentacion.DTO.InputsDTO.ViajeInputDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ViajeVistaDTO;
import com.tallerwebi.dominio.excepcion.NominatimResponseException;
import com.tallerwebi.dominio.excepcion.NotFoundException;
import com.tallerwebi.presentacion.DTO.InputsDTO.ViajeInputDTO;
import com.tallerwebi.presentacion.DTO.NominatimResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/viaje")
public class ControladorViaje {

    private final ServicioViaje servicioViaje;
    private final ServicioVehiculo servicioVehiculo;
    private final ServicioNominatim servicioNominatim;
    private final ServicioCiudad servicioCiudad;
    private final ServicioConductor servicioConductor; 

    @Autowired
    public ControladorViaje(ServicioViaje servicioViaje,
                           ServicioVehiculo servicioVehiculo,
                           ServicioNominatim servicioNominatim,
                           ServicioCiudad servicioCiudad, 
                           ServicioConductor servicioConductor) {
        this.servicioViaje = servicioViaje;
        this.servicioVehiculo = servicioVehiculo;
        this.servicioNominatim = servicioNominatim;
        this.servicioCiudad = servicioCiudad;
        this.servicioConductor = servicioConductor;
        }


    @GetMapping("/buscarViaje")
    public ModelAndView buscarViaje() {
        ModelMap model = new ModelMap();
        return new ModelAndView("buscarViajePorId", model);
    }

    @GetMapping("")
    public ModelAndView getViajeById(@RequestParam Long id) {
        return null;
    }

    @GetMapping("/publicar")
    public ModelAndView irAPublicarViaje(HttpSession session) {
        ModelMap model = new ModelMap();

        // Verificar que el usuario esté logueado
        Object usuarioId = session.getAttribute("usuarioId");
        Object rol = session.getAttribute("rol");

        if (usuarioId == null || !"CONDUCTOR".equals(rol)) {
            return new ModelAndView("redirect:/conductor/login");
        }

        Long conductorId = (Long) usuarioId;

        // Obtener los vehículos del conductor
        List<Vehiculo> vehiculos = servicioVehiculo.obtenerVehiculosParaConductor(conductorId);

        // Crear un DTO vacío para el formulario
        ViajeInputDTO viajeInputDTO = new ViajeInputDTO();
        viajeInputDTO.setConductorId(conductorId);

        model.put("viaje", viajeInputDTO);
        model.put("vehiculos", vehiculos);

        return new ModelAndView("publicarViaje", model);
    }

    @PostMapping("/publicar")
    public ModelAndView publicarViaje(@ModelAttribute("viaje") ViajeInputDTO viajeInputDTO, HttpSession session) {
        ModelMap model = new ModelMap();

        // Verificar que el usuario esté logueado
        Object usuarioId = session.getAttribute("usuarioId");
        Object rol = session.getAttribute("rol");

        if (usuarioId == null || !"CONDUCTOR".equals(rol)) {
            return new ModelAndView("redirect:/conductor/login");
        }

        Long conductorId = (Long) usuarioId;

        try {
            // 1. Resolver ciudades usando Nominatim
            Ciudad origen = resolverCiudad(viajeInputDTO.getNombreCiudadOrigen());
            Ciudad destino = resolverCiudad(viajeInputDTO.getNombreCiudadDestino());

            // 2. Convertir DTO a Entity pasando las ciudades resueltas
            Viaje viaje = viajeInputDTO.toEntity(origen, destino, new ArrayList<>());

            // 3. Crear y setear paradas si existen
            if (viajeInputDTO.getNombresParadas() != null && !viajeInputDTO.getNombresParadas().isEmpty()) {
                List<Parada> paradas = crearParadasDesdeNombres(viajeInputDTO.getNombresParadas(), viaje);
                viaje.setParadas(paradas);
            }

            // 4. Publicar el viaje
            servicioViaje.publicarViaje(viaje, conductorId, viajeInputDTO.getIdVehiculo());

            // Redirigir al home del conductor con mensaje de éxito
            return new ModelAndView("redirect:/conductor/home");

        } catch (Exception e) {
            // Si hay error, volver a mostrar el formulario con el mensaje de error
            List<Vehiculo> vehiculos = servicioVehiculo.obtenerVehiculosParaConductor(conductorId);
            model.put("viaje", viajeInputDTO);
            model.put("vehiculos", vehiculos);
            model.put("error", e.getMessage());
            return new ModelAndView("publicarViaje", model);
        }
    }

    @GetMapping("/listar")
public ModelAndView listarViajes(HttpSession session) { // QUITAMOS el 'throws UsuarioInexistente'
    ModelMap model = new ModelMap();

    Object usuarioIdObj = session.getAttribute("usuarioId");
    Object rol = session.getAttribute("rol");

    // 1. Validación de Sesión y Rol (Se mantiene)
    if (usuarioIdObj == null || !"CONDUCTOR".equals(rol)) {
        model.put("error", "Debés iniciar sesión como conductor");
        return new ModelAndView("errorAcceso", model);
    }
    
    Long conductorId = (Long) usuarioIdObj;
    Conductor conductorEnSesion; // Declaramos aquí

    try {
        // 2. BUSCAR AL CONDUCTOR (El servicio lanza la excepción si no lo encuentra)
        conductorEnSesion = servicioConductor.obtenerConductor(conductorId); 
        
        // 3. Listar Viajes (Llamada al servicio de Viaje)
        List<Viaje> listaViajes = servicioViaje.listarViajesPorConductor(conductorEnSesion);

        // dtos para la vista
        List<ViajeVistaDTO> listaViajesDTO = listaViajes.stream()
                .map(ViajeVistaDTO::new)
                .collect(Collectors.toList());

        model.put("listaViajes", listaViajesDTO);
        return new ModelAndView("listarViajesConductor", model);

    } catch (UsuarioInexistente e) {
        // MANEJO DEL ERROR DE BÚSQUEDA POR ID
        model.put("error", "Error interno: El conductor de la sesión no fue encontrado.");
        return new ModelAndView("errorAcceso", model);
        
    } catch (UsuarioNoAutorizadoException e) {
        // MANEJO DEL ERROR DE AUTORIZACIÓN (si el servicio lo lanza por conductor=null, aunque ya chequeamos)
        model.put("error", "No tenés permisos para ver los viajes");
        return new ModelAndView("errorAcceso", model);
    }
}

    @GetMapping("/cancelarViaje/{id}")
    public ModelAndView irACancelarViaje(@PathVariable Long id, HttpSession session) throws NotFoundException {
        ModelMap model = new ModelMap();

        Object usuarioId = session.getAttribute("usuarioId");
        Object rol = session.getAttribute("rol");

        // Validación de sesión
        if (usuarioId == null || !"CONDUCTOR".equals(rol)) {
            return new ModelAndView("redirect:/conductor/login");
        }

        Usuario usuarioEnSesion = new Usuario();
        usuarioEnSesion.setId((Long) usuarioId);
        usuarioEnSesion.setRol((String) rol);

        try {
        Viaje viaje = servicioViaje.obtenerViajePorId(id);
        ViajeVistaDTO viajeDTO = new ViajeVistaDTO(viaje);

        model.put("viaje", viajeDTO);
        return new ModelAndView("cancelarViaje", model);
    
    } catch (ViajeNoEncontradoException e) {
        model.put("error", "No se encontró el viaje especificado.");
        return new ModelAndView("errorCancelarViaje", model);
    } catch (UsuarioNoAutorizadoException e) {
        model.put("error", "No tiene permisos para acceder a este viaje.");
        return new ModelAndView("errorCancelarViaje", model);
    }
}

    @PostMapping("/cancelarViaje")
    public ModelAndView cancelarViaje(@RequestParam Long id, HttpSession session) {
        ModelMap model = new ModelMap();

        Object usuarioId = session.getAttribute("usuarioId");
        Object rol = session.getAttribute("rol");

        if (usuarioId == null || !"CONDUCTOR".equals(rol)) {
        return new ModelAndView("redirect:/conductor/login");
        }

        Usuario usuarioEnSesion = new Usuario();
        usuarioEnSesion.setId((Long) usuarioId);
        usuarioEnSesion.setRol((String) rol);

        try {
        servicioViaje.cancelarViaje(id, usuarioEnSesion);
        model.put("exito", "El viaje fue cancelado exitosamente.");
        return new ModelAndView("redirect:/viaje/listar");

    } catch (ViajeNoEncontradoException e) {
        model.put("error", "No se encontró el viaje especificado.");
    } catch (UsuarioNoAutorizadoException e) {
        model.put("error", "No tiene permisos para cancelar este viaje.");
    } catch (ViajeNoCancelableException e) {
        model.put("error", "El viaje no se puede cancelar en este estado.");
    } catch (Exception e) {
        model.put("error", "Ocurrió un error al intentar cancelar el viaje.");
    }

    return new ModelAndView("errorCancelarViaje", model);

    }




    /**
     * Resuelve el nombre de una ciudad a una entidad Ciudad usando Nominatim.
     * Busca en la base de datos si ya existe (por latitud/longitud), o la crea si no existe.
     *
     * @param nombreCompleto Nombre completo de la ciudad elegido del autocomplete
     * @return Ciudad entity guardada en base de datos
     * @throws NominatimResponseException si no se encuentra la ciudad
     * @throws JsonProcessingException si hay error parseando la respuesta de Nominatim
     */
    private Ciudad resolverCiudad(String nombreCompleto) throws NominatimResponseException, JsonProcessingException {
        // Buscar ciudad en Nominatim
        NominatimResponse nominatimResponse = servicioNominatim.buscarCiudadPorInputCompleto(nombreCompleto);

        // Convertir respuesta de Nominatim a entidad Ciudad
        Ciudad ciudad = new Ciudad();
        ciudad.setNombre(nominatimResponse.getName());
        ciudad.setLatitud(Float.parseFloat(nominatimResponse.getLat()));
        ciudad.setLongitud(Float.parseFloat(nominatimResponse.getLon()));

        // Guardar o buscar ciudad existente (ServicioCiudad debe manejar duplicados por lat/lon)
        return servicioCiudad.guardarCiudad(ciudad);
    }

    /**
     * Crea una lista de Paradas a partir de nombres de ciudades.
     *
     * @param nombres Lista de nombres completos de ciudades
     * @param viaje Viaje al que pertenecen las paradas
     * @return Lista de Paradas con orden asignado
     * @throws NominatimResponseException si no se encuentra alguna ciudad
     * @throws JsonProcessingException si hay error parseando respuesta de Nominatim
     */
    private List<Parada> crearParadasDesdeNombres(List<String> nombres, Viaje viaje)
            throws NominatimResponseException, JsonProcessingException {
        List<Parada> paradas = new ArrayList<>();

        for (int i = 0; i < nombres.size(); i++) {
            Ciudad ciudad = resolverCiudad(nombres.get(i));

            Parada parada = new Parada();
            parada.setOrden(i + 1); // Orden comienza en 1
            parada.setCiudad(ciudad);
            parada.setViaje(viaje);

            paradas.add(parada);
        }

        return paradas;
    }
}
