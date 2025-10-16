package com.tallerwebi.presentacion.Controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.tallerwebi.dominio.Entity.Ciudad;
import com.tallerwebi.dominio.Entity.Parada;
import com.tallerwebi.dominio.Entity.Vehiculo;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.IServicio.ServicioCiudad;
import com.tallerwebi.dominio.IServicio.ServicioNominatim;
import com.tallerwebi.dominio.IServicio.ServicioVehiculo;
import com.tallerwebi.dominio.IServicio.ServicioViaje;
import com.tallerwebi.dominio.excepcion.NominatimResponseException;
import com.tallerwebi.dominio.excepcion.NotFoundException;
import com.tallerwebi.dominio.excepcion.UsuarioNoAutorizadoException;
import com.tallerwebi.presentacion.DTO.InputsDTO.ViajeInputDTO;
import com.tallerwebi.presentacion.DTO.NominatimResponse;
import com.tallerwebi.presentacion.DTO.OutputsDTO.DetalleViajeOutputDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/viaje")
public class ControladorViaje {

    private final ServicioViaje servicioViaje;
    private final ServicioVehiculo servicioVehiculo;
    private final ServicioNominatim servicioNominatim;
    private final ServicioCiudad servicioCiudad;

    @Autowired
    public ControladorViaje(ServicioViaje servicioViaje,
                           ServicioVehiculo servicioVehiculo,
                           ServicioNominatim servicioNominatim,
                           ServicioCiudad servicioCiudad) {
        this.servicioViaje = servicioViaje;
        this.servicioVehiculo = servicioVehiculo;
        this.servicioNominatim = servicioNominatim;
        this.servicioCiudad = servicioCiudad;
    }


    @GetMapping("/buscarViaje")
    public ModelAndView buscarViaje() {
        ModelMap model = new ModelMap();
        return new ModelAndView("buscarViajePorId", model);
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

    @GetMapping("/detalle")
    public ModelAndView verDetalleDeUnViaje(HttpSession httpSession , @RequestParam("id") Long id) {
        ModelMap model = new ModelMap();
       /* Object rol = httpSession.getAttribute("rol");
        if (rol == null || !rol.equals("CONDUCTOR")) {
            UsuarioNoAutorizadoException e = new UsuarioNoAutorizadoException("Debe ser un usuario conductor para ver detalles de un viaje");
            model.put("error" , e.getMessage());
            return new ModelAndView("detalleViaje" , model);
        } */
        try {
            Viaje viaje = servicioViaje.obtenerDetalleDeViaje(id);
            DetalleViajeOutputDTO detalleViajeOutputDTO = new DetalleViajeOutputDTO(viaje);
            model.put("detalle" , detalleViajeOutputDTO);
            return  new ModelAndView("detalleViaje" , model);

        } catch (NotFoundException e) {
            model.put("error" , e.getMessage());
            return new ModelAndView("detalleViaje" , model);
        }
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
