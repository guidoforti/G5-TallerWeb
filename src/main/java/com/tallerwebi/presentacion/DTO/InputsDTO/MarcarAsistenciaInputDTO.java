package com.tallerwebi.presentacion.DTO.InputsDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MarcarAsistenciaInputDTO {

    private Long reservaId;
    private String asistencia; // "PRESENTE" or "AUSENTE"
}
