package com.tallerwebi.presentacion.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tallerwebi.dominio.IServicio.ServicioNominatim;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/nominatim")
public class ControladorNominatim {

    private final ServicioNominatim servicioNominatim;

    @Autowired
    public ControladorNominatim(ServicioNominatim servicioNominatim) {
        this.servicioNominatim = servicioNominatim;
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<String>> buscarCiudades(@RequestParam String query) {
        // Validar que el query tenga al menos 2 caracteres
        if (query == null || query.trim().length() < 2) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        try {
            List<String> sugerencias = servicioNominatim.devolverNombresDeCiudadesPorInputIncompleto(query.trim());
            return ResponseEntity.ok(sugerencias);
        } catch (JsonProcessingException e) {
            // En caso de error, devolver lista vacía
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
}
