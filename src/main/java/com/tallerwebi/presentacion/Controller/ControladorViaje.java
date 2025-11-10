package com.tallerwebi.presentacion.Controller;

import com.tallerwebi.dominio.Entity.Conductor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.tallerwebi.dominio.Entity.Ciudad;
import com.tallerwebi.dominio.Entity.Parada;
import com.tallerwebi.dominio.Entity.Vehiculo;
import com.tallerwebi.dominio.Entity.Viaje;
import com.tallerwebi.dominio.Enums.TipoNotificacion;
import com.tallerwebi.dominio.IServicio.*;
import com.tallerwebi.dominio.excepcion.DatoObligatorioException;
import com.tallerwebi.dominio.excepcion.UsuarioInexistente;
import com.tallerwebi.dominio.excepcion.UsuarioNoAutorizadoException;
import com.tallerwebi.dominio.excepcion.ViajeNoCancelableException;
import com.tallerwebi.dominio.excepcion.ViajeNoEncontradoException;
import com.tallerwebi.presentacion.DTO.InputsDTO.BusquedaViajeInputDTO;
import com.tallerwebi.presentacion.DTO.InputsDTO.ViajeEdicionDTO;
import com.tallerwebi.presentacion.DTO.InputsDTO.ViajeInputDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ViajeResultadoDTO;
import com.tallerwebi.presentacion.DTO.OutputsDTO.ViajeVistaDTO;
import com.tallerwebi.dominio.excepcion.NominatimResponseException;
import com.tallerwebi.dominio.excepcion.NotFoundException;
import com.tallerwebi.presentacion.DTO.NominatimResponse;
import com.tallerwebi.presentacion.DTO.OutputsDTO.DetalleViajeOutputDTO;
import com.tallerwebi.presentacion.DTO.ViajeroDTO;
import com.tallerwebi.dominio.Entity.Viajero;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
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
    private final ServicioReserva servicioReserva;
    private final ServicioNotificacion servicioNotificacion;

    @Autowired
    public ControladorViaje(ServicioViaje servicioViaje,
                            ServicioVehiculo servicioVehiculo,
                            ServicioNominatim servicioNominatim,
                            ServicioCiudad servicioCiudad,
                            ServicioConductor servicioConductor,
                            ServicioReserva servicioReserva,
                            ServicioNotificacion servicioNotificacion) {
        this.servicioViaje = servicioViaje;
        this.servicioVehiculo = servicioVehiculo;
        this.servicioNominatim = servicioNominatim;
        this.servicioCiudad = servicioCiudad;
        this.servicioConductor = servicioConductor;
        this.servicioReserva = servicioReserva;
        this.servicioNotificacion = servicioNotificacion;
    }



    @GetMapping("/buscar")
    public ModelAndView buscarViaje(HttpSession session) {
        ModelMap model = new ModelMap();

        // Validar sesión
        Object usuarioId = session.getAttribute("idUsuario");
        String rol = (String) session.getAttribute("ROL");
        if (usuarioId == null) {
            return new ModelAndView("redirect:/login");
        }
        Long userId = (Long) usuarioId;
        try {
            Long contador = servicioNotificacion.contarNoLeidas(userId);
            model.put("contadorNotificaciones", contador.intValue());
        } catch (NotFoundException e) {
            model.put("contadorNotificaciones", 0);
        }
        model.put("idUsuario", userId);
        model.put("ROL", rol);



        // Retornar vista con formulario vacío
        model.put("busqueda", new BusquedaViajeInputDTO());
        return new ModelAndView("buscarViaje", model);
    }

    @PostMapping("/buscar")
    public ModelAndView buscarViajePost(@ModelAttribute("busqueda") BusquedaViajeInputDTO busquedaDTO,
                                        HttpSession session) {
        ModelMap model = new ModelMap();

        // Validar sesión
        Object usuarioId = session.getAttribute("idUsuario");
        String rol = (String) session.getAttribute("ROL");
        if (usuarioId == null) {
            return new ModelAndView("redirect:/login");
        }

        if (usuarioId != null) {
            Long userId = (Long) usuarioId;
            try {
                Long contador = servicioNotificacion.contarNoLeidas(userId);
                model.put("contadorNotificaciones", contador.intValue());
            } catch (NotFoundException e) {
                model.put("contadorNotificaciones", 0);
            }
            model.put("idUsuario", userId);
            model.put("ROL", rol);
        }

        try {
            // Resolver ciudades usando Nominatim
            Ciudad origen = resolverCiudad(busquedaDTO.getNombreCiudadOrigen());
            Ciudad destino = resolverCiudad(busquedaDTO.getNombreCiudadDestino());

            // Convertir LocalDate a LocalDateTime (inicio del día)
            LocalDateTime fechaSalidaDateTime = null;
            if (busquedaDTO.getFechaSalida() != null) {
                fechaSalidaDateTime = busquedaDTO.getFechaSalida().atStartOfDay();
            }

            // Buscar viajes disponibles
            List<Viaje> viajes = servicioViaje.buscarViajesDisponibles(
                origen,
                destino,
                fechaSalidaDateTime,
                busquedaDTO.getPrecioMin(),
                busquedaDTO.getPrecioMax()
            );

            // Convertir a DTOs
            List<ViajeResultadoDTO> resultados = viajes.stream()
                .map(ViajeResultadoDTO::new)
                .collect(Collectors.toList());

            model.put("resultados", resultados);
            model.put("busqueda", busquedaDTO);

            // Si no hay resultados, agregar mensaje
            if (resultados.isEmpty()) {
                model.put("mensaje", "No se encontraron viajes disponibles con los filtros seleccionados.");
            }

            return new ModelAndView("buscarViaje", model);

        } catch (NominatimResponseException e) {
            model.put("error", "Error al buscar las ciudades: " + e.getMessage());
            model.put("busqueda", busquedaDTO);
            return new ModelAndView("buscarViaje", model);
        } catch (DatoObligatorioException e) {
            model.put("error", e.getMessage());
            model.put("busqueda", busquedaDTO);
            return new ModelAndView("buscarViaje", model);
        } catch (Exception e) {
            model.put("error", "Ocurrió un error al buscar viajes: " + e.getMessage());
            model.put("busqueda", busquedaDTO);
            return new ModelAndView("buscarViaje", model);
        }
    }

    @GetMapping("/buscarViaje")
    public ModelAndView buscarViajePorId() {
        ModelMap model = new ModelMap();
        return new ModelAndView("buscarViajePorId", model);
    }


    @GetMapping("/publicar")
    public ModelAndView irAPublicarViaje(HttpSession session) {
        ModelMap model = new ModelMap();
        //prueba
        // CLAVES CORREGIDAS
        Object usuarioId = session.getAttribute("idUsuario");
        Object rol = session.getAttribute("ROL");

        if (usuarioId == null || !"CONDUCTOR".equals(rol)) {
            return new ModelAndView("redirect:/login");
        }

        Long conductorId = (Long) usuarioId;

        try {
            Long contador = servicioNotificacion.contarNoLeidas(conductorId);
            model.put("contadorNotificaciones", contador.intValue());
        } catch (NotFoundException e) {
            model.put("contadorNotificaciones", 0);
        }
        model.put("idUsuario", conductorId);
        model.put("ROL", rol);
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

        // CLAVES CORREGIDAS
        Object usuarioIdObj = session.getAttribute("idUsuario");
        Object rol = session.getAttribute("ROL");

        if (usuarioIdObj == null || !"CONDUCTOR".equals(rol)) {
            return new ModelAndView("redirect:/login");
        }

        Long conductorId = (Long) usuarioIdObj;

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
            // Se debe volver a cargar la lista de vehículos para evitar NullPointerException en la vista
            List<Vehiculo> vehiculos = servicioVehiculo.obtenerVehiculosParaConductor(conductorId);
            model.put("viaje", viajeInputDTO);
            model.put("vehiculos", vehiculos);
            model.put("error", e.getMessage());
            return new ModelAndView("publicarViaje", model);
        }
    }

    @GetMapping("/listar")
    public ModelAndView listarViajes(HttpSession session) {
        ModelMap model = new ModelMap();

        // CLAVES CORREGIDAS
        Object usuarioIdObj = session.getAttribute("idUsuario");
        Object rol = session.getAttribute("ROL");

        // 1. Validación de Sesión y Rol
        if (usuarioIdObj == null || !"CONDUCTOR".equals(rol)) {
            // Si falla la validación inicial, redirige al login.
            return new ModelAndView("redirect:/login", model);
        }

        Long conductorId = (Long) usuarioIdObj;
        agregarContadorNotificaciones(model, conductorId);
        try {
            // 2. BUSCAR AL CONDUCTOR
            Conductor conductorEnSesion = servicioConductor.obtenerConductor(conductorId);

            // 3. Listar Viajes
            List<Viaje> listaViajes = servicioViaje.listarViajesPorConductor(conductorEnSesion);

            // dtos para la vista
            List<ViajeVistaDTO> listaViajesDTO = listaViajes.stream()
                    .map(ViajeVistaDTO::new)
                    .collect(Collectors.toList());

            model.put("listaViajes", listaViajesDTO);
            return new ModelAndView("listarViajesConductor", model);

        } catch (UsuarioInexistente e) {
            // MANEJO DEL ERROR DE BÚSQUEDA POR ID (Sesión válida, pero usuario borrado de DB)
            model.put("error", "Error interno: El conductor de la sesión no fue encontrado.");
            return new ModelAndView("errorAcceso", model);

        } catch (UsuarioNoAutorizadoException e) {
            // MANEJO DEL ERROR DE AUTORIZACIÓN
            model.put("error", "No tenés permisos para ver los viajes");
            return new ModelAndView("errorAcceso", model);
        }
    }

    @GetMapping("/cancelarViaje/{id}")
    public ModelAndView irACancelarViaje(@PathVariable Long id, HttpSession session) throws NotFoundException {
        ModelMap model = new ModelMap();

        // CLAVES CORREGIDAS
        Object usuarioId = session.getAttribute("idUsuario");
        Object rol = session.getAttribute("ROL");

        // Validación de sesión
        if (usuarioId == null || !"CONDUCTOR".equals(rol)) {
            return new ModelAndView("redirect:/login");
        }

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

        // CLAVES CORREGIDAS
        Object usuarioIdObj = session.getAttribute("idUsuario");
        Object rol = session.getAttribute("ROL");

        if (usuarioIdObj == null || !"CONDUCTOR".equals(rol)) {
            return new ModelAndView("redirect:/login");
        }


        Long conductorId = (Long) usuarioIdObj;
        Conductor conductorEnSesion;

        try {
            Viaje viajeACancelar = servicioViaje.obtenerViajePorId(id);
            conductorEnSesion = servicioConductor.obtenerConductor(conductorId);
            servicioViaje.cancelarViaje(id, conductorEnSesion);
            List<Viajero> todosLosViajeros = servicioReserva.obtenerViajerosConfirmados(viajeACancelar);

            for (Viajero viajero : todosLosViajeros) {
                String mensaje = String.format("¡ATENCIÓN! El viaje a %s ha sido CANCELADO.", viajeACancelar.getDestino().getNombre());
                String url = "/reserva/misReservasActivas"; // Redirigir al listado de sus reservas

                servicioNotificacion.crearYEnviar(viajero, TipoNotificacion.VIAJE_CANCELADO, mensaje, url);
            }
            model.put("exito", "El viaje fue cancelado exitosamente.");
            return new ModelAndView("redirect:/viaje/listar");

        } catch (ViajeNoEncontradoException e) {
            model.put("error", "No se encontró el viaje especificado.");
        } catch (UsuarioNoAutorizadoException e) {
            model.put("error", "No tiene permisos para cancelar este viaje.");
        } catch (ViajeNoCancelableException e) {
            model.put("error", "El viaje no se puede cancelar en este estado.");
        } catch (UsuarioInexistente e) {
            // Manejamos UsuarioInexistente que puede lanzar obtenerConductor, aunque el flujo no lo cubra directamente en el try/catch
            model.put("error", "Error interno: El conductor de la sesión no fue encontrado.");
        } catch (Exception e) {
            model.put("error", "Ocurrió un error al intentar cancelar el viaje.");
        }

        return new ModelAndView("errorCancelarViaje", model);

    }


    @GetMapping("/detalle")
    public ModelAndView verDetalleDeUnViaje(HttpSession httpSession, @RequestParam("id") Long id) {
        ModelMap model = new ModelMap();

        Object usuarioIdObj = httpSession.getAttribute("idUsuario");
        Object rolObj = httpSession.getAttribute("ROL");

        if (usuarioIdObj == null || rolObj == null) {
            return new ModelAndView("redirect:/login");
        }

        String userRole = rolObj.toString();
        model.put("userRole", userRole);

        if (!userRole.equals("CONDUCTOR") && !userRole.equals("VIAJERO")) {
            model.put("error", "Su rol no tiene acceso a la visualización de detalles de viaje.");
            return new ModelAndView("usuarioNoAutorizado", model);
        }

        try {
            Viaje viaje = servicioViaje.obtenerDetalleDeViaje(id);

            List<Viajero> viajerosConfirmados = servicioReserva.obtenerViajerosConfirmados(viaje);
            List<ViajeroDTO> viajerosDTO = viajerosConfirmados.stream()
                    .map(ViajeroDTO::new)
                    .collect(Collectors.toList());

            boolean reservaExistente = false;
            if (userRole.equals("VIAJERO")) {
                Long viajeroId = (Long) usuarioIdObj;
                reservaExistente = servicioReserva.tieneReservaActiva(viajeroId, id);
            }

            model.put("reservaExistente", reservaExistente);

            DetalleViajeOutputDTO detalleViajeOutputDTO = new DetalleViajeOutputDTO(viaje, viajerosDTO);
            model.put("detalle", detalleViajeOutputDTO);
            model.put("viajeId", id);

            return new ModelAndView("detalleViaje", model);

        } catch (NotFoundException | ViajeNoEncontradoException | UsuarioNoAutorizadoException e) {
            model.put("error", e.getMessage());
            return new ModelAndView("detalleViaje", model);
        }
    }

    @GetMapping("/editar/{id}")
    public ModelAndView mostrarFormularioEdicion(HttpSession httpSession, @PathVariable Long id) throws UsuarioNoAutorizadoException, ViajeNoEncontradoException, NotFoundException, UsuarioInexistente {
        ModelMap model = new ModelMap();
        //prueba
        // CLAVES CORREGIDAS
        Object usuarioId = httpSession.getAttribute("idUsuario");
        Object rol = httpSession.getAttribute("ROL");
        if (usuarioId == null || !"CONDUCTOR".equals(rol)) {
            Exception e = new UsuarioNoAutorizadoException("usuario no autorizado");
            model.addAttribute("error", e.getMessage());
            return new ModelAndView("usuarioNoAutorizado", model);
        }


        // Obtener el viaje existente
        Viaje viaje = servicioViaje.obtenerViajeConParadas(id);

        // Verificar que el viaje pertenezca al conductor
        if (!viaje.getConductor().getId().equals(usuarioId)) {
            throw new UsuarioNoAutorizadoException("No tienes permiso para editar este viaje");
        }
        Long conductorId = (Long) usuarioId;
        Conductor conductorEnSesion;
        conductorEnSesion = servicioConductor.obtenerConductor(conductorId);
        // Convertir a DTO para el formulario

        ViajeEdicionDTO viajeDTO = new ViajeEdicionDTO();
        viajeDTO.setId(viaje.getId());
        viajeDTO.setNombreCiudadOrigen(viaje.getOrigen().getNombre());
        viajeDTO.setNombreCiudadDestino(viaje.getDestino().getNombre());
        viajeDTO.setVehiculoId(viaje.getVehiculo().getId());
        viajeDTO.setFechaHoraDeSalida(viaje.getFechaHoraDeSalida());
        viajeDTO.setPrecio(viaje.getPrecio());
        viajeDTO.setAsientosDisponibles(viaje.getAsientosDisponibles());
        List<String> paradas = viaje.getParadas().stream()
                .map(parada -> parada.getCiudad().getNombre())  // Obtener el nombre de la ciudad
                .collect(Collectors.toList());
        viajeDTO.setNombreParadas(paradas);
        model.addAttribute("viaje", viajeDTO);
        model.addAttribute("vehiculos", servicioVehiculo.obtenerVehiculosParaConductor(conductorEnSesion.getId()));
        return new ModelAndView("editarViaje", model);
    }

    @PostMapping("/editar")
    public ModelAndView editarViajer(HttpSession httpSession, @ModelAttribute("viaje") ViajeEdicionDTO viaje) throws UsuarioInexistente {
        ModelMap model = new ModelMap();
        Object usuarioId = httpSession.getAttribute("idUsuario");
        Object rol = httpSession.getAttribute("ROL");

        if (usuarioId == null || !"CONDUCTOR".equals(rol)) {
            Exception e = new UsuarioNoAutorizadoException("usuario no autorizado");
            model.addAttribute("error", e.getMessage());
            return new ModelAndView("usuarioNoAutorizado", model);
        }
        Long conductorId = (Long) usuarioId;
        Conductor conductorEnSesion;
        conductorEnSesion = servicioConductor.obtenerConductor(conductorId);


        try {
            // Obtener el viaje existente
            Viaje viajeExistente = servicioViaje.obtenerViajeConParadas(viaje.getId());

            // Verificar que el viaje pertenece al conductor
            if (!viajeExistente.getConductor().getId().equals(usuarioId)) {
                model.addAttribute("error", "No tienes permiso para editar este viaje");
                return new ModelAndView("usuarioNoAutorizado", model);
            }

            // Resolver ciudades en el controlador
            Ciudad origen = resolverCiudad(viaje.getNombreCiudadOrigen());
            Ciudad destino = resolverCiudad(viaje.getNombreCiudadDestino());

            // Resolver ciudades de las paradas
            List<Parada> paradasActualizadas = new ArrayList<>();
            if (viaje.getNombreParadas() != null) {
                for (int i = 0; i < viaje.getNombreParadas().size(); i++) {
                    String nombreParada = viaje.getNombreParadas().get(i);
                    if (nombreParada != null && !nombreParada.trim().isEmpty()) {
                        Ciudad ciudadParada = resolverCiudad(nombreParada.trim());
                        Parada parada = new Parada(ciudadParada, i + 1);
                        parada.setViaje(viajeExistente);
                        paradasActualizadas.add(parada);
                    }
                }
            }

            // Convertir DTO a entidad con las ciudades resueltas
            Vehiculo vehiculo = servicioVehiculo.getById(viaje.getVehiculoId());
            Viaje viajeActualizado = viaje.toEntity(origen, destino, vehiculo);
            viajeActualizado.setId(viaje.getId());

            // Llamar al servicio para modificar el viaje con las paradas ya resueltas
            servicioViaje.modificarViaje(viajeActualizado, paradasActualizadas);

            model.put("exito", "Se modificó el viaje con éxito");
            return new ModelAndView("redirect:/viaje/listar", model);

        } catch (Exception e) {
            try{
                Conductor conductorRecargado = servicioConductor.obtenerConductor(conductorId);
                List<Vehiculo> vehiculos = servicioVehiculo.obtenerVehiculosParaConductor(conductorRecargado.getId());
                model.addAttribute("vehiculos", vehiculos);
            }catch (UsuarioInexistente ex){

            }
            List<Vehiculo> vehiculos = servicioVehiculo.obtenerVehiculosParaConductor(conductorEnSesion.getId());
            model.addAttribute("viaje", viaje);
            model.addAttribute("error", "Error al modificar el viaje: " + e.getMessage());

            return new ModelAndView("editarViaje", model);
        }
    }


    /**
     * Resuelve el nombre de una ciudad a una entidad Ciudad usando Nominatim.
     * Busca en la base de datos si ya existe (por latitud/longitud), o la crea si no existe.
     *
     * @param nombreCompleto Nombre completo de la ciudad elegido del autocomplete
     * @return Ciudad entity guardada en base de datos
     * @throws NominatimResponseException si no se encuentra la ciudad
     * @throws JsonProcessingException    si hay error parseando la respuesta de Nominatim
     */
    private Ciudad resolverCiudad(String nombreCompleto) throws NominatimResponseException, JsonProcessingException {
        // Buscar ciudad en Nominatim
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
            return null;
        }
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
     * @param viaje   Viaje al que pertenecen las paradas
     * @return Lista de Paradas con orden asignado
     * @throws NominatimResponseException si no se encuentra alguna ciudad
     * @throws JsonProcessingException    si hay error parseando respuesta de Nominatim
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

    /**
     * Endpoint para iniciar un viaje
     */
    @PostMapping("/{id}/iniciar")
    public ModelAndView iniciarViaje(@PathVariable Long id, HttpSession session) {
        ModelMap model = new ModelMap();

        // Validar sesión
        Long conductorId = (Long) session.getAttribute("idUsuario");
        if (conductorId == null) {
            return new ModelAndView("redirect:/login");
        }

        try {
            servicioViaje.iniciarViaje(id, conductorId);
            Viaje viaje = servicioViaje.obtenerViajePorId(id);
            List<Viajero> viajerosConfirmados = servicioReserva.obtenerViajerosConfirmados(viaje);
            for (Viajero viajero : viajerosConfirmados) {
                String mensaje = String.format("¡Tu viaje a %s ha comenzado!", viaje.getDestino().getNombre());
                String url = "/reserva/misViajes";
                servicioNotificacion.crearYEnviar(viajero, TipoNotificacion.VIAJE_INICIADO, mensaje, url);
            }
            model.put("mensaje", "Viaje iniciado correctamente");
        } catch (ViajeNoEncontradoException e) {
            model.put("error", "Viaje no encontrado");
        } catch (UsuarioNoAutorizadoException e) {
            model.put("error", "No tienes permiso para iniciar este viaje");
        } catch (Exception e) {
            model.put("error", e.getMessage());
        }

        model.put("viajeId", id);
        return new ModelAndView("accionViajeCompletada", model);
    }

    /**
     * Endpoint para finalizar un viaje
     */
    @PostMapping("/{id}/finalizar")
    public ModelAndView finalizarViaje(@PathVariable Long id, HttpSession session) {
        ModelMap model = new ModelMap();

        // Validar sesión
        Long conductorId = (Long) session.getAttribute("idUsuario");
        if (conductorId == null) {
            return new ModelAndView("redirect:/login");
        }

        try {
            servicioViaje.finalizarViaje(id, conductorId);
            model.put("mensaje", "Viaje finalizado correctamente");
        } catch (ViajeNoEncontradoException e) {
            model.put("error", "Viaje no encontrado");
        } catch (UsuarioNoAutorizadoException e) {
            model.put("error", "No tienes permiso para finalizar este viaje");
        } catch (Exception e) {
            model.put("error", e.getMessage());
        }
        
        model.put("viajeId", id);
        model.put("accionFinalizada", true);
        model.put("idUsuario", conductorId);
        return new ModelAndView("accionViajeCompletada", model);
    }


}