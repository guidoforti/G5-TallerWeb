package com.tallerwebi.presentacion.Controller;

import com.tallerwebi.dominio.IServicio.ServicioCiudad;
import com.tallerwebi.dominio.Entity.Ciudad;
import com.tallerwebi.presentacion.DTO.CiudadDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.stream.Collectors;

import java.util.List;

@Controller
@RequestMapping("/ciudades")

public class ControladorCiudad {

    private final ServicioCiudad servicioCiudad;

    public ControladorCiudad(ServicioCiudad servicioCiudad) {
        this.servicioCiudad = servicioCiudad;
    }

    @PostMapping("/origen")
    public ResponseEntity<CiudadDTO> crearOrigen(@RequestBody CiudadDTO ciudadDTO) {
        Ciudad ciudad = servicioCiudad.guardarCiudad(ciudadDTO.toEntity());
        return ResponseEntity.ok(new CiudadDTO(ciudad));
    }

    @PostMapping("/destino")
    public ResponseEntity<CiudadDTO> crearDestino(@RequestBody CiudadDTO ciudadDTO) {
        Ciudad ciudad = servicioCiudad.guardarCiudad(ciudadDTO.toEntity());
        return ResponseEntity.ok(new CiudadDTO(ciudad));
    }

    @PostMapping("/origen-destino")
    public ResponseEntity<List<CiudadDTO>> crearOrigenDestino(@RequestBody List<CiudadDTO> ciudadesDTO) {
        List<CiudadDTO> result = ciudadesDTO.stream()
                .map(dto -> new CiudadDTO(servicioCiudad.guardarCiudad(dto.toEntity())))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<CiudadDTO>> listarTodas() {
        List<CiudadDTO> ciudades = servicioCiudad.listarTodas()
                .stream()
                .map(CiudadDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ciudades);
    }
}

