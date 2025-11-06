package com.tallerwebi.presentacion.DTO.InputsDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ValoracionNuevaInputDTO {
    private Long receptorId;
    private Integer puntuacion;
    private String comentario;
}
