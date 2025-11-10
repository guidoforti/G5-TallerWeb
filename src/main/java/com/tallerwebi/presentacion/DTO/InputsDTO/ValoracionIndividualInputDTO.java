package com.tallerwebi.presentacion.DTO.InputsDTO;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// DTO para manejar la valoración de UN solo viajero
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ValoracionIndividualInputDTO {
    private Long receptorId; // ID del viajero que recibe la valoración
    private Integer puntuacion; // Puntuación de 1 a 5
    private String comentario; // Comentario opcional (o nulo/vacío)
}