package com.tallerwebi.presentacion.DTO.OutputsDTO;

import com.tallerwebi.dominio.Entity.Viajero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class ViajeroParaValorarOutputDTO {
    private Long id;
    private String nombre;
    private String email;
    // Agrega aquí cualquier otra propiedad que necesites de Viajero/Usuario.

    // Constructor que extrae explícitamente los datos de la entidad
    public ViajeroParaValorarOutputDTO(Viajero viajero) {
        this.id = viajero.getId();
        this.nombre = viajero.getNombre(); // Acceso a la propiedad DENTRO de la transacción
        this.email = viajero.getEmail();   // Acceso a la propiedad DENTRO de la transacción
    }
}