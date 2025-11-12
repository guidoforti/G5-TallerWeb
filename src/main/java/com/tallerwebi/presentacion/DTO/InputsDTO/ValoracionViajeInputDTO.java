package com.tallerwebi.presentacion.DTO.InputsDTO;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ValoracionViajeInputDTO {
    // Lista de las valoraciones individuales
    private List<ValoracionIndividualInputDTO> valoraciones;
}